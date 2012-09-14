package controllers

import play.api._
import play.api.mvc._
import models._

import play.api.data._
import play.api.data.Forms._
import play.api.data.format._

import com.mongodb.casbah.Imports._

object Posts extends Controller {

  val postForm = Form(
    mapping(
      "title" -> nonEmptyText,
      "body" -> nonEmptyText
    )
      ((title: String, body: String) => Post(title = title, body = body))
      ((post: Post) => Some(post.title, post.body))
  )

  val commentForm = Form(
    mapping(
      "body" -> nonEmptyText
    )
      ((body: String) => Comment(body = body))
      ((comment: Comment) => Some(comment.body))
  )

  def index = Action { implicit request =>
    val posts = Post.findAll.toList
    Ok(views.html.Posts.index(posts))
  }

  def show(id: String) = Action { implicit request =>
    Post.findOneById(id) match {
      case Some(p: Post) => Ok(views.html.Posts.show(p, commentForm))
      case _ => Redirect(routes.Posts.index)
    }
  }

  def newInput = Action { implicit request =>
    Ok(views.html.Posts.newInput(postForm))
  }

  def create = Action { implicit request =>
    postForm.bindFromRequest.fold(
        errors => BadRequest(views.html.Posts.newInput(errors)),
        post => {
          Post.insert(post) match {
            case Some(p: Post) => Redirect(routes.Posts.show(p.id.toString))
            case _ => Redirect(routes.Posts.index)
          }
        }
    )
  }

  def addComment(id: String) = Action { implicit request =>
     Post.findOneById(id) match {
      case Some(p: Post) => {
        commentForm.bindFromRequest.fold(
           errors => BadRequest(views.html.Posts.show(p, errors)),
           comment => {
             val comments = p.comments :+ comment
             val toUpdate = p.copy(comments = comments)
             Post.update(MongoDBObject("_id" -> p.id), toUpdate, false, false, new WriteConcern)
             Redirect(routes.Posts.show(p.id.toString))           
           }
        )
      }
      case _ => Redirect(routes.Posts.index)
    }
  }
}
