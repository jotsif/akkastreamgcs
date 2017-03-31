package akkastreamsgcs.impl

import org.scalatest._


import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.Await
import scala.concurrent.duration._
import akkastreamsgcs.auth.Auth
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akkastreamsgcs.GoogleToken
import org.scalatest._
import akka.stream.scaladsl.{FileIO, Keep}
import com.typesafe.config.ConfigFactory
import scala.collection.JavaConverters._
import java.nio.file.{Paths}

class ObjectSinkSpec extends FlatSpec with BeforeAndAfterAll with Matchers with ScalaFutures {
  val conf = ConfigFactory.parseMap(Map("akka.http.client.parsing.max-content-length" -> "100000000").asJava)
  implicit val as = ActorSystem("testsystem", conf)
  implicit val mat = ActorMaterializer()
  val pk = sys.env("GCS_PRIVATE_KEY")
  val email = sys.env("GCS_CLIENT_EMAIL")
  val filename = "/1490345649316.gz"
  val file = Paths.get(getClass.getResource(filename).toURI())
  
  "ObjectSink" should "should upload an object stream" in {
    import mat.executionContext
    val tokenresp = Auth.getToken(email, pk)
    val source = tokenresp
      .flatMap(token => {
        FileIO.fromPath(file, 256)
          .toMat(ObjectSink.create("ru-recorder", "1490144541256", token.asInstanceOf[GoogleToken].access_token))(Keep.right)
          .run()
      })
    val sourceready = Await.ready(source, 100.seconds)
    sourceready.futureValue should be (31)
    
  }
}
