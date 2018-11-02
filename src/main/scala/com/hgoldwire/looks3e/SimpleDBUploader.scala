package com.hgoldwire.looks3e

import com.amazonaws.services.simpledb.model._
import com.amazonaws.services.simpledb.{AmazonSimpleDB, AmazonSimpleDBClientBuilder}
import com.hgoldwire.looks3e.model.S3Item

import scala.collection.JavaConverters._

object SimpleDBUploader {

  def createDomain(domain: String)(implicit sdb: AmazonSimpleDB): CreateDomainResult = {
    val req = new CreateDomainRequest(domain)
    val res = sdb.createDomain(req)
    println(s"created SDB domain $domain")
    res
  }

  private def itemAddtributes(i: model.S3Item) = {
    Seq(
      new ReplaceableAttribute("bucket", i.bucketName, true),
      new ReplaceableAttribute("etag", i.etag, true),
      new ReplaceableAttribute("lastModified", i.lastModified.toInstant.toEpochMilli.toString, true),
      new ReplaceableAttribute("size", i.size.toString, true),
      new ReplaceableAttribute("storageClass", i.storageClass.toString, true)
    )
  }

  def replacableItem(item: model.S3Item): ReplaceableItem = {
    val ras = itemAddtributes(item)
    new ReplaceableItem(item.key, itemAddtributes(item).asJava)
  }


}

case class SimpleDBUploader(domain: String, queue: Queue[S3Item], batchSize: Int) {

  implicit val sdb: AmazonSimpleDB = AmazonSimpleDBClientBuilder.defaultClient()

  DynamoDBUploader.createDomain(domain)

  def start = {
    println(s"$this starting")
    while (true) {
      val items = queue.take(batchSize)
      val ris = items.map(DynamoDBUploader.replacableItem)
      val req = new BatchPutAttributesRequest(domain, ris.asJava)
      val res = sdb.batchPutAttributes(req)
    }
  }

}
