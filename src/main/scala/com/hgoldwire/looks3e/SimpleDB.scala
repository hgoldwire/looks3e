package com.hgoldwire.looks3e

import com.amazonaws.services.simpledb.model._
import com.amazonaws.services.simpledb.{AmazonSimpleDB, AmazonSimpleDBClientBuilder}

import scala.collection.JavaConverters._


object SimpleDB {
  val sdb: AmazonSimpleDB = AmazonSimpleDBClientBuilder.defaultClient()

  def createDomain(domain: String): CreateDomainResult = {
    val req = new CreateDomainRequest(domain)
    val res = sdb.createDomain(req)
    println(s"created domain $domain")
    res
  }

  def itemAddtributes(i: model.Item) = {
    Seq(
      new ReplaceableAttribute("bucket", i.bucketName, true),
      new ReplaceableAttribute("etag", i.etag, true),
      new ReplaceableAttribute("lastModified", i.lastModified.toInstant.toEpochMilli.toString, true),
      new ReplaceableAttribute("size", i.size.toString, true)
    )
  }

  def replacableItem(item: model.Item) = {
    val ras = itemAddtributes(item)
    new ReplaceableItem(item.key, itemAddtributes(item).asJava)
  }

  def add(domain: String, items: Seq[model.Item], batchSize: Int = 25) = {
    items.grouped(batchSize).foldLeft(true)((continue, batch) => {
      val ris = batch.map(replacableItem)
      val req = new BatchPutAttributesRequest(domain, ris.asJava)
      val res = sdb.batchPutAttributes(req)
      res != null
    })

  }

}
