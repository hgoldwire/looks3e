package com.hgoldwire.looks3e


object Main extends App {

  val allItems = for {
    bucket <- S3.listBuckets
    _ = println(s"about to recurse $bucket")
    objs <- S3.recurseBucket(bucket)
  } yield objs

  println(s"saw ${allItems.size} items")


}
