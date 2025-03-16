package logic

import upickle.default.*

import scala.collection.mutable.SortedSet

case class Renter( name: String, email: String, comments: SortedSet[String] = SortedSet.empty )
    derives ReadWriter