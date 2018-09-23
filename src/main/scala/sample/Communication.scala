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

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit._
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
import scala.util.Either
import scala.util.Failure
import scala.util.Success
import scala.util.Try

object Communication {
  def main(args: Array[String]) {
    val pool = Executors.newScheduledThreadPool(3)
    pool.schedule(Starter.create(), 0, SECONDS)
    pool.schedule(Pong.create(), 2, SECONDS)
    pool.schedule(Ping.create(), 5, SECONDS)
    Thread.sleep(10* 1000)
    pool.shutdownNow
    println("done!")
  }
}

object Starter {

  def create(mediaDriverCtx: MediaDriver.Context = mediaDriverContext): Starter = 
    new Starter(mediaDriverCtx)

}

class Starter(mediaDriverCtx: MediaDriver.Context) extends Callable[Either[Throwable, Boolean]] {

  import Starter._

  require(null != mediaDriverCtx, "MediaDriver.Context is not presented!")

  @throws(classOf[Exception])
  override def call(): Either[Throwable, Boolean] = Try(MediaDriver.launch(mediaDriverCtx)) match {
    case Success(_) => new ShutdownSignalBarrier().await; Right(true) 
    case Failure(ex) => Left(ex)
  }

}

object Ping {
 
  case class Holder(driver: MediaDriver, aeron: Aeron, pub: Publication, sub: Subscription)

  def create(
    mediaDriverCtx: MediaDriver.Context = mediaDriverContext,
    aeronCtx: Aeron.Context = aeronContext,
  ): Ping = new Ping(mediaDriverCtx, aeronCtx)

}
class Ping(mediaDriverCtx: MediaDriver.Context, aeronCtx: Aeron.Context) 
    extends Callable[Unit] {

  import Ping._

  require(null != mediaDriverCtx, "MediaDriver.Context is not presented!")
  require(null != aeronCtx, "Aeron.Context is not presented!")

  def initialize(): Holder = {
    val mediaDriver = MediaDriver.launch(mediaDriverCtx)
    val aeron = Aeron.connect(aeronCtx)
    val pub = aeron.addPublication(pingChannel("localhost", 9876), 10)
    val sub = aeron.addSubscription(pongChannel("localhost", 9877), 10)
    Holder(mediaDriver, aeron, pub, sub)
  }

  @throws(classOf[Exception])
  override def call(): Unit = {
    val holder = initialize()
    val buffer = new UnsafeBuffer(BufferUtil.allocateDirectAligned(256, CACHE_LINE_LENGTH))
    do {
      buffer.putLong(0, System.nanoTime)
    } while({
      val offeredPosition = holder.pub.offer(buffer, 0, 256)
      offeredPosition < 0L
    })
  }

}

object Pong {
 
  case class Holder(driver: MediaDriver, aeron: Aeron, pub: Publication, sub: Subscription)

  def create(
    mediaDriverCtx: MediaDriver.Context = mediaDriverContext,
    aeronCtx: Aeron.Context = aeronContext,
  ): Pong = new Pong(mediaDriverCtx, aeronCtx)

}
class Pong(mediaDriverCtx: MediaDriver.Context, aeronCtx: Aeron.Context) 
    extends Callable[Unit] {

  import Pong._

  require(null != mediaDriverCtx, "MediaDriver.Context is not presented!")
  require(null != aeronCtx, "Aeron.Context is not presented!")

  def initialize(): Holder = {
    val mediaDriver = MediaDriver.launch(mediaDriverCtx)
    val aeron = Aeron.connect(aeronCtx)
    val pub = aeron.addPublication(pongChannel("localhost", 9877), 10)
    val sub = aeron.addSubscription(pingChannel("localhost", 9876), 10)
    Holder(mediaDriver, aeron, pub, sub)
  }


  @throws(classOf[Exception])
  override def call(): Unit = {
    val holder = initialize()
    val flag = new AtomicBoolean(true)
    SigInt.register({ () => flag.set(false) })
    val handler = new FragmentAssembler({ 
      (buffer: DirectBuffer, offset: Int, length: Int, header: Header) => {
        println(s"xxxxxxxxxxx [Pong] buffer: $buffer, offset: $offset, length: $length")
      }
    })
 
    val idleStrategy = new BusySpinIdleStrategy()

    while(flag.get) {
      idleStrategy.idle(holder.sub.poll(handler, 20))
    }
  }

}
