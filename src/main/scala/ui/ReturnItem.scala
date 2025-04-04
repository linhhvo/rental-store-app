package ui

import logic.RentalStore.bookings
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.{ChoiceBox, Label, ScrollPane}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.text.FontWeight.Bold
import scalafx.scene.text.{Font, FontWeight}
import ui.Helper.createRenterPicker

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ReturnItem:
   // define date conversion format
   private val dateFormatter = DateTimeFormatter.ofPattern( "dd-MM-yyyy HH:mm" )

   // renter selection
   private val renterPicker = createRenterPicker

   // booking items list container
   private val itemList = new VBox()

   // booking selection
   private val pickerLabel = Label( "Select booking" )
   pickerLabel.font = Font( null, FontWeight( Bold ), 14 )

   // get list of bookings
   private val bookingList = new ObservableBuffer[String]()

   // update booking list as renter selection changes
   renterPicker._1
      .valueProperty()
      .addListener( ( observable, oldValue, newValue ) =>
         // empty booking list
         bookingList.clear()
         // if selected renter is not empty, update booking list
         if newValue != null then
            bookingList ++= bookings
               .filter( _.renter == newValue )
               .map( b =>
                  b.bookingStartTime.format( dateFormatter ) + " to " + b.bookingEndTime
                     .format( dateFormatter )
               )
      )

   // dropdown menu to select booking
   private val bookingPicker = ChoiceBox( bookingList )
   bookingPicker.prefWidth = 270

   // generate list of items in the booking
   bookingPicker
      .valueProperty()
      .addListener( ( observable, oldValue, newValue ) =>
         if newValue != null then
            findBooking( newValue ) match
               case Some( booking ) =>
                  itemList.children = booking.getItems.map( i => Label( i.name ) )
               case _ => itemList.children = Array( Label( "No item to return in this booking" ) )
         else itemList.children.clear()
      )

   // group booking selection with label
   private val bookingContainer = new VBox:
      children = Array( pickerLabel, bookingPicker )
      spacing = 5

   private val filterContainer = HBox( renterPicker._2, bookingContainer )
   filterContainer.spacing = 20

   private val returnItemPageContainer = VBox( filterContainer, itemList )
   returnItemPageContainer.padding = Insets( 20 )
   returnItemPageContainer.spacing = 10

   val returnItemScrollContainer = ScrollPane()
   returnItemScrollContainer.setContent( returnItemPageContainer )
   returnItemScrollContainer.setVisible( false )

   def resetReturnItemPage() =
      renterPicker._1.getSelectionModel.clearSelection()
      bookingPicker.getSelectionModel.clearSelection()

   private def findBooking( bookingPeriodStr: String ) =
      val period = bookingPeriodStr.split( " to " )
      val start = LocalDateTime.parse( period.head, dateFormatter )
      val end = LocalDateTime.parse( period.last, dateFormatter )

      bookings.find( b => b.bookingStartTime == start && b.bookingEndTime == end )