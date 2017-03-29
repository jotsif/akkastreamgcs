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


class AuthSpec extends FlatSpec with Matchers with ScalaFutures {
  implicit val as = ActorSystem("testsystem")
  implicit val mat = ActorMaterializer()
    val pk = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDuDcbzv9sFtiWK\nuoAsKYkak1Tjw7ZmRjmZs1W9RiroJy5PAZOTDbC/VlKptKFd4FGqpVvWuV2Ert24\nVYwpAdBZR5evI705MX/iQY2kAJ0IchoRJ2INPbvYgkbknxTPjwfMKIxpF5supYKi\nbFf8qg4V2DhioaZ+EN4zkBDgcBP85ZOssTu+KADtVmEMo/agERs3UWWwum2tbsw4\nJS0FCdi7B6jHYdisqNXQ8OQEjmhlO7PtBEN7mqcgWOrlampRU1+4hOQNM2BisXGd\nLlayG+BgDrDdOX3G3CKL1RrDriBEUfM/X6Rb3E6tpR5mXbH0179qOdhzbhKlT3r0\nemm2uAIJAgMBAAECggEBAJZy2Jk2WKrsah+aLOU8Pu0vzgfAqidLHJ46C+b6UKW2\nFXtTKLxYe6sBWG7uvMlCuvpZVYiIUEVJ6tDUKCfGgLHcIE5NDQr3cLZC7cyHorcy\nvay3si1iJbT46OsWayWeZLQvsEW+6JF7gus6BAWoSAygQUp8lWe5K2V1GGVwEAHU\nseQ7nDnbZyqF4Cx3otzHfjG9KU5R0N2rzIN0FLkLKz+j9YHIDDX0lCsYFY/yWn+c\nlKs+f1q93XPAbzn+bFegDROg6fUVHZQJlpGIo51+jg3/xco0omrNKgwOy1a4NxUa\nt8aAa2fQb1VVo/kPBw4ERY6qMURHML3E6D7NLEbFxN0CgYEA/ukucb4RfY+E4yWx\naEiHsow7Sj1g4PIFMGtL4wA7kfjOOzZ1dDCuKfkziwyWxbtzFvWKfHhT6qsb6opM\nK2V6FGWuKl6ts3caviqs4vw7DLYpU2+vqiJqNt4mySljiIJhmNeYV9I5hCEklU4w\n80rYBF+69bsxiYGWJ51xhNCEz18CgYEA7xIoXkLRRsCfk/pFB54pbzyr5BY0kCQN\n9AqBEG6buoAYZ0UAgTMyNFWA9SlwP/BBhN58ZoetwWk9/yK7woPck/rq9q+F40ES\nzN4vagSrUqylv3eGGiGIiwXOj6w0ttaqBF/UMUw03z5NtZwm7ybUTRxcCf7PvgKE\nWbB8aWI475cCgYB/9Yms6x5YiyzH4Wn20UHc7OvuTnVNNfBI5/OGFd3RXrYXnzTC\niJVE2KV5DW65/2i8g7Fq3fTx/oba62Vk+2GWz5voBPLo/cbc4ws6PideMCr6iTwD\nCZeLx2Rs4mvmYJyhXshIfW0F2KVGlaOY3V8mgu+U3sz1G6nGZRBQ/WNNvQKBgQC6\nNpN48GSfzpO9qFeyWlB90200CNPCXkL8Dl5/VRg5iWL4tTdya1U0jFEZJMDJHLN7\n8exF1HLTzsy6eOx0006xeOUhZpBL9bjWGE4oLyDfEZk87LVojywS1WASapjYvZXK\nOHZIO8qHBLl0tv9gkgcVVPyf0Hkx0DYUwjH1x8r/WwKBgBuPK/vT59jO1f6xLltN\nP9KWR9weoJYL2Rv+a+JWWBmUtey/A3lbonrTSkRkYfGT06mV4ANOQAmvgBO9PHVX\nG/RH5e8g0uWi3iaa61kA6aFPzrhAsWUa65uEBsBX5dllGowiFdku2asr16DRibQd\nZWj0r6T/5FqNrsl+WjMxRjWU\n\n"
  val email = "blabla@blabla.gserviceaccount.com"

  "Auth" should "create a correct token request" in {
    val tokenrequest = Auth.tokenRequest(email, pk)
    tokenrequest.entity.contentType should be (ContentType(`application/x-www-form-urlencoded`, HttpCharsets.`UTF-8`))
  }
  it should "be able to send request to Google" in { 
    val tokenresp = Auth.getToken(email, pk)
    val ready = Await.ready(tokenresp, 10.seconds)
    ready.futureValue should be (ErrorMessage("invalid_client",Some("The OAuth client was not found.")))
  }
}
