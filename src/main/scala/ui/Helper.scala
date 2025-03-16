package ui

import javafx.scene.control.{DateCell as JFXDateCell, DatePicker as JFXDatePicker}
import javafx.util.Callback
import logic.InvalidDateTime
import scalafx.collections.ObservableBuffer
import scalafx.scene.Node
import scalafx.scene.control.*
import scalafx.scene.layout.{ColumnConstraints, GridPane, Pane}
import scalafx.util.StringConverter

import java.time.format.DateTimeFormatter
import java.time.{Duration, LocalDate, LocalDateTime}

object Helper:
   // Function to show a page in the stack pane while hiding the rest
   def displayPage( parentContainer: Pane, targetPage: Node ) =
      parentContainer.children.foreach( page => page.setVisible( false ) )
      targetPage.setVisible( true )

   // Toggle visibility of a node
   def toggleDisplay( element: Node ) =
      if element.visible.get() then
         element.visible = false
         element.managed = false
      else
         element.visible = true
         element.managed = true

   // Show pop-up alert with appropriate message
   def displayAlert( alert: Alert, msg: String ) =
      alert.headerText = msg
      alert.showAndWait()

   // Create a date time picker control with customized label
   def createDateTimePicker( labelText: String ) =
      // Change how date format is displayed
      val converter = new StringConverter[LocalDate]:
         val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern( "d-MM-yyyy" )

         override def toString( date: LocalDate ): String =
            if date != null then dateFormatter.format( date ) else ""

         override def fromString( string: String ): LocalDate =
            if string != null && string.nonEmpty then LocalDate.parse( string, dateFormatter )
            else null

      // Disable dates that are before current date
      val dayCellFactory: Callback[JFXDatePicker, JFXDateCell] = ( datePicker: JFXDatePicker ) =>
         new JFXDateCell:
            override def updateItem( item: LocalDate, empty: Boolean ): Unit =
               super.updateItem( item, empty )

               if item != null && item.isBefore( LocalDate.now )
               then setDisable( true )

      // List of hours for ChoiceBox
      val hours = ObservableBuffer[String]() ++ ( 0 to 23 ).toArray.map( num =>
         String.format( "%02d" + ":00", num )
      )
//      val mins = ObservableBuffer[Int]() ++ ( 0 to 59 ).toArray

      val date = new DatePicker()
      date.maxWidth = 125
      date.setValue( LocalDate.now )
      date.setConverter( converter )
      date.editable = false
      date.dayCellFactory = dayCellFactory

      val hour = ChoiceBox( hours )
      hour.maxWidth = 70
      hour.setValue( String.format( "%02d" + ":00", LocalDateTime.now().getHour ) )
//      val min = ChoiceBox( mins )
//      min.maxWidth = 70
//      min.setValue( LocalDateTime.now().getMinute )

      val container = GridPane()
      container.add( Label( s"${labelText} Date" ), 0, 0 )
      container.add( date, 0, 1 )
      container.add( Label( s"${labelText} Time" ), 1, 0 )
      container.add( hour, 1, 1 )
//      container.add( Label( s"${labelText} Min" ), 2, 0 )
//      container.add( min, 2, 1 )
      container.columnConstraints =
         Array( ColumnConstraints( 135 ), ColumnConstraints( 80 ), ColumnConstraints( 80 ) )

      ( container, date, hour )

   def calculateRentalDuration( start: ( GridPane, DatePicker, ChoiceBox[String] ),
                                end:   ( GridPane, DatePicker, ChoiceBox[String] )
   ) =
      if start._2.getValue == null || end._2.getValue == null then
         throw InvalidDateTime( "Date and time cannot be empty" )
      else if start._3.getValue.split( ":" ).head.toInt < LocalDateTime.now.getHour
         || end._3.getValue.split( ":" ).head.toInt < LocalDateTime.now.getHour
      then throw InvalidDateTime( "Time cannot be before current time" )
      else
         val startStr = start._2.getValue.toString + " " + start._3.getValue
         val endStr = end._2.getValue.toString + " " + end._3.getValue

         val startDT =
            LocalDateTime.parse( startStr, DateTimeFormatter.ofPattern( "yyyy-MM-d H:m" ) )
         val endDT =
            LocalDateTime.parse( endStr, DateTimeFormatter.ofPattern( "yyyy-MM-d H:m" ) )

         if startDT.isAfter( endDT ) then
            throw InvalidDateTime( "End date and time cannot be before start date and time " )
         else Duration.between( startDT, endDT ).toHours.toDouble