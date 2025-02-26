import ItemCategory.Camping
import upickle.default.*

import scala.collection.mutable.Set

@upickle.implicits.serializeDefaults(true)
case class Item ( name         : String,
                  desc         : String,
                  ownedQuantity: Int,
                  category     : ItemCategory,
                  ratePerHour  : Double,
                  itemId       : String = "item1",
                  incidents    : Set[Incident] = Set.empty,
                  dependencies : Set[Item] = Set.empty,
                  images       : Set[String] = Set.empty,
                  comments     : Set[String] = Set.empty )
   derives ReadWriter:

   private var availableQuantity = ownedQuantity

   def addComment ( newComment: String ) =
      comments += newComment

   def reportIncident ( newIncident: Incident ) =
      incidents += newIncident
      if newIncident.incidentType == IncidentType.Loss
      then this.makeUnavailable()

   def addDependency ( childItem: Item ) =
      dependencies += childItem

   def isAvailable: Boolean =
      availableQuantity > 0

   //   decrease quantity when item is rented out or reported loss
   def makeUnavailable ( ) =
      availableQuantity -= 1

   //   increase quantity when item is returned
   def freeItem ( ) =
      availableQuantity += 1

   def saveItemAsJson ( ): String =
      upickle.default.write(this, indent = 2)

