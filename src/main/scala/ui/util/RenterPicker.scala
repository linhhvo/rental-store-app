package ui.util

import logic.Renter
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{ChoiceBox, Label}
import scalafx.scene.layout.VBox
import scalafx.scene.text.FontWeight.Bold
import scalafx.scene.text.{Font, FontWeight}

import scala.collection.mutable.Buffer

/** Create a group of components to select renter from a dropdown list */
class RenterPicker( val renterData: Buffer[Renter] ):
   private val label = new Label:
      text = "Select renter"
      font = Font( null, FontWeight( Bold ), 14 )

   // get list of renters
   private val options = ObservableBuffer.empty[String]
   options ++= renterData.map( r => r.name )

   // dropdown menu to select renter
   val picker = new ChoiceBox[String]:
      items = options
      prefWidth = 125

   // group renter selection with label
   val container = new VBox:
      children = Array( label, picker )
      spacing = 5