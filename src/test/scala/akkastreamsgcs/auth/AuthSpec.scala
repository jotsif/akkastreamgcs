package akkastreamsgcs.auth

import scala.concurrent.Await
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentType, HttpCharsets}
import akka.http.scaladsl.model.MediaTypes._
import akka.stream.ActorMaterializer
import akkastreamsgcs.ErrorMessage
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

class AuthSpec extends FlatSpec with BeforeAndAfterAll with Matchers with ScalaFutures {
  implicit val as = ActorSystem("testsystem")
  implicit val mat = ActorMaterializer()
  val pk = sys.env("GCS_PRIVATE_KEY")
  val email = "test@test.nu"

  "Auth" should "create a correct token request" in {
    val tokenrequest = Auth.tokenRequest(email, pk, Seq(Auth.GcsFullControlScope))
    tokenrequest.entity.contentType should be (ContentType(`application/x-www-form-urlencoded`, HttpCharsets.`UTF-8`))
  }
  it should "be able to send request to Google" in { 
    val tokenresp = Auth.getToken(email, pk)
    val ready = Await.ready(tokenresp, 10.seconds)
    ready.futureValue should be (ErrorMessage("invalid_client",Some("The OAuth client was invalid.")))
  }
  override def afterAll = {
    as.terminate()
  }  
}
