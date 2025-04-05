package logic

import upickle.default.*

/** keep track of saved item's status Pending - Item added to cart but has not been confirmed for
  * booking Booked - Item has been rented out but has not been returned Returend - Item has been
  * returned
  */
enum SavedItemStatus derives ReadWriter:
   case Pending
   case Booked
   case Returned