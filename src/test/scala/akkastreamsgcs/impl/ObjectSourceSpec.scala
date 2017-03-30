package akkastreamsgcs.impl

import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.Await
import scala.concurrent.duration._
import akkastreamsgcs.auth.Auth
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akkastreamsgcs.GoogleToken
import org.scalatest._
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import scala.collection.JavaConverters._

class ObjectSourceSpec extends FlatSpec with BeforeAndAfterAll with Matchers with ScalaFutures {
  val conf = ConfigFactory.parseMap(Map("akka.http.client.parsing.max-content-length" -> "300000000").asJava)
  implicit val as = ActorSystem("testsystem", conf)
  implicit val mat = ActorMaterializer()
  val pk = sys.env("GCS_PRIVATE_KEY")
  val email = sys.env("GCS_CLIENT_EMAIL")  
  "ObjectSource" should "should get an object stream" in {
    import mat.executionContext
    val tokenresp = Auth.getToken(email, pk)
    val source = tokenresp
      .flatMap(token => {
        ObjectSource.create("ru-recorder", "1490144541256", token.asInstanceOf[GoogleToken].access_token).runWith(Sink.head)
      })
    val sourceready = Await.ready(source, 10.seconds)
    sourceready.futureValue.head should be (31)
    
  }
}
