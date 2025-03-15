package ui

import logic.SavedItems.getSavedItems
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.*
import scalafx.scene.control.Alert.AlertType.Information
import scalafx.scene.layout.{ColumnConstraints, GridPane, VBox}
import scalafx.scene.text.FontWeight.Bold
import scalafx.scene.text.{Font, FontWeight}
import ui.Helper.displayAlert

object RentItem:
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
   val totalCost = 123.45
   val costLabel = Label( s"Rental Total Cost: ${totalCost}" )
   costLabel.font = Font( null, FontWeight( Bold ), 14 )
   costLabel.margin = Insets( 10, 0, 0, 0 )

   /** Booking button */
   val msgAlert = new Alert( Information )
   val bookingBtn = Button( "Book Rental" )
   bookingBtn.onAction = ( ( e ) => displayAlert( msgAlert, "Rental is confirmed" ) )

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
      val savedItems = getSavedItems
      val itemElements = savedItems.map( item => Label( item.itemId + " - " + item.rentQuantity ) )

      val label = Label( "Items added:" )
      label.font = Font( null, FontWeight( Bold ), 14 )

      val savedItemsContainer = new VBox():
         children = Array( label ) ++ itemElements

      savedItemsContainer
//      println(
//                 rentStartDate.getValue.toString + " " + rentStartHour.getValue + ":" + rentStartMin.getValue
//             )

   def createDateTimePicker( labelText: String ) =
      val hours = ObservableBuffer[Int]() ++ ( 0 to 23 ).toArray
      val mins = ObservableBuffer[Int]() ++ ( 0 to 59 ).toArray
      val date = new DatePicker()
      date.maxWidth = 125
      val hour = ChoiceBox( hours )
      hour.maxWidth = 70
      val min = ChoiceBox( mins )
      min.maxWidth = 70
      val container = GridPane()
      container.add( Label( s"${labelText} Date" ), 0, 0 )
      container.add( date, 0, 1 )
      container.add( Label( s"${labelText} Hour" ), 1, 0 )
      container.add( hour, 1, 1 )
      container.add( Label( s"${labelText} Min" ), 2, 0 )
      container.add( min, 2, 1 )
      container.columnConstraints =
         Array( ColumnConstraints( 135 ), ColumnConstraints( 80 ), ColumnConstraints( 80 ) )

      ( container, date, hour, min )