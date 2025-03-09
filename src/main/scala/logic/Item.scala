package logic

import com.aventrix.jnanoid.jnanoid.NanoIdUtils.*
import upickle.default.*

import scala.collection.mutable.{Buffer, Set, SortedSet}

@upickle.implicits.serializeDefaults( true )
case class Item( name:          String,
                 desc:          String,
                 ownedQuantity: Int,
                 category:      String,
                 ratePerHour:   Double,
                 itemId:        String = randomNanoId(),
                 incidents:     Set[Incident] = Set.empty,
                 dependencies:  Set[Item] = Set.empty,
                 var images:    String = "",
                 comments:      SortedSet[String] = SortedSet.empty
) derives ReadWriter:

   private var availableQuantity = ownedQuantity

   images = s"${this.name}.jpg"

   def addComment( newComment: String ) =
      comments += newComment

   def reportIncident( newIncident: Incident ) =
      incidents += newIncident
      if newIncident.incidentType == IncidentType.Loss
      then this.makeUnavailable()

   def addDependency( childItem: Item ) =
      dependencies += childItem

   def isAvailable: Boolean =
      availableQuantity > 0

   //   decrease quantity when item is rented out or reported loss
   def makeUnavailable() =
      availableQuantity -= 1

   //   increase quantity when item is returned
   def freeItem() =
      availableQuantity += 1
