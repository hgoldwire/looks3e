import java.time.Instant
import java.util.Date

import com.hgoldwire.looks3e.model.Item
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import com.hgoldwire.looks3e.model.Json._
import org.scalatest.{EitherValues, Matchers, WordSpec}

class ItemToJason extends WordSpec with Matchers with EitherValues {
  "an S3 Item" should {
    "serialize and deserialize as JSNON" in {
      val item = Item("hgoldwire", "etag", "/some/path", Date.from(Instant.now), None, 500)
      val jsonString = item.asJson.noSpaces
      decode[Item](jsonString).right.value should be(item)
    }
  }
}
