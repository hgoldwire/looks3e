package com.hgoldwire.looks3e

import scala.concurrent.ExecutionContext


object Main extends App {

  val baseDomain = "looks3e"

  val simpleDbThreads = 1
  val simpleDbBatchSize = 25

  val s3Threads = 4

  //  val buckets: Queue[Bucket] = Queue(S3BucketRecursor.listBuckets)
  val buckets = S3BucketRecursor.listBuckets

  val s3 = S3BucketRecursor(buckets.find(_.name == "hgoldwire").get)
  val sdb = DynamoDBUploader("looks3e-hgoldwire", s3.queue, 25)

  val t1 = ExecutionContext.global.execute(new Runnable {
    override def run(): Unit = s3.start
  })

  sdb.start

}
