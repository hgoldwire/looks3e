package com.hgoldwire.looks3e

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.{ListObjectsV2Request, ListObjectsV2Result, S3ObjectSummary}
import com.hgoldwire.looks3e.model.{Bucket, Item}

import scala.collection.JavaConverters._
import scala.collection.mutable


object S3 {

  val s3 = AmazonS3ClientBuilder.defaultClient

  def objectSummaryToItem(o: S3ObjectSummary): Item = {
    Item(o.getBucketName, o.getETag, o.getKey, o.getLastModified, Option(o.getOwner).map(_.getId), o.getSize)
  }

  def listBuckets: mutable.Buffer[Bucket] = {
    val buckets = s3.listBuckets().asScala.map(b => Bucket(b.getName, b.getCreationDate))
    println(s"listBuckets saw $buckets")
    buckets
  }

  def recurseBucket(bucket: Bucket): Seq[Item] = {

    def go(request: ListObjectsV2Request, accum: Seq[Item] = Seq.empty): Seq[Item] = {
      val result: ListObjectsV2Result = s3.listObjectsV2(request)
      val summaries = result.getObjectSummaries.asScala
      val items = summaries.map(objectSummaryToItem)

      if (result.isTruncated) {
        println(s"bucket ${bucket.name} saw ${items.size} items, continuing with ${result.getNextContinuationToken}")
        go(request.withContinuationToken(result.getNextContinuationToken), accum ++ items)
      }
      else
        accum ++ items
    }

    val initialRequest = new ListObjectsV2Request()
      .withBucketName(bucket.name)
    go(initialRequest)
  }
}
