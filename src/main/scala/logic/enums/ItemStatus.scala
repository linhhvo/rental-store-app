package logic.enums

import upickle.default.*

/** Keep track of item status <br>
  * Active - item is visible for user and available for booking <br>
  * Inactive - item is not visible for user and only available for reporting
  */
enum ItemStatus derives ReadWriter:
   case Active
   case Inactive