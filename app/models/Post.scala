package models

import play.api.Play.current
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import mongoContext._

case class Post(
  @Key("_id")id: ObjectId = new ObjectId,
  title: String,
  body: String
)

object Post extends ModelCompanion[Post, ObjectId]{
  val dao = new SalatDAO[Post, ObjectId](collection = mongoCollection("posts")) {}
  
  def findOneById(id: String): Option[Post] = dao.findOneById(new ObjectId(id))
  
}
