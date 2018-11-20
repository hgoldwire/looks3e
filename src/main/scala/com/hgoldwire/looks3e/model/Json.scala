package com.hgoldwire.looks3e.model

import java.time.Instant
import java.util.Date

import io.circe.{Decoder, Encoder}

import scala.util.Try

object Json {
  implicit val encodeDate: Encoder[Date] = Encoder.encodeLong.contramap[Date](_.getTime)
  implicit val decodeDate: Decoder[Date] = Decoder.decodeLong.emapTry[Date](epochMilli => Try {
    Date.from(Instant.ofEpochMilli(epochMilli))
  })
}
