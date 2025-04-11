package ui.util

import logic.Renter
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{ChoiceBox, Label}
import scalafx.scene.layout.VBox
import scalafx.scene.text.FontWeight.Bold
import scalafx.scene.text.{Font, FontWeight}

import scala.collection.mutable.Buffer

/** create a group of components to select renter from a dropdown list */
class RenterPicker( val renterData: Buffer[Renter] ):
   private val label = Label( "Select renter" )
   label.font = Font( null, FontWeight( Bold ), 14 )

   // get list of renters
   private val options = new ObservableBuffer[String]()
   options ++= renterData.map( r => r.name )

   // dropdown menu to select renter
   val picker = ChoiceBox(options)
   picker.prefWidth = 125

   // group renter selection with label
   val container = new VBox:
      children = Array(label, picker)
      spacing = 5
