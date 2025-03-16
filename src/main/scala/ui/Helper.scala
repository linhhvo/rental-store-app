package ui

import scalafx.collections.ObservableBuffer
import scalafx.scene.Node
import scalafx.scene.control.{Alert, ChoiceBox, DatePicker, Label}
import scalafx.scene.layout.{ColumnConstraints, GridPane, Pane}

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
      val hours = ObservableBuffer[Int]() ++ ( 0 to 23 ).toArray
      val mins = ObservableBuffer[Int]() ++ ( 0 to 59 ).toArray
      val date = new DatePicker()
      date.maxWidth = 125
      date.setValue( LocalDate.now )
      val hour = ChoiceBox( hours )
      hour.maxWidth = 70
      hour.setValue( LocalDateTime.now().getHour )
      val min = ChoiceBox( mins )
      min.maxWidth = 70
      min.setValue( LocalDateTime.now().getMinute )
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

   def calculateRentalDuration( start: ( GridPane, DatePicker, ChoiceBox[Int], ChoiceBox[Int] ),
                                end:   ( GridPane, DatePicker, ChoiceBox[Int], ChoiceBox[Int] )
   ) =
      val startStr = start._2.getValue.toString + " " + start._3.getValue + ":" + start._4.getValue
      val endStr = end._2.getValue.toString + " " + end._3.getValue + ":" + end._4.getValue

      val startDT =
         LocalDateTime.parse( startStr, DateTimeFormatter.ofPattern( "yyyy-MM-d H:m" ) )
      val endDT =
         LocalDateTime.parse( endStr, DateTimeFormatter.ofPattern( "yyyy-MM-d H:m" ) )
      Duration.between( startDT, endDT ).toHours