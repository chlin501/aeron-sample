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
import io.aeron.Aeron
import io.aeron.driver.MediaDriver
import io.aeron.driver.ThreadingMode._
import org.agrona.concurrent.BusySpinIdleStrategy

package object sample {

  val pingChannel = (host: String, port: Int) => s"aeron:udp?endpoint=$host:$port"

  val pongChannel = (host: String, port: Int) => s"aeron:udp?endpoint=$host:$port"

  def mediaDriverContext = new MediaDriver.Context().
    threadingMode(DEDICATED).
    conductorIdleStrategy(new BusySpinIdleStrategy).
    receiverIdleStrategy(new BusySpinIdleStrategy).
    senderIdleStrategy(new BusySpinIdleStrategy)

  def aeronContext = new Aeron.Context()


}
