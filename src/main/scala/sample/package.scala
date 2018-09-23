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
