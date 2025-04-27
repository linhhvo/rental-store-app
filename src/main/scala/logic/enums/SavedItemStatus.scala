package logic.enums

import upickle.default.*

/** Keep track of saved item's status <br>
  * Pending - Item added to cart but has not been confirmed for booking <br>
  * Booked - Item has been rented out but has not been returned <br>
  * Returned - Item has been returned
  */
enum SavedItemStatus derives ReadWriter:
   case Pending
   case Booked
   case Returned