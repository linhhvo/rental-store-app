package logic

import com.aventrix.jnanoid.jnanoid.NanoIdUtils.*
import logic.RentalStore.getAllItems
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
                 children:           Set[String] = Set.empty,
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

   def addChildren( childItemId: String ) =
      children += childItemId

   def deleteChildren( childItemId: String ) =
      children -= childItemId

   /** method to retrieve children items details */
   def getChildrenDetails =
      this.children.flatMap( c => getAllItems.filter( i => i.itemId == c ) )

   def availableQuantity: Int =
      ownedQuantity - lostQuantity - rentedQuantity

   def hasEnoughQuantity( requestQuantity: Int ): Boolean =
      this.availableQuantity - requestQuantity >= 0

   def rented() =
      rentedQuantity += 1

   //   increase quantity when item is returned
   def free() =
      rentedQuantity -= 1