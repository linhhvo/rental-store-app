package ui

import logic.RentalStore.{addBooking, getAvailableItems}
import logic.SavedItems.{getSavedItems, rentalRatePerHour, resetSavedList}
import logic.{Booking, InvalidDateTime}
import scalafx.beans.property.{BooleanProperty, DoubleProperty}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.*
import scalafx.scene.layout.VBox
import scalafx.scene.text.FontWeight.Bold
import scalafx.scene.text.{Font, FontWeight}
import ui.Helper.*

import java.time.{LocalDate, LocalDateTime}

object RentItem:
   val savedItems = getSavedItems
   var render = BooleanProperty( false )

   // Container to enable scrolling if page is long
   val rentItemScrollContainer = ScrollPane()
   rentItemScrollContainer.setVisible( false )
   rentItemScrollContainer.setPadding( Insets( 20 ) )

   /** Rental period container */
   // Date and time controls for rental period start
   var rentStart = createDateTimePicker( "Start" )

   // Date and time controls for rental period end
   var rentEnd = createDateTimePicker( "End" )
   rentEnd._3.setValue(
       String.format( "%02d" + ":00", LocalDateTime.now().plusHours( 1 ).getHour )
   )

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
   var totalCost = DoubleProperty(
       rentalRatePerHour * calculateRentalDuration( rentStart, rentEnd )
   )

   // Calculate and update total cost when start date and time change
   rentStart._2
      .valueProperty()
      .addListener( ( observable, oldDate, newDate ) => {
         try totalCost.value = rentalRatePerHour * calculateRentalDuration( rentStart, rentEnd )
         catch
            case InvalidDateTime( msg ) =>
               displayAlert( msgAlert, msg )
               rentStart._2.setValue( LocalDate.now )
      } )
   rentStart._3
      .valueProperty()
      .addListener( ( observable, oldDate, newDate ) => {
         try totalCost.value = rentalRatePerHour * calculateRentalDuration( rentStart, rentEnd )
         catch
            case InvalidDateTime( msg ) =>
               displayAlert( msgAlert, msg )
               rentStart._3.setValue( String.format( "%02d" + ":00", LocalDateTime.now().getHour ) )
      } )

   // Calculate and update total cost when end date and time change
   rentEnd._2
      .valueProperty()
      .addListener( ( observable, oldDate, newDate ) => {
         try totalCost.value = rentalRatePerHour * calculateRentalDuration( rentStart, rentEnd )
         catch
            case InvalidDateTime( msg ) =>
               displayAlert( msgAlert, msg )
               rentEnd._2.setValue( LocalDate.now )
      } )
   rentEnd._3
      .valueProperty()
      .addListener( ( observable, oldDate, newDate ) => {
         try totalCost.value = rentalRatePerHour * calculateRentalDuration( rentStart, rentEnd )
         catch
            case InvalidDateTime( msg ) =>
               displayAlert( msgAlert, msg )
               rentEnd._3.setValue(
                   String.format( "%02d" + ":00", LocalDateTime.now().plusHours( 1 ).getHour )
               )
      } )

   val costLabel = Label( s"Rental Total Cost: ${totalCost.value}" )
   costLabel.font = Font( null, FontWeight( Bold ), 14 )
   costLabel.margin = Insets( 10, 0, 0, 0 )

   totalCost.onChange { ( source, oldValue, newValue ) =>
      costLabel.text = s"Rental Total Cost: ${totalCost.value}"
   }

   /** Booking button */
   val msgAlert = new Alert( Alert.AlertType.Information )
   val bookingBtn = Button( "Book Rental" )

   rentItemScrollContainer.setContent( renderPage )

   bookingBtn.onAction = (
       ( e ) =>
          if renterPicker.getValue == null then displayAlert( msgAlert, "Please select a renter" )
          else
             val newBooking = Booking( savedItems.map( _.itemId ).toSet,
                                       getDateTime( rentStart._2, rentStart._3 ),
                                       getDateTime( rentEnd._2, rentEnd._3 ),
                                       totalCost.toDouble,
                                       renterPicker.getValue
             )
             addBooking( newBooking )
             resetSavedList()
             savedItems.clear()
             displayAlert( msgAlert, "Rental is confirmed" )
             rentItemScrollContainer.setContent(
                 Label( "No items in the saved list. Add items to list to book a rental" )
             )
   )

   def renderPage =
      println( "render" )
      val allItems = getAvailableItems
      val itemElements = savedItems.map( savedItem =>
         allItems.find( i => i.itemId == savedItem.itemId ) match
            case Some( i ) => Label( "• " + i.name + " - Quantity: " + savedItem.rentQuantity )
            case None      => Label( null )
      )

      val label = Label( "Items added:" )
      label.font = Font( null, FontWeight( Bold ), 14 )

      val savedItemsContainer = new VBox():
         children = Array( label ) ++ itemElements

      /** Combined container */
      val boxContainer = new VBox:
         if savedItems.isEmpty then
            children =
               Array( Label( "No items in the saved list. Add items to list to book a rental" ) )
         else
            children = Array( savedItemsContainer,
                              periodContainer,
                              renterContainer,
                              //                        cmtContainer,
                              costLabel,
                              bookingBtn
            )
         spacing = 10

      boxContainer

   def resetSavedListPage() =
      rentItemScrollContainer.setContent( renderPage )
      rentStart._2.setValue( LocalDate.now )
      rentStart._3.setValue(
          String.format( "%02d" + ":00", LocalDateTime.now().getHour )
      )
      rentEnd._2.setValue( LocalDate.now )
      rentEnd._3.setValue(
          String.format( "%02d" + ":00", LocalDateTime.now().plusHours( 1 ).getHour )
      )
      renterPicker.getSelectionModel.clearSelection()