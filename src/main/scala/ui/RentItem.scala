package ui

import logic.RentalStore.{addBooking, getAvailableItems}
import logic.SavedItems.{getSavedItems, rentalRatePerHour, resetList}
import logic.{Booking, InvalidDateTime}
import scalafx.beans.property.DoubleProperty
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

   // Container to enable scrolling if page is long
   val rentItemScrollContainer = ScrollPane()
   rentItemScrollContainer.setVisible( false )
   rentItemScrollContainer.setPadding( Insets( 20 ) )

   /** Rental period container */
   // Date and time controls for rental period start
   val rentStart = createDateTimePicker( "Start" )

   // Date and time controls for rental period end
   val rentEnd = createDateTimePicker( "End" )

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
   var totalCost = DoubleProperty( 0 )

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
               rentEnd._3.setValue( String.format( "%02d" + ":00", LocalDateTime.now().getHour ) )
      } )

   totalCost.onChange { ( source, oldValue, newValue ) =>
      costLabel.text = s"Rental Total Cost: ${totalCost.value}"
   }
   val costLabel = Label( "Update rental period to view total cost" )
   costLabel.font = Font( null, FontWeight( Bold ), 14 )
   costLabel.margin = Insets( 10, 0, 0, 0 )

   /** Booking button */
   val msgAlert = new Alert( Alert.AlertType.Information )
   val bookingBtn = Button( "Book Rental" )
   bookingBtn.onAction = ( ( e ) =>
      val newBooking = Booking( savedItems.map( _.itemId ).toSet,
                                getDateTime( rentStart._2, rentStart._3 ),
                                getDateTime( rentEnd._2, rentEnd._3 ),
                                totalCost.toDouble,
                                renterPicker.getValue
      )
      addBooking( newBooking )
      resetList()
      savedItems.clear()
      displayAlert( msgAlert, "Rental is confirmed" )
   )

   /** Combined container */
   val boxContainer = new VBox:
      children = Array( renderSavedItems,
                        periodContainer,
                        renterContainer,
//                        cmtContainer,
                        costLabel,
                        bookingBtn
      )
      spacing = 10

   rentItemScrollContainer.setContent( boxContainer )

   def renderSavedItems =
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

      savedItemsContainer
