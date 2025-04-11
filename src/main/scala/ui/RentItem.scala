package ui

import logic.RentalStore.addBooking
import logic.Renter.renters
import logic.SavedItems.*
import logic.utils.BookingDuration
import logic.{Booking, InvalidDateTime, ItemNotAvailable}
import scalafx.beans.property.DoubleProperty
import scalafx.geometry.Insets
import scalafx.scene.control.*
import scalafx.scene.control.Alert.AlertType.Confirmation
import scalafx.scene.layout.VBox
import scalafx.scene.text.FontWeight.Bold
import scalafx.scene.text.{Font, FontWeight}
import ui.Helper.*
import ui.utils.RenterPicker

import java.time.{LocalDate, LocalDateTime}

object RentItem:
   // predefine size for element
   private val med = 125

   /** Rental period container */
   // Date and time controls for rental period start
   private val rentStart = createDateTimePicker( "Start" )

   // Date and time controls for rental period end
   private val rentEnd = createDateTimePicker( "End" )
   // end date should be initially set to 1 hour ahead of start time
   // if start time is 23:00, end date should be set to next day
   rentEnd._2.setValue( LocalDateTime.now().plusHours( 1 ).toLocalDate )
   rentEnd._3.setValue(
       String.format( "%02d" + ":00", LocalDateTime.now.plusHours( 1 ).getHour )
   )

   // group datetime pickers with label in container
   private val periodLabel = Label( "Select rental period" )
   periodLabel.font = Font( null, FontWeight( Bold ), 14 )
   private val periodContainer = VBox( periodLabel, rentStart._1, rentEnd._1 )
   periodContainer.spacing = 5

   /** Renter Container */
   private val renterPicker = RenterPicker(renters)

   /** Total rental cost container */
   private val totalCost = DoubleProperty( 0.0 )

   // Calculate and update total cost when start date and time change
   rentStart._2
      .valueProperty()
      .addListener( ( observable, oldDate, newDate ) =>
         try totalCost.value = calculateRentalCost
         catch
            case InvalidDateTime( msg ) =>
               displayAlert( msgAlert, msg )
               rentStart._2.setValue( LocalDate.now )
      )
   rentStart._3
      .valueProperty()
      .addListener( ( observable, oldDate, newDate ) =>
         try totalCost.value = calculateRentalCost
         catch
            case InvalidDateTime( msg ) =>
               displayAlert( msgAlert, msg )
               rentStart._3.setValue( String.format( "%02d" + ":00", LocalDateTime.now().getHour ) )
      )

   // Calculate and update total cost when end date and time change
   rentEnd._2
      .valueProperty()
      .addListener( ( observable, oldDate, newDate ) =>
         try totalCost.value = calculateRentalCost
         catch
            case InvalidDateTime( msg ) =>
               displayAlert( msgAlert, msg )
               rentEnd._2.setValue( LocalDate.now )
      )
   rentEnd._3
      .valueProperty()
      .addListener( ( observable, oldDate, newDate ) =>
         try totalCost.value = calculateRentalCost
         catch
            case InvalidDateTime( msg ) =>
               displayAlert( msgAlert, msg )
               rentEnd._3.setValue(
                   String.format( "%02d" + ":00", LocalDateTime.now().plusHours( 1 ).getHour )
               )
      )

   private val costLabel = Label( s"Rental Total Cost: ${totalCost.value}" )
   costLabel.font = Font( null, FontWeight( Bold ), 14 )
   costLabel.margin = Insets( 10, 0, 10, 0 )

   // update cost label to display new cost when start or end datetime change
   totalCost.onChange { ( source, oldValue, newValue ) =>
      costLabel.text = s"Rental Total Cost: ${totalCost.value}"
   }

   /** Booking button */

   // set up alert to display user message
   private val msgAlert = new Alert( Alert.AlertType.Information )

   // button to confirm booking and add new rental
   private val bookingBtn = Button( "Book Rental" )
   bookingBtn.prefWidth = med

   bookingBtn.onAction = (
       ( e ) =>
          // display error message if renter is not selected
          if renterPicker.picker.getValue == null then
             displayAlert( msgAlert, "Please select a renter" )
          // when all information is entered
          else
             val newBooking = Booking( savedItems,
                                       getDateTime( rentStart._2, rentStart._3 ),
                                       getDateTime( rentEnd._2, rentEnd._3 ),
                                       totalCost.toDouble,
                                       renterPicker.picker.getValue
             )

             // add new booking to the store
             try
                addBooking( newBooking )
                savedItems = resetSavedList() // update saved item list in external file to be empty
                displayAlert( msgAlert, "Rental is confirmed" )
                // update page to display user message
                rentItemScrollContainer.setContent( renderPage )
             catch case ItemNotAvailable( m ) => displayAlert( msgAlert, m )
   )

   /** Clear list button */

   // button to delete all items in saved list and clear saved list
   private val clearBtn = Button( "Delete All Items" )
   clearBtn.prefWidth = med
   clearBtn.onAction = (
       ( e ) =>
          new Alert( Confirmation ) {
             headerText = "Do you want to remove all items from the saved list?"
             buttonTypes = Seq( ButtonType.Yes, ButtonType.No )
          }.showAndWait() match
             case Some( ButtonType.Yes ) =>
                savedItems = resetSavedList()
                rentItemScrollContainer.setContent( renderPage )
             case _                      => // Do nothing
   )

   /** Page scroll container */
   val rentItemScrollContainer = new ScrollPane:
      visible = false
      padding = Insets( 20 )
      content = renderPage

   /** method to update data and update GUI content */
   private def renderPage =
      // generate GUI elements to display saved items list
      val itemElements = savedItems.map( savedItem =>
         savedItem.getDetailsFromItemList() match
            case Some( i ) => Label( "• " + i.name + " - Quantity: " + savedItem.rentQuantity )
            case None      => Label( null )
      )

      val label = Label( "Items added:" )
      label.font = Font( null, FontWeight( Bold ), 14 )

      // group saved items list with label
      val savedItemsContainer = new VBox():
         children = Array( label ) ++ itemElements

      /** overall page container */
      val boxContainer = new VBox:
         if savedItems.isEmpty then
            children =
               Array( Label( "No items in the saved list. Add items to list to book a rental" ) )
         else
            children = Array( savedItemsContainer,
                              periodContainer,
                              renterPicker.container,
                              costLabel,
                              bookingBtn,
                              clearBtn
            )
         spacing = 10

      boxContainer

   /** method to clear input fields and reset initial datetime picker values */
   def resetSavedListPage() =
      // recalculate cost in case new item is added to saved list
      totalCost.value = calculateRentalCost

      // reset input fields to default values
      rentEnd._3.setValue(
          String.format( "%02d" + ":00", LocalDateTime.now().plusHours( 1 ).getHour )
      )
      rentStart._3.setValue(
          String.format( "%02d" + ":00", LocalDateTime.now().getHour )
      )
      rentEnd._2.setValue( LocalDateTime.now().plusHours( 1 ).toLocalDate )
      rentStart._2.setValue( LocalDate.now )

      renterPicker.picker.getSelectionModel.clearSelection()

      // render page container to get updated saved items list
      rentItemScrollContainer.setContent( renderPage )

   /** validate datetime selections and covert selected values to LocalDateTime then use valid
     * datetime values to get booking duration
     */
   private def calculateRentalDuration: BookingDuration =

      // check that start date and end date are not empty
      if rentStart._2.getValue == null || rentEnd._2.getValue == null then
         throw InvalidDateTime( "Date and time cannot be empty" )
      else
         // transform datetimes to LocalDateTime type
         val startDT = getDateTime( rentStart._2, rentStart._3 )
         val endDT = getDateTime( rentEnd._2, rentEnd._3 )

         // check that user selected a valid rental period
         if startDT.isAfter( endDT ) then
            throw InvalidDateTime( "End date time cannot be before start date time " )
         else if startDT.isEqual( endDT ) then
            throw InvalidDateTime( "Start date time cannot be the same as end date time" )
         else if startDT.toLocalDate.isEqual( LocalDate.now )
            && rentStart._3.getValue.split( ":" ).head.toInt < LocalDateTime.now.getHour
         then throw InvalidDateTime( "Start date time cannot be before current date time" )

         BookingDuration( startDT, endDT )

   /** method to calculate the total rental cost of saved items */
   private def calculateRentalCost =
      val rentalDuration = calculateRentalDuration
      combinedRatePerWeek() * rentalDuration.durationInWeeks
         + combinedRatePerDay() * rentalDuration.durationInDays
         + combinedRatePerHour() * rentalDuration.durationInHours