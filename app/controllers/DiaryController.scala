package controllers

import javax.inject._
import play.api.libs.json
import play.api.mvc._
//import io.circe._
//import io.circe.generic.auto._
//import io.circe.parser._
import io.circe.syntax._
import play.api.libs.json._

import scala.io.BufferedSource
//import scalaj.http.Http
//import requests.Request

import scala.io.Source
import scala.util.parsing.json._
import models.Users
import scala.util.matching.Regex


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class DiaryController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */

  //val dayMap: Map[String, String] = List("mon", "tue", "wed", "thu", "fri")



  def date(d: Option[String], m: Option[String], y: Option[String]): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>

    val valuesEntered: Seq[String] = Seq(("Day", d), ("Month", m), ("Year", y)).collect {
      case enteredDate if(!enteredDate._2.contains("--") && enteredDate._2.isDefined) =>
        s"${enteredDate._1} is ${enteredDate._2.get.toLowerCase}"
    }
    //Http("http://foo.com/search").param("q", "monkeys").asString
    val url = "http://jsonplaceholder.typicode.com/users"
    val result: BufferedSource = Source.fromURL(url)
    val resultString = result.mkString

    val testJson: JsValue = Json.parse(resultString)
    //val jp = new JsPath()
    val p: JsLookupResult = testJson.\(0)
    println(p.get("id"))


//    val decodedString: Either[Error, Results] = decode[Results](resultsString)
//    decodedString match {
//      case Left (error) => println(error)
//      case Right (result) => println(result)
//    }



//    val r = requests.get("https://api.github.com/users/lihaoyi")
//
//      r.statusCode
//      // 200
//
//      r.headers("content-type")
//      // Buffer("application/json; charset=utf-8")
//
//      println(r.text)


    if(valuesEntered.size < 2) {
      BadRequest("You have entered too few arguments!!!")
    }
    else{
      Ok(views.html.date(valuesEntered: Seq[String]))
    }

  }

  val movieInfoQueries = Map (
    "image" -> """loadlate="(.*?)"""".r,
    "rank" -> """<span class="lister-item-index unbold text-primary">(.*?)</span>""".r,
    "title" -> """a href="/title/\w+/"\n> <img alt="(.*)"""".r,
    "year" -> """<span class="lister-item-year text-muted unbold">.*\((\d+)\)</span>""".r,
    "certificate" -> """<span class="certificate\">(.*?)</span>""".r,
    "titleRuntime" -> """<span class="runtime">(.*?)</span>""".r,
    "rating" -> """name="ir"((\n\t*)*|\s)data-value="(.*?)>""".r,
    "plot" -> """<p class="text-muted">((\n\t*).*(\n\t*)*.*?)(?:</p>|\.\.\.)""".r,
    "directors" -> """Directors?:\n<a href="/name/\w+/"\n>(.*)</a>(?:,\s\n<a href="/name/\w+/"\n>(.*)</a>)*""".r,
    "starring" -> """Stars:\n<a href="/name/\w+/"\n>(.*)</a>(?:,\s\n<a href="/name/\w+/"\n>(.*)</a>)*""".r
  )

  def imdbBestMovie(med: Option[String], gen: Option[String], fy: Option[String], ty: Option[String]): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    if(fy.get.toInt <= ty.get.toInt) {
      val url = {"https://www.imdb.com/search/title/" +
        s"?title_type=feature" + // feature
        s"&genre=${
          if(gen.get == "All") "" else gen.get

        }" +
        s"&release_date=${fy.get}-01-01," +
        s"${ty.get}-12-31" +
        "&user_rating=7.5,10.0" +
        "&num_votes=10000," +
        "&sort=user_rating,desc"}

      val htmlResult: BufferedSource = Source.fromURL(url)
      val htmlResultString = htmlResult.mkString
      val startOfRelevantHtml = htmlResultString.indexOf("Your Rating")
      val endOfRelevantHtml = htmlResultString.indexOf("Recently Viewed")
      val truncatedHtml = htmlResultString.slice(startOfRelevantHtml, endOfRelevantHtml)
      val movieChunks = truncatedHtml.split("""<div class="lister-item-image float-left">""")
      val movieChunksCleaned = movieChunks.drop(1)

      movieChunksCleaned.foreach {truncatedHtml =>
        println(movieInfoQueries("image").findAllIn(truncatedHtml).subgroups,
          movieInfoQueries("rank").findAllIn(truncatedHtml).subgroups,
          movieInfoQueries("title").findAllIn(truncatedHtml).subgroups,
          movieInfoQueries("year").findAllIn(truncatedHtml).subgroups,
          movieInfoQueries("certificate").findAllIn(truncatedHtml),
          movieInfoQueries("titleRuntime").findAllIn(truncatedHtml).subgroups,
          movieInfoQueries("rating").findAllIn(truncatedHtml).subgroups,
          movieInfoQueries("plot").findAllIn(truncatedHtml).subgroups,
          movieInfoQueries("directors").findAllIn(truncatedHtml).subgroups,
          movieInfoQueries("starring").findAllIn(truncatedHtml).subgroups)
      }

//      val m = movieChunksCleaned.map {truncatedHtml =>
//        List(movieInfoQueries("image").findAllIn(truncatedHtml).subgroups,
//          movieInfoQueries("rank").findAllIn(truncatedHtml).subgroups,
//          movieInfoQueries("title").findAllIn(truncatedHtml).subgroups,
//          movieInfoQueries("year").findAllIn(truncatedHtml).subgroups,
//          movieInfoQueries("certificate").findAllIn(truncatedHtml).subgroups,
//          movieInfoQueries("titleRuntime").findAllIn(truncatedHtml).subgroups,
//          movieInfoQueries("rating").findAllIn(truncatedHtml).subgroups,
//          movieInfoQueries("plot").findAllIn(truncatedHtml).subgroups,
//          movieInfoQueries("directors").findAllIn(truncatedHtml).subgroups,
//          movieInfoQueries("starring").findAllIn(truncatedHtml).subgroups)
//      }
//      m.foreach(x => println(x + "\n\n\n"))

      Redirect(url)
    }
    else {
      Ok("ERROR")
    }


    //      val listOfRows = List()
    //      Ok(views.html.top50(listOfRows: List[List[String]]))

  }


}
