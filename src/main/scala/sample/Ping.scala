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
import io.aeron.Aeron
import io.aeron.Publication
import org.agrona.BufferUtil
import org.agrona.concurrent.BusySpinIdleStrategy
import org.agrona.concurrent.SigInt
import org.agrona.concurrent.UnsafeBuffer
import org.agrona.BitUtil._
import org.slf4j.LoggerFactory


object Ping {

  def create(aeronCtx: Aeron.Context = aeronContext): Ping = new Ping(aeronCtx)

  def main(args: Array[String]) {
    val pinger = create()
    pinger.send()
  }

}
class Ping(aeronCtx: Aeron.Context) {

  import Ping._

  val log = LoggerFactory.getLogger(classOf[Ping])

  require(null != aeronCtx, "Aeron.Context is not presented!")

  def send() {
    log.info("Ping ...")
    val buffer = new UnsafeBuffer(BufferUtil.allocateDirectAligned(512, CACHE_LINE_LENGTH))
    val aeron = Aeron.connect(aeronCtx)
    val pub = aeron.addPublication(pingChannel("localhost", 40123), 10)
    val messageBytes = "Hello World!".getBytes 
    log.info(s"Befoer UnsafeBuffer.putLong() ...")
    buffer.putBytes(0, messageBytes)
    val deadline = System.nanoTime + TimeUnit.SECONDS.toNanos(5)
    while(!pub.isConnected) {
      if(System.nanoTime >= deadline) {
        log.error(s"Can't connect to publication!")
        return
      }
      Thread.sleep(1)
    }
    val result = pub.offer(buffer, 0, messageBytes.length)
    log.info(s"After publication.offer() => result is $result ...")
    log.info("Finish send()  ...")

  }

}
