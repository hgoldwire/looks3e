package com.hgoldwire.looks3e

import java.util.Date

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.{ListObjectsV2Request, ListObjectsV2Result, S3ObjectSummary}
import com.amazonaws.services.simpledb.model._
import com.amazonaws.services.simpledb.{AmazonSimpleDB, AmazonSimpleDBClientBuilder}

import scala.collection.JavaConverters._

case class Item(bucketName: String, etag: String, key: String, lastModified: Date, ownerId: Option[String], size: Long)


object S3 {

  val s3 = AmazonS3ClientBuilder.defaultClient

  def objectSummaryToItem(o: S3ObjectSummary): Item = {
    Item(o.getBucketName, o.getETag, o.getKey, o.getLastModified, Option(o.getOwner).map(_.getId), o.getSize)
  }

  def recurseBucket(bucketName: String): Seq[Item] = {

    def go(request: ListObjectsV2Request, accum: Seq[Item] = Seq.empty): Seq[Item] = {
      val result: ListObjectsV2Result = s3.listObjectsV2(request)
      val summaries = result.getObjectSummaries.asScala
      val items = summaries.map(objectSummaryToItem)

      if (result.isTruncated) {
        println(s"saw ${items.size} items, continuing with ${result.getNextContinuationToken}")
        go(request.withContinuationToken(result.getNextContinuationToken), accum ++ items)
      }
      else
        accum ++ items
    }

    val initialRequest = new ListObjectsV2Request()
      .withBucketName(bucketName)
    go(initialRequest)
  }
}

object SimpleDB {
  val sdb: AmazonSimpleDB = AmazonSimpleDBClientBuilder.defaultClient()

  def createDomain(domain: String): CreateDomainResult = {
    val req = new CreateDomainRequest(domain)
    val res = sdb.createDomain(req)
    println(s"created domain $domain")
    res
  }

  def itemAddtributes(i: Item) = {
    Seq(
      new ReplaceableAttribute("etag", i.etag, true),
      new ReplaceableAttribute("lastModified", i.lastModified.toInstant.toEpochMilli.toString, true),
      new ReplaceableAttribute("size", i.size.toString, true)
    )
  }

  def replacableItem(item: Item) = {
    val ras = itemAddtributes(item)
    new ReplaceableItem(item.key, itemAddtributes(item).asJava)
  }

  def add(domain: String, items: Seq[Item], batchSize: Int = 25) = {
    items.grouped(batchSize).foldLeft(true)((continue, batch) => {
      val ris = batch.map(replacableItem)
      val req = new BatchPutAttributesRequest(domain, ris.asJava)
      val res = sdb.batchPutAttributes(req)
      res != null
    })

  }

}

object Main extends App {


  val bucketName = "hgoldwire"
  val domain = "looks3e"

  val allItems = S3.recurseBucket(bucketName)
  SimpleDB.createDomain(domain)
  SimpleDB.add(domain, allItems)
  println(s"saw ${allItems.size} items")


}
