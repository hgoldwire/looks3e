package com.hgoldwire.looks3e

import java.security.MessageDigest

import com.hgoldwire.looks3e.model.Item
import com.sksamuel.elastic4s.bulk.BulkCompatibleRequest
import com.sksamuel.elastic4s.circe._
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.{ElasticClient, ElasticProperties}
import io.circe.generic.auto._
import io.circe.parser.decode
//import com.hgoldwire.looks3e.model.Json._
import com.hgoldwire.looks3e.model.Json._
import scala.io.Source

object Elastic extends App {

  val client = ElasticClient(ElasticProperties("http://localhost:9200"))

  val filename = "/HG/looks3e/src/test/resources/hgoldwire.json"
  val text: String = Source.fromFile(filename).getLines.mkString
  val decodedItems = decode[Seq[Item]](text)

  def md5(s: String) = {
    MessageDigest.getInstance("MD5").digest(s.getBytes).map("%02X".format(_)).mkString
  }

  def makeBulk(i: Item): BulkCompatibleRequest = {
    index("looks3e", "s3item").doc(i) id md5(s"${i.bucketName}-${i.key}")
  }

  client.execute {
//    deleteIndex("looks3e")
    createIndex("looks3e").mappings(
      mapping("s3item")
    )
  }.await

  decodedItems map { items =>
    items.grouped(1000).foreach { group =>
      client.execute(
        bulk(
          group.map(makeBulk)
        )
      ).await
    }
  }

}
