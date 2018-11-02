package com.hgoldwire.looks3e

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.{ListObjectsV2Request, ListObjectsV2Result, S3ObjectSummary, StorageClass}
import com.hgoldwire.looks3e.model.{Bucket, S3Item}

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.mutable


object S3BucketRecursor {
  val s3 = AmazonS3ClientBuilder.defaultClient

  def objectSummaryToItem(o: S3ObjectSummary): S3Item = {
    S3Item(o.getBucketName, o.getETag, o.getKey, o.getLastModified, Option(o.getOwner).map(_.getId), o.getSize, StorageClass.fromValue(o.getStorageClass))
  }

  def listBuckets: Iterable[Bucket] = {
    val buckets = s3.listBuckets().asScala.map(b => Bucket(b.getName, b.getCreationDate))
    println(s"listBuckets saw $buckets")
    buckets
  }

}

case class S3BucketRecursor(bucket: Bucket, queue: Queue[S3Item] = new Queue[S3Item]) {

  val s3 = AmazonS3ClientBuilder.defaultClient

  @tailrec
  private def go(request: ListObjectsV2Request): Queue[S3Item] = {
    val result: ListObjectsV2Result = s3.listObjectsV2(request)
    val summaries = result.getObjectSummaries.asScala
    val items = summaries.map(S3BucketRecursor.objectSummaryToItem)
    queue.add(items)

    if (result.isTruncated) {
      go(request.withContinuationToken(result.getNextContinuationToken))
    }
    else
      queue
  }

  def start = {
    println(s"$this starting")
    val initialRequest = new ListObjectsV2Request().withBucketName(bucket.name)
    go(initialRequest)
    println(s"$this complete")
    queue
  }

}
