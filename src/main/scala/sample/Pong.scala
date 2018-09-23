/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sample

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import io.aeron.Aeron
import io.aeron.Publication
import io.aeron.Subscription
import io.aeron.logbuffer.Header
import io.aeron.FragmentAssembler
import io.aeron.driver.MediaDriver
import org.agrona.BufferUtil
import org.agrona.DirectBuffer
import org.agrona.concurrent.BusySpinIdleStrategy
import org.agrona.concurrent.ShutdownSignalBarrier
import org.agrona.concurrent.SigInt
import org.agrona.concurrent.UnsafeBuffer
import org.agrona.BitUtil._
import org.slf4j.LoggerFactory
import scala.util.Either
import scala.util.Failure
import scala.util.Success
import scala.util.Try


object Pong {

  def create(aeronCtx: Aeron.Context = aeronContext): Pong = new Pong(aeronCtx)

  def main(args: Array[String]) {
    val ponger = create() 
    ponger.receive()
  }

}
class Pong(aeronCtx: Aeron.Context) {

  import Pong._

  val log = LoggerFactory.getLogger(classOf[Pong])

  require(null != aeronCtx, "Aeron.Context is not presented!")

  def receive() {
    log.info("Pong ...")
    val aeron = Aeron.connect(aeronCtx)
    val sub = aeron.addSubscription(pingChannel("localhost", 40123), 10)

    val flag = new AtomicBoolean(true)
    SigInt.register({ () => flag.set(false) })

    log.info("Before handler ...")
    val handler = new FragmentAssembler({ 
      (buffer: DirectBuffer, offset: Int, length: Int, header: Header) => {
        log.info(s"Buffer: $buffer, offset: $offset, length: $length, header: $header ...")
        val data = new Array[Byte](length)
        buffer.getBytes(offset, data) 
        log.info(s"Received data => ${new String(data)} ...")
        flag.set(false)
      }
    })
 
    log.info("IdleStragety ...")
    val idleStrategy = new BusySpinIdleStrategy()

    log.info("Before while() ...")
    while(flag.get) {
      val read = sub.poll(handler, 10)
      idleStrategy.idle(read)
    }
    log.info("Finish receive() ...")
  }

}
