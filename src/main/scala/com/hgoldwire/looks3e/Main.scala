package com.hgoldwire.looks3e

import cats.effect.IO
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import org.http4s.HttpService
import org.http4s.dsl.impl.Root
import org.http4s.dsl.io._
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global


object Main extends StreamApp[IO] {

  val helloWorldService = HttpService[IO] {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name.")
  }

  val bucketService = HttpService[IO] {
    case GET -> Root / "add" / bucket =>
      Ok(s"adding $bucket")

    case GET -> Root / "search" / bucket =>
      Ok(s"searching $bucket")

    case GET -> Root / "all" =>
      Ok(SimpleDBUploader.search.toString)
  }

  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
    BlazeBuilder[IO]
      .bindHttp(8080, "localhost")
      .mountService(helloWorldService, "/")
      .mountService(bucketService)
      .serve

  val baseDomain = "looks3e"

  val simpleDbThreads = 1
  val simpleDbBatchSize = 25

  val s3Threads = 4

  //  val buckets: Queue[Bucket] = Queue(S3BucketRecursor.listBuckets)
  val buckets = S3BucketRecursor.listBuckets

  //  val s3 = S3BucketRecursor(buckets.find(_.name == "hgoldwire").get)
  //  val sdb = SimpleDBUploader("looks3e-hgoldwire", s3.queue, 25)
  //
  //  val t1 = ExecutionContext.global.execute(new Runnable {
  //    override def run(): Unit = s3.start
  //  })
  //
  //  sdb.start


}
