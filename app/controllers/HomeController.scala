package controllers

import javax.inject._
import play.api._
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */

//  val days = List("--", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
//  val months = List("--", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
  val years: List[String] = (1920 to 2020).reverse.map{
    year => year.toString
  }.toList

  val genres = List("All", "Action", "Adventure", "Animation", "Biography", "Comedy", "Crime", "Documentary", "Drama", "Family", "Fantasy", "Film-Noir", "Game-Show", "History", "Horror", "Music", "Musical", "Mystery", "News", "Reality-TV", "Romance", "Sci-Fi", "Sport", "Talk-Show", "Thriller", "War", "Western")

//  def index(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
//
//    Ok(views.html.index(days, months, "--" :: years))
//  }


  def imdb(): Action[AnyContent] = Action {
    Ok(views.html.imdbsBest(genres, years))

  }


}
