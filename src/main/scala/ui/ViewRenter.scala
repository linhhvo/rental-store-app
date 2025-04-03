package ui

import logic.Renter.renters
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{ChoiceBox, Label, ScrollPane}
import scalafx.scene.layout.VBox
import scalafx.scene.text.FontWeight.Bold
import scalafx.scene.text.{Font, FontWeight}

object ViewRenter:

   private val pickerLabel = Label( "Select renter" )
   pickerLabel.font = Font( null, FontWeight( Bold ), 14 )

   // get list of renters
   private val renterList = new ObservableBuffer[String]()
   renterList ++= renters.map( r => r.name )

   // dropdown menu to select renter
   private val renterPicker = ChoiceBox( renterList )

   // group renter selection with label
   private val pickerContainer = new VBox:
      children = Array( pickerLabel, renterPicker )
      spacing = 5

   val renterPageContainer = ScrollPane()
   renterPageContainer.setContent( pickerContainer )
   renterPageContainer.setVisible( false )