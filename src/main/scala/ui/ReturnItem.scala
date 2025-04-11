package ui

import logic.Booking
import logic.IOHandler.writeBookings
import logic.RentalStore.{bookings, updateItemsDataSource}
import logic.Renter.renters
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.*
import scalafx.scene.control.Alert.AlertType.Information
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.text.FontWeight.Bold
import scalafx.scene.text.{Font, FontWeight}
import ui.Helper.displayAlert
import ui.utils.RenterPicker

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
   private val itemList = new VBox()
   itemList.spacing = 5

   // booking selection
   private val pickerLabel = Label( "Select booking" )
   pickerLabel.font = Font( null, FontWeight( Bold ), 14 )

   // get list of bookings
   private val bookingList = new ObservableBuffer[String]()

   // initialize a bookings list to replace with bookings filtered based on selected renter
   private var filteredBookings = bookings

   // update booking list as renter selection changes
   renterPicker.picker
      .valueProperty()
      .addListener( ( observable, oldValue, newValue ) =>
         // empty booking list
         bookingList.clear()
         // if selected renter is not empty, update booking list
         if newValue != null then
            filteredBookings = bookings
               .filter( b => b.renter == newValue && b.outstandingItems.nonEmpty )
            bookingList ++= filteredBookings
               .map( b =>
                  b.bookingStartTime.format( dateFormatter ) + " to " + b.bookingEndTime
                     .format( dateFormatter )
               )
      )

   // return button
   private val returnItemBtn = Button( "Return Item" )
   returnItemBtn.visible = false

   // dropdown menu to select booking
   private val bookingPicker = ChoiceBox( bookingList )
   bookingPicker.prefWidth = 270

   private var selectedBooking =
      if bookingPicker.getSelectionModel.getSelectedItem != null then
         findBooking( bookingPicker.getValue )
      else bookings.headOption

   // generate list of items in the booking
   private var itemCheckBoxes: Buffer[CheckBox] = Buffer.empty
   bookingPicker
      .valueProperty()
      .addListener( ( observable, oldValue, newValue ) =>
         if newValue != null then
            // get booking instance based on selected string value
            selectedBooking = findBooking( newValue )
            // if found, display a list of items that need to be returned
            selectedBooking match
               case Some( booking ) =>
                  itemCheckBoxes = booking.outstandingItems.map( i => CheckBox( i.name ) )
                  // dislay items to be returned or display a message informing user
                  if itemCheckBoxes.nonEmpty then
                     itemList.children = itemCheckBoxes
                     returnItemBtn.visible = true
                  else
                     itemList.children =
                        Array( Label( "All items in this booking have been returned" ) )
               case _ => itemList.children = Array( Label( "This booking doesn't exist" ) )
         else itemList.children.clear()
      )

   // group booking selection with label
   private val bookingContainer = new VBox:
      children = Array( pickerLabel, bookingPicker )
      spacing = 5

   // group renter selection and booking selection
   private val filterContainer = HBox( renterPicker.container, bookingContainer )
   filterContainer.spacing = 20

   // button action to return selected items
   returnItemBtn.onAction = ( ( e ) =>
      val selections = itemCheckBoxes.filter( _.selected.value ).map( _.text.value )
      selectedBooking match
         case Some( b ) =>
            b.returnItems( selections )
            writeBookings( bookings )
            updateItemsDataSource()
            displayAlert( msgAlert, "Item(s) have been returned" )
            resetReturnItemPage()
         case _         => println( "do nothing" )
   )

   private val returnItemPageContainer = VBox( filterContainer, itemList, returnItemBtn )
   returnItemPageContainer.padding = Insets( 20 )
   returnItemPageContainer.spacing = 10

   val returnItemScrollContainer = ScrollPane()
   returnItemScrollContainer.setContent( returnItemPageContainer )
   returnItemScrollContainer.setVisible( false )

   /** clear dropdown menu selections and hide return button */
   def resetReturnItemPage() =
      renterPicker.picker.getSelectionModel.clearSelection()
      bookingPicker.getSelectionModel.clearSelection()
      returnItemBtn.visible = false

   /** return a booking instance based on booking period string */
   private def findBooking( bookingPeriodStr: String ): Option[Booking] =
      val period = bookingPeriodStr.split( " to " )
      val start = LocalDateTime.parse( period.head, dateFormatter )
      val end = LocalDateTime.parse( period.last, dateFormatter )

      filteredBookings.find( b => b.bookingStartTime == start && b.bookingEndTime == end )