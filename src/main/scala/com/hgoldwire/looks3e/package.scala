package com.hgoldwire

import com.hgoldwire.looks3e.model.{Bucket, S3Item}

package object looks3e {
  type BucketAndQueue = (Bucket, Queue[S3Item])
}
