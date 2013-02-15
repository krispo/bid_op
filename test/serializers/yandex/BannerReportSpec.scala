package serializers.yandex

import org.specs2.mutable._
import org.specs2.specification._
import play.api.mvc._
import play.api._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import io.Source
import dao.squerylorm.test.helpers._
import org.codehaus.jackson.JsonFactory
import org.codehaus.jackson.JsonParser.Feature.ALLOW_COMMENTS
import org.codehaus.jackson.map.ObjectMapper

class BannerReportSpec extends Specification with AllExpectations {

  "apply(jsonString)" should {
    sequential

    "create BannerReport" in {
      // get test data
      /*val jf = new JsonFactory()
      jf.enable(ALLOW_COMMENTS)
      val objectMapper = new ObjectMapper()
      objectMapper.configure(ALLOW_COMMENTS, true)*/

      val file_name = "test/serializers/yandex/reports/bannerReport1.json"
      val js = Source.fromFile(file_name, "utf-8").getLines.mkString
      val breport = BannerReport._apply(Json.parse(js))
      breport.data.length must_== (1)
      val bInfo = breport.data(0)
      bInfo.BannerID must_== (11)
      bInfo.Text must_== ("some")
      // Phrases
      bInfo.Phrases.length must_== (1)
      val phrase = bInfo.Phrases(0)
      phrase.Min must_== (1.1)
      phrase.AutoBroker must_== ("Yes")
      phrase.CurrentOnSearch must_== (1.0)
    }
  }

  "createRegions(jsonString)" should {
    sequential
    "create 2 domain.Regions" in {
      // get test data
      val file_name = "test/serializers/yandex/reports/bannerReport1.json"
      val js = Source.fromFile(file_name, "utf-8").getLines.mkString
      val breport = BannerReport._apply(Json.parse(js))
      val regions = breport.data(0).createRegions()
      regions.length must_== (2)
      regions(0).network_region_id must_== ("12")
    }

    "create 1 domain.Region(0) if Geo isEmty" in {
      // get test data
      val file_name = "test/serializers/yandex/reports/bannerReport1.json"
      val js = Source.fromFile(file_name, "utf-8").getLines.mkString
      val breport = BannerReport._apply(Json.parse(js))
      val regions = breport.data(0).createRegions("")
      regions.length must_== (1)
      regions(0).network_region_id must_== ("0")
    }

  }

}
