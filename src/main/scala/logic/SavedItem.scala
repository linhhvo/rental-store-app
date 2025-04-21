package logic

import logic.RentalStore.inventory
import logic.enums.SavedItemStatus
import logic.enums.SavedItemStatus.*
import upickle.default.*

import scala.collection.mutable.Buffer

@upickle.implicits.serializeDefaults( true )
case class SavedItem( itemId:              String,
                      var rentQuantity:    Int,
                      var savedItemStatus: SavedItemStatus = Pending
) derives ReadWriter:

   /** Get the item instance of this saved item */
   def getDetailsFromItemList( allItemsList: Buffer[Item] = inventory ): Option[Item] =
      allItemsList.find( _.itemId == this.itemId )

   def confirmBooking() =
      this.savedItemStatus = Booked

   def returnFromBooking() =
      this.savedItemStatus = Returned

end SavedItem