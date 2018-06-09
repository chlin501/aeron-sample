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

import io.aeron.Aeron
import io.aeron.FragmentAssembler
import io.aeron.driver.MediaDriver
import io.aeron.driver.ThreadingMode._
import org.agrona.BufferUtil
import org.agrona.concurrent.BusySpinIdleStrategy
import org.agrona.concurrent.UnsafeBuffer
import org.agrona.BitUtil._

object p {

  val pingChannel = "aeron:udp?endpoint=localhost:40123"

  val pongChannel = "aeron:udp?endpoint=localhost:40124"

  val msgLength = 256

  val atomicBuffer = new UnsafeBuffer(BufferUtil.allocateDirectAligned(
    msgLength, CACHE_LINE_LENGTH
  ))

  def main(args: Array[String]) {
    
  }

}

class p {

  import p._

  val driver = {
    val ctx = new MediaDriver.Context().
                              threadingMode(DEDICATED).
                              conductorIdleStrategy(new BusySpinIdleStrategy).
                              receiverIdleStrategy(new BusySpinIdleStrategy).
                              senderIdleStrategy(new BusySpinIdleStrategy)
    MediaDriver.launch(ctx)
  }

  val aeron = {
    val ctx = new Aeron.Context().aeronDirectoryName(driver.aeronDirectoryName)
    Aeron.connect(ctx)
  }

  val pub = aeron.addPublication(pingChannel, 10)
  val sub = aeron.addSubscription(pongChannel, 10)

  var offeredPosition = 0L

  do {
    atomicBuffer.putLong(0, System.nanoTime) 
  } while ({ 
    offeredPosition = pub.offer(atomicBuffer, 0, msgLength)
    offeredPosition < 0L
  })

}
