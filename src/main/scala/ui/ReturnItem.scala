package ui

import logic.RentalStore.bookings
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.{ChoiceBox, Label, ScrollPane}
import scalafx.scene.layout.VBox
import scalafx.scene.text.FontWeight.Bold
import scalafx.scene.text.{Font, FontWeight}
import ui.Helper.createRenterPicker

import java.time.format.DateTimeFormatter

object ReturnItem:
   // renter selection
   private val renterPicker = createRenterPicker

   // booking selection
   val pickerLabel = Label( "Select booking" )
   pickerLabel.font = Font( null, FontWeight( Bold ), 14 )

   // get list of bookings
   val bookingList = new ObservableBuffer[String]()
   bookingList ++= bookings.map( b =>
      b.bookingStartTime.format( DateTimeFormatter.ofPattern( "dd-MM-yyyy HH:mm" ) )
   )

   // dropdown menu to select renter
   val bookingPicker = ChoiceBox( bookingList )
   bookingPicker.prefWidth = 125

   // group renter selection with label
   val bookingContainer = new VBox:
      children = Array( pickerLabel, bookingPicker )
      spacing = 5

   private val returnItemPageContainer = new VBox( renterPicker._2, bookingContainer )
   returnItemPageContainer.padding = Insets( 20 )

   val returnItemScrollContainer = ScrollPane()
   returnItemScrollContainer.setContent( returnItemPageContainer )
   returnItemScrollContainer.setVisible( false )

   def resetRenterPage() =
      renterPicker._1.getSelectionModel.clearSelection()