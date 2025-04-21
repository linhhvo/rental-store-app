package ui.util

import logic.util.{InvalidInput, MissingData}

object Validator:
   /** Check that input values are valid numbers in number fields */
   def validateNumInput( input: String, fieldName: String ): Double =
      input match
         case input if input.isEmpty => throw MissingData( s"$fieldName can't be empty" )
         case input if input.toDoubleOption.isEmpty =>
            throw InvalidInput( s"$fieldName only accepts number input" )
         case input if input.toDouble <= 0          =>
            throw InvalidInput( s"$fieldName cannot be zero or negative" )
         case _                                     => input.toDouble

   /** Check that short text input (i.e. name, item category) is not over limit */
   def validateShortTextInput( input: String, fieldName: String ): String =
      if input.trim.isEmpty then throw InvalidInput( s" $fieldName cannot be empty" )
      else if input.length > 50 then throw InvalidInput( s"$fieldName cannot exceed 50 characters" )
      else input.trim

   /** Check that long text input (i.e. description, comment) is not over limit */
   def validateLongTextInput( input: String, fieldName: String ): String =
      if input.trim.isEmpty then throw InvalidInput( s" $fieldName cannot be empty" )
      else if input.length > 3000 then
         throw InvalidInput( s"$fieldName cannot exceed 3000 characters" )
      else input.trim