package com.hgoldwire.looks3e

import java.util
import java.util.concurrent.BlockingQueue

import com.amazonaws.services.simpledb.model._
import com.amazonaws.services.simpledb.{AmazonSimpleDB, AmazonSimpleDBClientBuilder}
import com.hgoldwire.looks3e.model.{Bucket, S3Item}

import scala.collection.JavaConverters._

object SimpleDB {

  def createDomain(domain: String)(implicit sdb: AmazonSimpleDB): CreateDomainResult = {
    val req = new CreateDomainRequest(domain)
    val res = sdb.createDomain(req)
    println(s"created domain $domain")
    res
  }

  def itemAddtributes(i: model.S3Item) = {
    Seq(
      new ReplaceableAttribute("bucket", i.bucketName, true),
      new ReplaceableAttribute("etag", i.etag, true),
      new ReplaceableAttribute("lastModified", i.lastModified.toInstant.toEpochMilli.toString, true),
      new ReplaceableAttribute("size", i.size.toString, true),
      new ReplaceableAttribute("storageClass", i.storageClass.toString, true)
    )
  }

  def replacableItem(item: model.S3Item) = {
    val ras = itemAddtributes(item)
    new ReplaceableItem(item.key, itemAddtributes(item).asJava)
  }

  def apply(baseDomain: String, queues: Seq[BlockingQueue[S3Item]], threads: Int, batchSize: Int): Seq[Thread] = queues.map { bq =>
    new Thread (
      SimpleDBRunner(s"$baseDomain-${bq._1.name}", bq._2, batchSize)
    )
  }

}

case class SimpleDBRunner(domain: String, queue: BlockingQueue[S3Item], batchSize: Int) extends Runnable {
  override def run(): Unit = {
    implicit val sdb: AmazonSimpleDB = AmazonSimpleDBClientBuilder.defaultClient()

    println(s"SimpleDBRunner starting")
    SimpleDB.createDomain(domain)

    while (true) {
      val inFlight = new util.ArrayList[S3Item]()
      if (queue.drainTo(inFlight) == 0) {
        println(s"SimpleDB $domain waiting for more items")
        inFlight.add(queue.take())
      } else {
        println(s"SimpleDB $domain drained ${inFlight.size()} items")
      }
      inFlight.asScala.grouped(batchSize).foreach(batch => {
        val ris = batch.map(SimpleDB.replacableItem)
        val req = new BatchPutAttributesRequest(domain, ris.asJava)
        val res = sdb.batchPutAttributes(req)
      })
    }
  }
}
