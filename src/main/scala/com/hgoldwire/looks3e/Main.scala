package com.hgoldwire.looks3e

import java.util.concurrent.LinkedBlockingQueue

import com.hgoldwire.looks3e.model.{Bucket, S3Item}

import scala.collection.JavaConverters._


object Main extends App {

  type Queue = LinkedBlockingQueue[S3Item]

  val baseDomain = "looks3e"
  val simpleDbThreads = 1
  val simpleDbBatchSize = 25

  val buckets = S3.listBuckets

  val queue: Queue = new Queue()

  // Set up S3 recurser threads and associated queues
  val bqts: Seq[(Bucket, Queue, Thread)] = buckets.map { bucket =>
    val queue: Queue = new Queue()
    val runnable = new Runnable {
      override def run(): Unit = S3.recurseBucket(bucket, items => queue.addAll(items.asJavaCollection))
    }
    val thread = new Thread(runnable)
    (bucket, queue, thread)
  }

  // start S3 recurser threads
  bqts.foreach(_._3.start())

  // create simpledb domains
  buckets.map(b => SimpleDB.createDomain(s"$baseDomain-${b.name}"))

  // start simpledb uploader threads
  val sdb = SimpleDB(baseDomain, bqts.map(_._2), simpleDbThreads, simpleDbBatchSize)


}
