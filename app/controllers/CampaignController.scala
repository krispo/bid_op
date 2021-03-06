package controllers

import play.api._
import play.api.mvc._
import org.joda.time._
import json_api.Convert._
import play.api.libs.json._
import domain.{ User, Campaign, Network }
import dao.squerylorm.{ SquerylDao, Charts }
import serializers._
import serializers.yandex._
import org.jboss.netty.handler.timeout.IdleStateHandler
import dao.squerylorm.Phrase

object CampaignController extends Controller with Secured {

  /**
   * get all Campaigns for User and Network
   */
  // TODO: add links for campaigns and post new Campaign
  // GET /user/:user/net/:net/camp
  def campaigns(user_name: String, net_name: String) = IsAuth(
    user_name,
    (dao, user) => implicit request => {
      dao.getCampaigns(user_name, net_name) match {
        case Nil => NotFound("CAMPAIGNS are NOT FOUND...")
        case campaigns =>
          val sCampaigns = campaigns map { c =>
            c.historyStartDate = c.startDate
            serializers.Campaign._apply(c)
          }
          Ok(toJson[List[serializers.Campaign]](sCampaigns)) as (JSON)
      }
    })

  /**
   * get Campaign for User, Network and network_campaign_id
   * GET /user/:user/net/:net/camp/:id
   */
  def campaign(user_name: String, net_name: String, network_campaign_id: String) = IsAuth(
    user_name,
    (dao, user) => implicit request => {
      dao.getCampaign(user_name, net_name, network_campaign_id) match {
        case None => NotFound("CAMPAIGN is NOT FOUND...")
        case Some(c) =>
          c.historyStartDate = c.startDate
          val sCampaign = serializers.Campaign._apply(c)
          Ok(toJson[List[serializers.Campaign]](List(sCampaign))) as (JSON)
      }
    })

  /**
   * POST Campaign for User, Network - creates new Campaign
   * @param user_name: String, net_name: String
   * @return play.api.mvc.Result
   * @through Exception if request json body has no valid representation of Campaign
   * POST /user/:user/net/:net/camp
   */
  def createCampaign(user_name: String, net_name: String) = IsAuth(
    user_name,
    (dao, user) => request => {
      //TODO: optimize for one DB select
      dao.getNetwork(net_name) match {
        case None => NotFound("network not found")
        case network => {
          request.body.asJson match {
            case None => BadRequest("Invalid json body")
            case Some(jbody) =>
              try {
                //Create Campaign
                fromJson[serializers.Campaign](jbody) map { c =>
                  c.user = Some(user)
                  c.network = network
                  // insert Campaign
                  val domCamp = dao.create(c)

                  // respond with CREATED header and Campaign body
                  Created(toJson[serializers.Campaign](serializers.Campaign._apply(domCamp))) as (JSON)
                } getOrElse BadRequest
              } catch {
                case e: Throwable =>
                  println(e) //TODO: change to log
                  BadRequest("exception caught: " + e)
              }
          }
        }
      }
    })

  /**
   * recieve stats in the END of the day!
   * POST detailed performance report (BannerPhraseStats) for User, Network and network_campaign_id
   * @param user_name: String, net_name: String, network_campaign_id: String
   * @return play.api.mvc.Result
   * @through Exception if request json body has no valid representation of BannerPhraseStats
   * POST /user/:user/net/:net/camp
   */
  def createXmlReport(user_name: String, net_name: String, network_campaign_id: String) = IsAuth(
    user_name,
    (dao, user) => request => {
      //select Campaign
      dao.getCampaign(user_name, net_name, network_campaign_id) match {
        case None => NotFound("""Can't find Campaign for given User: %s, Network: %s,
          network_campaign_id: %s""".format(user_name, net_name, network_campaign_id))
        case Some(c) =>
          request.body.asXml match {
            case None => BadRequest("Invalid xml body")
            case Some(body_node) =>
              try {
                // TODO: ReportHelper has to be chosen dynamically
                val report = (new XmlReport(body_node)).createBannerPhrasePerformanceReport

                //save report in DB
                dao.createBannerPhrasesPerformanceReport(c, report, true) match {
                  case true =>
                    println("!!! CREATED XmlReport !!! cID=" + c.network_campaign_id)
                    Created("Report has been created")
                  case false => BadRequest("Report has NOT been created. Post it agaign if you sure that Report content is OK")
                }
              } catch {
                case e: Throwable =>
                  //e.printStackTrace
                  println(e) //TODO: change to log
                  BadRequest("Invalid xml. Error caught: " + e)
              }
          }
      }
    })

  /**
   * recieve Banners Stats DURING the day! CREATE INITIAL PERMUTATION, CURVE, RECOMMENDATION,...
   * for - GetStats - BUTTON
   * POST Banners Performance for User, Network and network_campaign_id
   * @param user_name: String, net_name: String, network_campaign_id: String
   * @return play.api.mvc.Result
   * @through Exception if request json body has no valid representation of TimeSlot
   * POST /user/:user/net/:net/camp/:id/bannersstats
   */
  def createBannersPerformance(user_name: String, net_name: String, network_campaign_id: String) = IsAuthJSON(
    user_name,
    (dao, user) => request => {
      //select Campaign
      dao.getCampaign(user_name, net_name, network_campaign_id) match {
        case None => NotFound("""Can't find Campaign for given User: %s, Network: %s,
          network_campaign_id: %s""".format(user_name, net_name, network_campaign_id))
        case Some(c) =>
          val jbody = request.body
          /*request.body.asJson match {
            case None => BadRequest("Invalid json body")
            case Some(jbody) =>*/
          try {
            val cur_dt = request.headers.get("current_datetime") map { dt =>
              format.ISODateTimeFormat.dateTime().parseDateTime(dt)
            } getOrElse (new DateTime())
            val periodType = dao.getPeriodType(cur_dt)

            val direct = fromJson[serializers.GetBannersStatResponse](jbody \ "direct")
            val metrika = fromJson[List[serializers.PerformanceMetrika]](jbody \ "metrika")
              .map {
                _ match {
                  case Nil => Nil
                  case pml => pml
                }
              }
              .getOrElse(Nil)

            metrika match {
              case Nil =>
                println("!!! EMPTY Banners PERFORMANCE - METRIKA !!! cID=" + c.network_campaign_id)
              case pml =>
                val report = serializers
                  .BannersPerformanceMetrika
                  .createBannerPhrasePerformanceMetrikaReport(pml, c, cur_dt)

                dao.createBannerPhrasesPerformanceMetrikaReport(c, report) match {
                  case true => println("!!! CREATED Banners PERFORMANCE - METRIKA !!! cID=" + c.network_campaign_id)
                  case false => println("??? Failed... Banners PERFORMANCE - METRIKA ??? cID=" + c.network_campaign_id)
                }
            }

            direct map { bsr =>
              println("<<< raw:" + bsr.Stat.length + " >>>")
              val report = serializers.BannersPerformance.createBannerPhrasePerformanceReport(bsr, metrika, c, cur_dt)
              println("<<< " + report.toList.length + " >>>")
              //save report in DB
              dao.createBannerPhrasesPerformanceReport(c, report) match {
                case true =>
                  println("!!! CREATED Banners PERFORMANCE - DIRECT !!! cID=" + c.network_campaign_id)
                  Created("Report has been created")
                case false =>
                  println("??? Failed... Banners PERFORMANCE - DIRECT ??? cID=" + c.network_campaign_id)
                  BadRequest("Report has NOT been created. Post it agaign if you sure that Report content is OK")
              }
            } getOrElse BadRequest("Report has NOT been serialized. Post it agaign if you sure that Report content is OK")
          } catch {
            case e: Throwable =>
              e.printStackTrace //TODO: change to log
              BadRequest("exception caught: " + e)
          }
      }
      //}
    })

  /**
   * recieve stats DURING the day! CREATE INITIAL PERMUTATION, CURVE, RECOMMENDATION,...
   * for - GetStats - BUTTON
   * POST Campaign Performance for User, Network and network_campaign_id
   * @param user_name: String, net_name: String, network_campaign_id: String
   * @return play.api.mvc.Result
   * @through Exception if request json body has no valid representation of TimeSlot
   * POST /user/:user/net/:net/camp/:id/stats
   */
  def createCampaignPerformance(user_name: String, net_name: String, network_campaign_id: String) = IsAuth(
    user_name,
    (dao, user) => request => {
      //select Campaign
      dao.getCampaign(user_name, net_name, network_campaign_id) match {
        case None => NotFound("""Can't find Campaign for given User: %s, Network: %s,
          network_campaign_id: %s""".format(user_name, net_name, network_campaign_id))
        case Some(c) =>
          request.body.asJson match {
            case None => BadRequest("Invalid json body")
            case Some(jbody) =>
              try {
                //println(">>>>>" + jbody + "<<<<<")
                val direct = fromJson[serializers.Performance](jbody \ "direct")
                val metrika = fromJson[List[serializers.PerformanceMetrika]](jbody \ "metrika")
                  .map {
                    _ match {
                      case Nil => Nil
                      case pml => pml
                    }
                  }
                  .getOrElse(Nil)

                val cur_dt = direct.map(_.dateTime).getOrElse(new DateTime())
                val periodType = dao.getPeriodType(cur_dt)

                //println(">>>>>" + metrika + "<<<<<")
                metrika match {
                  case Nil =>
                    println("!!! EMPTY Campaign PERFORMANCE - METRIKA !!! cID=" + c.network_campaign_id)
                  case pml =>
                    /* it needs Dao to find out the PeriodType */
                    val perfList = pml.head.createPerformanceMetrikaWithGoals(cur_dt, periodType)

                    println("!!! CREATED Campaign PERFORMANCE - METRIKA !!! cID=" + c.network_campaign_id)
                    dao.createCampaignPerformanceMetrikaReport(c, perfList)
                }

                val perfDirectOpt = direct.map { performance =>
                  /* it needs Dao to find out the PeriodType */
                  performance.periodType = periodType
                  performance.updateWithMetrikaPerformance(metrika.headOption.map(_.statWithoutGoals))

                  // insert Performance
                  // TODO: no PeriodType now. Fix.
                  println("!!! CREATED Campaign PERFORMANCE - DIRECT !!! cID=" + c.network_campaign_id)
                  dao.createCampaignPerformanceReport(c, performance)
                }

                perfDirectOpt map { perfDirect =>
                  // respond with CREATED header and Performance body
                  Created(toJson[serializers.Performance](serializers.Performance._apply(perfDirect))) as (JSON)
                } getOrElse BadRequest

              } catch {
                case e: Throwable =>
                  e.printStackTrace //TODO: change to log
                  BadRequest("exception caught: " + e)
              }
          }
      }
    })

  def runOptimizerAlgorithm(c: Campaign, performance: serializers.Performance, dao: SquerylDao): Boolean = {
    /** Create Permutation-Recommendation **/
    /*c.historyStartDate = c.startDate
      c.historyEndDate = c.endDate.getOrElse(new DateTime())

      println("<<<<< " + c.permutationHistory.toString + " >>>>>")
      println("<<<<< " + c.curves.toString + " >>>>>")
      if (runOptimizerAlgorithm(c, performance, dao))
         println("CREATED PERMUTATION-RECOMMENDATION!!!!!!!!!!!!!!")
      else
         println("Algorithm is FAILED!!!!!!!!!!!!")*/
    try {
      val PR = c.permutationHistory match {
        case Nil => {
          // create Permutation Map[BannerPhrase, Position]
          val permutation_map = (
            for {
              (b, i) <- c.bannerPhrases.zipWithIndex
            } yield (b, domain.po.Position(i))).toMap

          // create domain.Permutation  (DB Positions is created in Permutation!!!)
          val permutation = new domain.po.Permutation(0, dateTime = performance.dateTime, permutation = permutation_map)

          // create dummy Curve
          val curve = dao.create(
            curve = new domain.po.Curve(0, 1, 1, 1, 1, performance.dateTime, Some(permutation)),
            campaign = c)
          // save Permutation Recommendation and RecommendationChangeDate to DB
          dao.createPermutaionRecommendation(permutation, c, curve)
        }
        case cList => {
          import optimizer._
          val opt = new OptimizerPro
          val bpBid = c.bannerPhrases.map(bp => bp -> 0.01).toMap

          dao.createPermutaionRecommendation(c, bpBid, performance.dateTime)

          //val loc_permutation = opt.createLocalPermutation(c, performance.dateTime)
          //dao.createPermutaionRecommendation(loc_permutation, c, c.curves.head)
        }
      }
      true
    } catch {
      case t: Throwable => false
    }
  }

  /**
   * ActualBids and NetAdvised Bids! CREATE INITIAL BANNERS,PHRASES,BANNERPHRASES,...
   * for - Get ActualBids and NetAdvisedBids - BUTTON
   * POST analogous to getBanners (Live) Yandex report
   * it creates ActualBidHistory and NetAdvisedBidHistory records
   * @param user_name: String, net_name: String, network_campaign_id: String
   * @return play.api.mvc.Result
   * @through Exception if request json body has no valid representation of TimeSlot
   * POST /user/:user/net/:net/camp/:id/bannerreports
   */
  def createBannerReport(user_name: String, net_name: String, network_campaign_id: String) = IsAuthJSON(
    user_name,
    (dao, user) => request => {
      //select Campaign
      dao.getCampaign(user_name, net_name, network_campaign_id) match {
        case None => NotFound("""Can't find Campaign for given User: %s, Network: %s,
          network_campaign_id: %s""".format(user_name, net_name, network_campaign_id))
        case Some(c) =>
          val jbody = request.body
          //          request.body.asJson match {
          //            case None =>
          //              println("??? Failed... ANA - Invalid json body")
          //              BadRequest("Invalid json body")
          //            case Some(jbody) =>
          try {
            //fromJson[List[serializers.yandex.BannerInfo]](jbody) map { bil =>
            jbody.asOpt[List[JsValue]] map { bil =>
              val report = serializers.yandex.BannerReport.getDomainReportJS(bil)
              // save in DB
              dao.createBannerPhraseNetAndActualBidReport(c, report) match {
                case true =>
                  println("!!! CREATED BannerReport ANA !!! cID=" + c.network_campaign_id)
                  Created(Json.toJson(true)) as (JSON) // respond with CREATED header and res: Boolean
                case false =>
                  println("??? Failed... BannerReport ANA ??? cID=" + c.network_campaign_id)
                  Created(Json.toJson(false)) as (JSON)
              }
            } getOrElse BadRequest
          } catch {
            case e: Throwable =>
              println(e) //TODO: change to log
              //e.printStackTrace
              BadRequest("exception caught: " + e)
          }
      }
      //}
    })

  /**
   * get current recommendations
   * Header If-Modified-Since must be set (preferably to the Date recieved with the last Recommendation)
   * if not Modified-Since than response Header is 304 (Not Modified) and body is empty.
   * Otherwise current recommendations in Json sent
   * recommendations are in form [{phrase_id: String, bannerID: String, regionID: String, bid: Double}]
   * GET /user/:user/net/:net/camp/:id/recommendations
   */
  def recommendations(user_name: String, net_name: String, network_campaign_id: String) = IsAuth(
    user_name,
    (dao, user) => request => {
      request.headers.get("If-Modified-Since") match {
        case None => BadRequest("Header: If-Modified-Since: yyyy-MM-dd'T'HH:mm:ss.SSSZZ has to be set")
        case Some(date_str) =>
          // get date from String
          val date: DateTime = format.ISODateTimeFormat.dateTime().parseDateTime(date_str)
          println(date)
          //select Campaign
          //val dao = new SquerylDao
          dao.getCampaign(user_name, net_name, network_campaign_id) match {
            case None => {
              println("??? Not found campaigns...");
              NotFound("""Can't find Campaign for given User: %s, Network: %s,
              network_campaign_id: %s""".format(user_name, net_name, network_campaign_id))
            }
            case Some(c) => {
              println("!!! Found campaigns");
              //check if recommendations has been modified since
              dao.recommendationChangedSince(c, date) match {
                // not changed - 304
                case false =>
                  println("!!! Recommendations are NOT changed !!!")
                  NotModified
                // changed
                case true =>
                  // retrieve recommendations from DB
                  dao.getCurrentRecommedation(c) match {
                    case None => {
                      println("??? NO recommendations found...");
                      NotFound("No Recommendation found")
                    }
                    case Some(rec) => {
                      println("!!! Recommendations are found !!!");
                      Ok(Json.stringify(serializers.Recommendation(c, rec))) as (JSON)
                    }
                  }
              }
            }

          }

      }
    })

  /**
   * Get Phrases for a given user, network, campaign.
   * This information needs for getting statistics of impresses over the month
   */
  def phrases(user_name: String, net_name: String, network_campaign_id: String) = IsAuth(
    user_name,
    (dao, user) => request => {
      dao.getCampaign(user_name, net_name, network_campaign_id) match {
        case None => {
          println("??? Campaign is NOT found...");
          NotFound("""Can't find Campaign for given User: %s, Network: %s,
              network_campaign_id: %s""".format(user_name, net_name, network_campaign_id))
        }
        case Some(c) => {
          val phl = c.bannerPhrases.flatMap(_.phrase)
          val js = Json.toJson(phl.map(_.phrase))
          Ok(Json.stringify(js))
        }
      }
    })

  def phrasesStats(user_name: String, net_name: String) = IsAuthJSON(
    user_name,
    (dao, user) => request => {
      val jbody = request.body
      val phStats = PhrasesStats.getStats(jbody)
      dao.createPhrasesStats(phStats) match {
        case true => Ok("!!! Wordstat report is POSTED")
        case false => InternalServerError("??? Failed... Wordstat report is NOT posted...")
      }
    })

  def runOptimization(user_name: String, net_name: String, network_campaign_id: String) = IsAuth(
    user_name,
    (dao, user) => request => {
      dao.getCampaign(user_name, net_name, network_campaign_id) match {
        case None => {
          println("??? Campaign is NOT found...");
          NotFound("""Can't find Campaign for given User: %s, Network: %s,
              network_campaign_id: %s""".format(user_name, net_name, network_campaign_id))
        }
        case Some(c) => {
          /*
           * RUN OPTIMIZATION
           */
          println("!!! The algorithm have been started, %s - %s !!!".format(c._login, c.network_campaign_id))
          Ok("!!! The algorithm have been started !!!")
        }
      }
    })

  /**
   * generate CHARTS in view for User, Network and network_campaign_id
   * GET /user/:user/net/:net/camp/:id/charts
   */
  def charts(user_name: String, net_name: String, network_campaign_id: String, password: String) =
    Action {
      import scala.concurrent.Future
      import scala.concurrent.ExecutionContext.Implicits.global
      Async {
        Future {
          Ok(views.html.charts(common.Helpers.cform))
        }
      }
    }
}

