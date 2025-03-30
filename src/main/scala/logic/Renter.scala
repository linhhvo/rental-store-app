package logic

import upickle.default.*

import scala.collection.mutable.SortedSet

@upickle.implicits.serializeDefaults( true )
case class Renter( name: String, email: String, comments: SortedSet[String] = SortedSet.empty )
    derives ReadWriter

@main def addRenter() =
   val renter1 = Renter( "renter1", "renter1@email.com" )
   val renter2 = Renter( "renter2", "renter2@email.com" )
   val renter3 = Renter( "renter3", "renter3@email.com" )

   val renters = List( renter1, renter2, renter3 )
   os.write.over( os.pwd / os.RelPath( "src/main/resources/data/renters.json" ),
                  write( renters, indent = 2 )
   )