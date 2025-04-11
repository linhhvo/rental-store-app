package logic.enums

import upickle.default.*

/** keep track of item status Active - item is visible for user and available for booking Inactive -
  * item is not visible for user and only available for reporting
  */
enum ItemStatus derives ReadWriter:
   case Active
   case Inactive