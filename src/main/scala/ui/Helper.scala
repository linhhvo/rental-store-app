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
         val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern( "dd-MM-yyyy" )

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

      val date = new DatePicker()
      date.maxWidth = 125
      date.setValue( LocalDate.now )
      date.setConverter( converter )
      date.editable = false
      date.dayCellFactory = dayCellFactory

      val hour = ChoiceBox( hours )
      hour.maxWidth = 70
      hour.setValue( String.format( "%02d" + ":00", LocalDateTime.now().getHour ) )

      val container = GridPane()
      container.add( Label( s"${labelText} Date" ), 0, 0 )
      container.add( date, 0, 1 )
      container.add( Label( s"${labelText} Time" ), 1, 0 )
      container.add( hour, 1, 1 )
      container.columnConstraints =
         Array( ColumnConstraints( 135 ), ColumnConstraints( 80 ), ColumnConstraints( 80 ) )

      ( container, date, hour )

   /** method to combine date and time inputs and convert to LocalDateTime */
   def getDateTime( date: DatePicker, time: ChoiceBox[String] ) =
      val dateTimeStr = date.getValue.toString + " " + time.getValue
      LocalDateTime.parse( dateTimeStr, DateTimeFormatter.ofPattern( "yyyy-MM-dd H:m" ) )
