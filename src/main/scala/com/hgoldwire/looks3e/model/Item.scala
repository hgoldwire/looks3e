package com.hgoldwire.looks3e.model

import java.util.Date

case class Item(bucketName: String, etag: String, key: String, lastModified: Date, ownerId: Option[String], size: Long)
