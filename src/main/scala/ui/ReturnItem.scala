package ui

import logic.Booking
import logic.BookingList.bookings
import logic.Renter.renters
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.*
import scalafx.scene.control.Alert.AlertType.Information
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.text.FontWeight.Bold
import scalafx.scene.text.{Font, FontWeight}
import ui.util.Helper.displayAlert
import ui.util.RenterPicker

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.collection.mutable.Buffer

object ReturnItem:
   // define date conversion format
   private val dateFormatter = DateTimeFormatter.ofPattern( "dd-MM-yyyy HH:mm" )

   // alert to display message to user
   private val msgAlert = new Alert( Information )

   // renter selection
   private val renterPicker = RenterPicker( renters )

   // booking items list container
   private val itemList = new VBox:
      spacing = 5

   // booking selection
   private val pickerLabel = new Label:
      text = "Select booking"
      font = Font( null, FontWeight( Bold ), 14 )

   // get list of bookings
   private val bookingList = ObservableBuffer.empty[String]

   // initialize a bookings list to replace with bookings filtered based on selected renter
   private var filteredBookings = bookings

   // update booking list as renter selection changes
   renterPicker.picker
      .valueProperty()
      .addListener( ( observable, oldValue, newValue ) =>
         // empty booking list
         bookingList.clear()
         returnItemBtn.visible = false
         // if selected renter is not empty, update booking list
         if newValue != null then
            filteredBookings = bookings
               .filter( b => b.renter == newValue && b.outstandingItems.nonEmpty )
            bookingList ++= filteredBookings
               .map( b =>
                  b.bookingStartTime.format( dateFormatter ) + " to " + b.bookingEndTime
                     .format( dateFormatter )
               )
               .toSet
      )

   // return button
   private val returnItemBtn = new Button:
      text = "Return Item"
      visible = false

   // dropdown menu to select booking
   private val bookingPicker = new ChoiceBox[String]:
      items = bookingList
      prefWidth = 270

   private var selectedBooking = Buffer.empty[Booking]

   // generate list of items in the booking
   private var itemCheckBoxes = Set.empty[CheckBox]

   // display outstanding items in the booking when a booking is selected
   bookingPicker
      .valueProperty()
      .addListener( ( observable, oldValue, newValue ) =>
         if newValue != null then
            // get booking instance based on selected string value
            selectedBooking = findBooking( newValue )
            // if found, display a list of items that need to be returned
            if selectedBooking.nonEmpty then
               itemCheckBoxes =
                  selectedBooking.flatMap( _.outstandingItems ).toSet.map( i => CheckBox( i.name ) )
               // dislay items to be returned or display a message informing user
               if itemCheckBoxes.nonEmpty then
                  itemList.children = itemCheckBoxes
                  returnItemBtn.visible = true
               else
                  itemList.children =
                     Array( Label( "All items in this booking have been returned" ) )
         else itemList.children.clear()
      )

   // group booking selection with label
   private val bookingContainer = new VBox:
      children = Array( pickerLabel, bookingPicker )
      spacing = 5

   // group renter selection and booking selection
   private val filterContainer = new HBox:
      children = Array( renterPicker.container, bookingContainer )
      spacing = 20

   // button action to return selected items
   returnItemBtn.onAction = ( ( e ) =>
      val selections = itemCheckBoxes.filter( _.selected.value ).map( _.text.value )
      if selectedBooking.nonEmpty && selections.nonEmpty then
         selectedBooking.foreach( _.returnItems( selections ) )
         displayAlert( msgAlert, "Item(s) have been returned" )
         resetReturnItemPage()
      else displayAlert( msgAlert, "Please select an item to return" )
   )

   private val returnItemPageContainer = new VBox:
      children = Array( filterContainer, itemList, returnItemBtn )
      padding = Insets( 20 )
      spacing = 10

   val returnItemScrollContainer = new ScrollPane:
      content = returnItemPageContainer
      visible = false

   /** clear dropdown menu selections and hide return button */
   def resetReturnItemPage() =
      renterPicker.picker.getSelectionModel.clearSelection()
      bookingPicker.getSelectionModel.clearSelection()
      returnItemBtn.visible = false

   /** return a booking instance based on booking period string */
   private def findBooking( bookingPeriodStr: String ): Buffer[Booking] =
      val period = bookingPeriodStr.split( " to " )
      val start = LocalDateTime.parse( period.head, dateFormatter )
      val end = LocalDateTime.parse( period.last, dateFormatter )

      filteredBookings.filter( b => b.bookingStartTime == start && b.bookingEndTime == end )