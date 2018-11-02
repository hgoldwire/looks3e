package com.hgoldwire.looks3e

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.{ListObjectsV2Request, ListObjectsV2Result, S3ObjectSummary, StorageClass}
import com.hgoldwire.looks3e.model.{Bucket, S3Item}

import scala.collection.JavaConverters._
import scala.collection.mutable


object S3 {

  val s3 = AmazonS3ClientBuilder.defaultClient

  def objectSummaryToItem(o: S3ObjectSummary): S3Item = {
    S3Item(o.getBucketName, o.getETag, o.getKey, o.getLastModified, Option(o.getOwner).map(_.getId), o.getSize, StorageClass.fromValue(o.getStorageClass))
  }

  def listBuckets: mutable.Buffer[Bucket] = {
    val buckets = s3.listBuckets().asScala.map(b => Bucket(b.getName, b.getCreationDate))
    println(s"listBuckets saw $buckets")
    buckets
  }

  def recurseBucket(bucket: Bucket, cb: Seq[S3Item] => Unit): Seq[S3Item] = {

    def go(request: ListObjectsV2Request, accum: Seq[S3Item] = Seq.empty): Seq[S3Item] = {
      val result: ListObjectsV2Result = s3.listObjectsV2(request)
      val summaries = result.getObjectSummaries.asScala
      val items = summaries.map(objectSummaryToItem)

      cb(items)

      if (result.isTruncated) {
//        println(s"bucket ${bucket.name} saw ${accum.size + items.size} items, continuing with ${result.getNextContinuationToken}")
        go(request.withContinuationToken(result.getNextContinuationToken), accum ++ items)
      }
      else
        accum ++ items
    }

    println(s"S3 recursing $bucket")
    val initialRequest = new ListObjectsV2Request().withBucketName(bucket.name)
    val items = go(initialRequest)
    println(s"$bucket has ${items.size} items")
    items
  }
}
