package controllers
import javax.inject._
import play.api.mvc._

import scala.io.BufferedSource
import scala.io.Source

import scala.util.matching.Regex

@Singleton
class MovieInfoController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  val movieInfoQueries = Map (
    "image" -> """loadlate="(.*)"""".r,
    "rank" -> """<span class="lister-item-index unbold text-primary">(.*)</span>""".r,
    "title" -> """a href="/title/\w+/"\s*> <img alt="(.*)"""".r,
    "movieYear" -> """<span class="lister-item-year text-muted unbold">.*\((\d+)\)</span>""".r,
    "tvShowYear" -> """muted unbold">\(([\d\–]+)\s*\)""".r,
    "certificate" -> """<span class="certificate\">(\w+)</span>""".r,
    "titleRuntime" -> """<span class="runtime">(.*)</span>""".r,
    "rating" -> """name="ir"\s*data-value="([\d\.]+)">""".r,
    "plot" -> """<p class="text-muted">(\s*.*\s*.*?)(?:</p>|\.\.\.)""".r,
    "directors" -> """>((?:\w+-?\w*\s*)+)</a>""".r,
    "starring" -> """>((?:\w+-?\w*\s*)+)</a>""".r
  )

  def urlGenerator(medium: String, genre: String, toYear: String, fromYear: String): String = {

    "https://www.imdb.com/search/title/" +
      s"?title_type=${if(medium == "Movies") "feature" else "tv_series"}" +
      s"&genre=${if(genre == "All") "" else genre}" +
      s"&release_date=${fromYear}-01-01," +
      s"${toYear}-12-31" +
      "&user_rating=7.5,10.0" +
      "&num_votes=10000," +
      "&sort=user_rating,desc"
  }

  def getGroups(movieAttribute: String, truncatedHtml: String): List[String] = {

    val movieAttributeAndHtmlText: List[String] = movieAttribute match {
      case "directors" => List(movieAttribute, """Directors?:(\s|.)*?<span class""".r.findFirstIn(truncatedHtml).get)
      case "starring" => List(movieAttribute, """Stars:(\s|.)*?</p>""".r.findFirstIn(truncatedHtml).get)
      case _ => List(movieAttribute, truncatedHtml)
    }

    val attribute = movieAttributeAndHtmlText(0)
    val htmlText = movieAttributeAndHtmlText(1)
    val movieInfoRegex: Regex = movieInfoQueries(attribute)

    if(movieInfoRegex.findAllIn(htmlText).nonEmpty) {
      if(attribute == "directors" || attribute == "starring") {
        movieInfoRegex.findAllIn(htmlText).matchData.map(result => result.group(1)).toList
      }
      else movieInfoRegex.findAllIn(htmlText).subgroups

    } else List("")
  }

  def imdbBestMovie(med: Option[String], gen: Option[String], fy: Option[String], ty: Option[String]):
  Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val fromYear = fy.get
    val toYear = ty.get
    val medium = med.get
    val genre = gen.get

    if(fromYear.toInt <= toYear.toInt) {
      val url = urlGenerator(medium, genre, toYear, fromYear)

      val htmlResult: BufferedSource = Source.fromURL(url)
      val htmlResultString: String  = htmlResult.mkString
      htmlResult.close

      val startOfRelevantHtml = htmlResultString.indexOf("Your Rating")
      val endOfRelevantHtml = htmlResultString.indexOf("Recently Viewed")

      val truncatedHtml = htmlResultString.slice(startOfRelevantHtml, endOfRelevantHtml)
      val movieChunks: Array[String] = truncatedHtml.split("""<div class="lister-item-image float-left">""")
      val movieChunksCleaned: Array[String] = movieChunks.drop(1) // the first item contain no useful information so drop it

      val movieInfoMap: Array[Map[String, List[String]]] = movieChunksCleaned.map { truncatedHtml =>
        val year = if(medium == "Movies") "movieYear" else "tvShowYear"
        val movieAttributes: List[String] = List("image", "rank", "title", year, "certificate", "titleRuntime",
                                                  "rating", "plot", "directors", "starring")

        val movieInfo: Seq[(String, List[String])] = movieAttributes.map{ movieAttribute =>
          val attribute = if (movieAttribute.contains("Year")) "year" else movieAttribute
          val movieInfoResults: List[String] = getGroups(movieAttribute, truncatedHtml)
          (attribute, movieInfoResults)
        }

        movieInfo.toMap
      }
      val toYearFormatted = if(fromYear.toInt == toYear.toInt) "" else s" to ${toYear}"

      Ok(views.html.top50(medium, fromYear, toYearFormatted, movieInfoMap))

    }

    else {
      Ok("ERROR")
    }
  }
}
