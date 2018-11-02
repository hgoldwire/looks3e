package com.hgoldwire.looks3e.model

import java.util.Date

import com.amazonaws.services.s3.model.StorageClass

case class S3Item(bucketName: String, etag: String, key: String, lastModified: Date, ownerId: Option[String], size: Long, storageClass: StorageClass)
