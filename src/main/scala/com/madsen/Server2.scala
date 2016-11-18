package com.madsen

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http
import com.twitter.finagle.http.ParamMap
import com.twitter.util.{Await, Future}

object Server extends App {
  val service = new EveryWebAppInTheUniverse
  val server = Http.serve(":8080", service)
  Await.ready(server)
}

class EveryWebAppInTheUniverse extends Service[http.Request, http.Response] {

  private val redis: Redis = new FakeRedis
  private val externalService: ExternalService = new FakeService

  def apply(req: http.Request): Future[http.Response] = {

    businessLogic(req.params) map { answer =>
      val r = http.Response(http.Status.Ok)
      r.contentString = answer
      r
    }
  }

  private def businessLogic(params: ParamMap): Future[String] = {

    requiredParams(params) map { case (size, name) =>
      fetchAnswer(size, name)
    } getOrElse Future.exception(new Exception("bad params"))
  }

  private def fetchAnswer(size: Int, name: String): Future[String] = {
    // Combining multiple layers of async by flatmap
    val redisResult: Future[String] = redis.get(name)

    redisResult flatMap { value =>
      val serviceResult: Future[Int] = externalService.calculateMagicNumber(size)
      serviceResult map { m =>
        s"Stored value: $value; Magic number: $m"
      }
    }
  }

  // Still flatMap, but more terse using for comprehension
  // (akin to Haskell do comprehension)
  private def fetchAnswer2(size: Int, name: String): Future[String] = {
    for {
        r <- redis.get(name)
        m <- externalService.calculateMagicNumber(size)
    } yield s"Stored value: $r; Magic number: $m"
  }


  private def requiredParams(params: ParamMap): Option[(Int, String)] = {
    for {
      size <- params.get("size")
      name <- params.get("name")
    } yield (size.toInt, name)
  }
}

trait Redis {
  def get(key: String): Future[String]
}

trait ExternalService {
  def calculateMagicNumber(seed: Int): Future[Int]
}

class FakeRedis extends Redis {
  def get(key: String): Future[String] = Future.value("yo")

}

class FakeService extends ExternalService {
  def calculateMagicNumber(seed: Int): Future[Int] = Future.value(seed + 42)
}
