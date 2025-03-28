package logic

import com.aventrix.jnanoid.jnanoid.NanoIdUtils.*
import upickle.default.*

import scala.collection.mutable.{Set, SortedSet}

@upickle.implicits.serializeDefaults( true )
case class Item( name:               String,
                 desc:               String,
                 ownedQuantity:      Int,
                 category:           String,
                 ratePerHour:        Double,
                 ratePerDay:         Double,
                 ratePerWeek:        Double,
                 itemId:             String = randomNanoId(),
                 incidents:          Set[Incident] = Set.empty,
                 dependencies:       Set[Item] = Set.empty,
                 var images:         String = "",
                 comments:           SortedSet[String] = SortedSet.empty,
                 var lostQuantity:   Int = 0,
                 var rentedQuantity: Int = 0
) derives ReadWriter:

   images = s"${this.name}.jpg"

   def addComment( newComment: String ) =
      comments += newComment

   def reportIncident( newIncident: Incident ) =
      incidents += newIncident
      if newIncident.incidentType == IncidentType.Loss
      then lostQuantity += 1

   def addDependency( childItem: Item ) =
      dependencies += childItem

   def isAvailable: Boolean =
      ownedQuantity - lostQuantity - rentedQuantity > 0

   def rented() =
      rentedQuantity += 1

   //   increase quantity when item is returned
   def free() =
      rentedQuantity -= 1
