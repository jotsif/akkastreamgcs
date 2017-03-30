package akkastreamsgcs.impl

import scala.concurrent.Await
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akkastreamsgcs.GoogleToken
import akkastreamsgcs.auth.Auth
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

class ListBucketSpec extends FlatSpec with BeforeAndAfterAll with Matchers with ScalaFutures {
  implicit val as = ActorSystem("testsystem")
  implicit val mat = ActorMaterializer()
  val pk = sys.env("GCS_PRIVATE_KEY")
  val email = sys.env("GCS_CLIENT_EMAIL")
  "ListBucket" should "should be able to list contents of a bucket" in {
    import mat.executionContext
    val tokenresp = Auth.getToken(email, pk)
    val source = tokenresp
      .flatMap(token => {
        val request = ListBucketRequest("ru-recorder", None, None, token.asInstanceOf[GoogleToken].access_token)
        ListBucket.list(request).runWith(Sink.head)
      })
    val sourceready = Await.ready(source, 10.seconds)
    sourceready.futureValue.name should be ("1490144541256")
  }
  override def afterAll = {
    as.terminate()
  } 
}
