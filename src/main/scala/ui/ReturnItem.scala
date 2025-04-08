package ui

import logic.IOHandler.writeBookings
import logic.RentalStore.{activeItems, bookings, updateItemsDataSource}
import logic.{Booking, Incident, IncidentType}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.*
import scalafx.scene.control.Alert.AlertType.Information
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.text.FontWeight.Bold
import scalafx.scene.text.{Font, FontWeight}
import ui.Helper.{createRenterPicker, displayAlert}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.collection.mutable.Buffer

object ReturnItem:
   private val smallWidth = 240
   private val btnWidth = 125
   private val smallHeight = 75

   /** User message alert */
   private val messageAlert = new Alert( Information )

   /** Select renter */
   private val renterPicker = createRenterPicker

   /** Select booking */

   // booking selection
   private val pickerLabel = Label( "Select booking" )
   pickerLabel.font = Font( null, FontWeight( Bold ), 14 )

   // get list of bookings
   private val bookingList = new ObservableBuffer[String]()

   // define date conversion format
   private val dateFormatter = DateTimeFormatter.ofPattern( "dd-MM-yyyy HH:mm" )

   // initialize a bookings list to replace with bookings filtered based on selected renter
   private var filteredBookings = bookings

   // booking items list container
   private val itemList = new VBox()
   itemList.spacing = 5

   // update booking list as renter selection changes
   renterPicker._1
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

   // dropdown menu to select booking
   private val bookingPicker = ChoiceBox( bookingList )
   bookingPicker.prefWidth = 270

   // group booking selection with label
   private val bookingContainer = new VBox:
      children = Array( pickerLabel, bookingPicker )
      spacing = 5

   /** Button to return selected items */

   private val returnItemBtn = Button( "Return Item" )
   returnItemBtn.prefWidth = btnWidth
   returnItemBtn.visible = false

   // store booking selection
   private var selectedBooking =
      if bookingPicker.getSelectionModel.getSelectedItem != null then
         findBooking( bookingPicker.getValue )
      else bookings.headOption

   // generate list of items in the booking for return
   private var itemCheckBoxes: Buffer[CheckBox] = Buffer.empty

   // generate list of items for incident reporting
   private var itemRadios: Buffer[RadioButton] = Buffer.empty

   // display item list as booking selection changes
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
                  itemRadios = booking.outstandingItems.map( i => RadioButton( i.name ) )
                  itemRadios.foreach( rb => rb.toggleGroup = itemToggleGroup )
                  // dislay items to be returned or display a message informing user
                  if itemCheckBoxes.nonEmpty then
                     itemList.children = itemCheckBoxes
                     returnItemBtn.visible = true
                     incidentContainer.children =
                        Array( Label( "Select an item below to report incident:" ) )
                           ++ itemRadios
                           ++ Array(
                               Label( "Incident Type" ),
                               incTypeChoiceBox,
                               Label( "Incident Date" ),
                               incidentDatePicker,
                               Label( "Incident Detail" ),
                               incidentCmt,
                               Label( "Repair Cost" ),
                               repairCostField,
                               submitIncidentBtn
                           )
                  else
                     itemList.children =
                        Array( Label( "All items in this booking have been returned" ) )
                     returnItemBtn.visible = false
               case _ => itemList.children = Array( Label( "This booking doesn't exist" ) )
         else
            itemList.children.clear()
            incidentContainer.children.clear()
            returnItemBtn.visible = false
      )

   // group renter selection and booking selection
   private val filterContainer = HBox( renterPicker._2, bookingContainer )
   filterContainer.spacing = 20

   // button action to return selected items
   returnItemBtn.onAction = ( ( e ) =>
      val selections = itemCheckBoxes.filter( _.selected.value ).map( _.text.value )
      selectedBooking match
         case Some( b ) =>
            b.returnItems( selections )
            writeBookings( bookings )
            updateItemsDataSource()
            resetReturnItemPage()
            displayAlert( messageAlert, "Item(s) have been returned" )
         case _         => println( "do nothing" )
   )

   /** Report item incident */

   // toggle group to select item
   private val itemToggleGroup = ToggleGroup()

   // dropdown menu to select incident type
   private val incTypeList =
      ObservableBuffer[IncidentType]( IncidentType.Loss, IncidentType.Damage )
   private val incTypeChoiceBox = ChoiceBox( incTypeList )
   incTypeChoiceBox.maxWidth = smallWidth

   // date picker to select incident date
   private val incidentDatePicker = DatePicker()
   incidentDatePicker.promptText = "Select incident date"
   incidentDatePicker.maxWidth = smallWidth

   // text field to add incident comment
   private val incidentCmt = TextArea()
   incidentCmt.maxWidth = smallWidth
   incidentCmt.maxHeight = smallHeight
   incidentCmt.promptText = "Enter incident detail"

   // text field to add cost to repair
   private val repairCostField = TextField()
   repairCostField.maxWidth = smallWidth
   repairCostField.promptText = "Enter repair cost"

   // button to add incident to item
   private val submitIncidentBtn = Button( "Submit" )

   // group incident input fields with labels
   private val incidentContainer = new VBox:
      spacing = 7
      margin = Insets( 5, 0, 5, 0 )
      visible = true

   // set action to add incident to item
   submitIncidentBtn.onAction = (
       ( e ) =>
          // get selected item radio
          val selectedRadio = itemRadios.find( rb => rb.isSelected )
          // check if a radio button is selected
          selectedRadio match
             case Some( rb ) =>
                // check that all input fields have entered data
                if incTypeChoiceBox.getSelectionModel.getSelectedItem != null
                   && incidentDatePicker.getValue != null
                   && incidentCmt.getText.nonEmpty
                   && repairCostField.getText.nonEmpty
                then
                   // create new incident
                   val newIncident = Incident( incidentDatePicker.getValue,
                                               incTypeChoiceBox.getValue,
                                               incidentCmt.getText,
                                               repairCostField.getText.toDouble
                   )
                   // find the item instance based on selected radio
                   val itemOption = activeItems.find( _.name == rb.getText )
                   itemOption match
                      case Some( item ) =>
                         // add incident to item
                         item.reportIncident( newIncident )
                         // display success message to user
                         displayAlert( messageAlert, "Incident has been reported" )
                      case None         => displayAlert( messageAlert, "Cannot find this item" )

                   // reset and clear input fields
                   incidentCmt.clear()
                   repairCostField.clear()
                   incTypeChoiceBox.getSelectionModel.clearSelection()
                   incidentDatePicker.setValue( null )
                else displayAlert( messageAlert, "Missing information" )
             case None       => displayAlert( messageAlert, "No item is selected" )
   )

   /** Page Containers */
   private val returnItemPageContainer = new VBox:
      children = Array( filterContainer, itemList, returnItemBtn, incidentContainer )
      spacing = 15
      padding = Insets( 20 )

   val returnItemScrollContainer = ScrollPane()
   returnItemScrollContainer.setContent( returnItemPageContainer )
   returnItemScrollContainer.setVisible( false )

   /** clear dropdown menu selections and hide return button */
   def resetReturnItemPage() =
      renterPicker._1.getSelectionModel.clearSelection()
      bookingPicker.getSelectionModel.clearSelection()
      returnItemBtn.visible = false

      incidentCmt.clear()
      repairCostField.clear()
      incTypeChoiceBox.getSelectionModel.clearSelection()
      incidentDatePicker.setValue( null )

   /** return a booking instance based on booking period string */
   private def findBooking( bookingPeriodStr: String ): Option[Booking] =
      val period = bookingPeriodStr.split( " to " )
      val start = LocalDateTime.parse( period.head, dateFormatter )
      val end = LocalDateTime.parse( period.last, dateFormatter )

      filteredBookings.find( b => b.bookingStartTime == start && b.bookingEndTime == end )