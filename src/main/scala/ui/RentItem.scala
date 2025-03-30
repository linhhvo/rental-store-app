package ui

import logic.RentalStore.{addBooking, ownedItems}
import logic.SavedItems.*
import logic.{Booking, InvalidDateTime}
import scalafx.beans.property.DoubleProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.*
import scalafx.scene.control.Alert.AlertType.Confirmation
import scalafx.scene.layout.{GridPane, VBox}
import scalafx.scene.text.FontWeight.Bold
import scalafx.scene.text.{Font, FontWeight}
import ui.Helper.*

import java.time.{Duration, LocalDate, LocalDateTime}

object RentItem:
   // retrieve saved items list from external file
//   var savedItems = getSavedItems

   // Container to enable scrolling if page is long
   val rentItemScrollContainer = ScrollPane()
   rentItemScrollContainer.setVisible( false )
   rentItemScrollContainer.setPadding( Insets( 20 ) )

   /** Rental period container */
   // Date and time controls for rental period start
   var rentStart = createDateTimePicker( "Start" )

   // Date and time controls for rental period end
   var rentEnd = createDateTimePicker( "End" )
   // end date should be initially set to 1 hour ahead of start time
   // if start time is 23:00, end date should be set to next day
   rentEnd._2.setValue( LocalDateTime.now().plusHours( 1 ).toLocalDate )
   rentEnd._3.setValue(
       String.format( "%02d" + ":00", LocalDateTime.now().plusHours( 1 ).getHour )
   )

   // group datetime pickers with label in container
   val periodLabel = Label( "Select rental period" )
   periodLabel.font = Font( null, FontWeight( Bold ), 14 )
   val periodContainer = VBox( periodLabel, rentStart._1, rentEnd._1 )
   periodContainer.spacing = 5

   /** Renter Container */
   val renterLabel = Label( "Select renter" )
   renterLabel.font = Font( null, FontWeight( Bold ), 14 )
   val renterList = ObservableBuffer[String]( "cookie1", "cookie2" )
   val renterPicker = ChoiceBox( renterList )
   val renterContainer = new VBox:
      children = Array( renterLabel, renterPicker )
      spacing = 5

//   /** Comment Container */
//   val cmtLabel = Label( "Comment" )
//   cmtLabel.font = Font( null, FontWeight( Bold ), 14 )
//   val cmtText = TextArea()
//   cmtText.maxWidth = 285
//   cmtText.maxHeight = 50
//   val cmtContainer = new VBox:
//      children = Array( cmtLabel, cmtText )
//      spacing = 5

   /** Total rental cost container */
   var totalCost = DoubleProperty( calculateRentalCost )

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

   val costLabel = Label( s"Rental Total Cost: ${totalCost.value}" )
   costLabel.font = Font( null, FontWeight( Bold ), 14 )
   costLabel.margin = Insets( 10, 0, 0, 0 )

   // update cost label to display new cost when start or end datetime change
   totalCost.onChange { ( source, oldValue, newValue ) =>
      costLabel.text = s"Rental Total Cost: ${totalCost.value}"
   }

   /** Booking button */

   // set up alert to display user message
   val msgAlert = new Alert( Alert.AlertType.Information )

   // button to confirm booking and add new rental
   val bookingBtn = Button( "Book Rental" )
   bookingBtn.onAction = (
       ( e ) =>
          // display error message if renter is not selected
          if renterPicker.getValue == null then displayAlert( msgAlert, "Please select a renter" )
          // when all information is entered
          else
             val newBooking = Booking( savedItems,
                                       getDateTime( rentStart._2, rentStart._3 ),
                                       getDateTime( rentEnd._2, rentEnd._3 ),
                                       totalCost.toDouble,
                                       renterPicker.getValue
             )
             addBooking( newBooking ) // add new booking to the store
             resetSavedList() // update saved item list in external file to be empty
             displayAlert( msgAlert, "Rental is confirmed" )
             // update page to display user message
             rentItemScrollContainer.setContent( renderPage )
   )

   /** Clear list button */

   // button to delete all items in saved list and clear saved list
   val clearBtn = Button( "Delete All Items" )
   clearBtn.onAction = (
       ( e ) =>
          new Alert( Confirmation ) {
             headerText = "Do you want to remove all items from the saved list?"
             buttonTypes = Seq( ButtonType.Yes, ButtonType.No )
          }.showAndWait() match
             case Some( ButtonType.Yes ) =>
                resetSavedList()
                rentItemScrollContainer.setContent( renderPage )
             case _                      => // Do nothing
   )

   // set content to scroll pane container
   rentItemScrollContainer.setContent( renderPage )

   /** method to update data and update GUI content */
   def renderPage =
      // generate GUI elements to display saved items list
      val itemElements = savedItems.map( savedItem =>
         ownedItems.find( i => i.itemId == savedItem.itemId ) match
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
                              renterContainer,
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
      // render page container to get updated saved items list
      rentItemScrollContainer.setContent( renderPage )
      // reset input fields to default values
      rentStart._2.setValue( LocalDate.now )
      rentStart._3.setValue(
          String.format( "%02d" + ":00", LocalDateTime.now().getHour )
      )
      rentEnd._2.setValue( LocalDateTime.now().plusHours( 1 ).toLocalDate )
      rentEnd._3.setValue(
          String.format( "%02d" + ":00", LocalDateTime.now().plusHours( 1 ).getHour )
      )
      renterPicker.getSelectionModel.clearSelection()

   /** method to calculate rental period based on selected start and end datetimes */
   def calculateRentalDuration =
      // initialize duration values
      var durationInWeeks = 0
      var durationInDays = 0
      var durationInHours = 0

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
         else if rentStart._3.getValue.split( ":" ).head.toInt < LocalDateTime.now.getHour then
            throw InvalidDateTime( "Start date time cannot be before current date time" )
         // if valid period is selected, calculate duration
         else
            durationInWeeks = Duration.between( startDT, endDT ).toDays.toInt / 7
            durationInDays = Duration.between( startDT, endDT ).toDays.toInt   % 7
            durationInHours = Duration.between( startDT, endDT ).toHours.toInt % 24

         ( durationInWeeks, durationInDays, durationInHours )

   /** method to calculate the total rental cost of saved items */
   def calculateRentalCost =
      combinedRatePerWeek * calculateRentalDuration._1 + combinedRatePerDay * calculateRentalDuration._2 + combinedRatePerHour * calculateRentalDuration._3