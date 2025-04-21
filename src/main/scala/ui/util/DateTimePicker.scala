package ui.util

import javafx.scene.control.{DateCell as JFXDateCell, DatePicker as JFXDatePicker}
import javafx.util.Callback
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{ChoiceBox, DatePicker, Label}
import scalafx.scene.layout.{ColumnConstraints, GridPane}
import ui.util.DateType.Start

import java.time.{LocalDate, LocalDateTime}

/** Create a group of components to select date and time */
class DateTimePicker( val dateLabel: DateType ):
   // disable dates that are before current date
   private val customeDayCellFactory: Callback[JFXDatePicker, JFXDateCell] =
      ( datePicker: JFXDatePicker ) =>
         new JFXDateCell:
            override def updateItem( item: LocalDate, empty: Boolean ): Unit =
               super.updateItem( item, empty )

               if item != null && item.isBefore( LocalDate.now )
               then setDisable( true )

   // list of hours for ChoiceBox
   private val hours = ObservableBuffer[String]() ++ ( 0 to 23 ).toArray.map( num =>
      String.format( "%02d" + ":00", num )
   )

   val date = new DatePicker:
      maxWidth = 125
      converter = Helper.dateStrConverter
      editable = false
      dayCellFactory = customeDayCellFactory

   val time = new ChoiceBox[String]:
      items = hours
      maxWidth = 70

   this.setDefaultValues()

   val container = GridPane()
   container.add( Label( s"$dateLabel Date" ), 0, 0 )
   container.add( date, 0, 1 )
   container.add( Label( s"$dateLabel Time" ), 1, 0 )
   container.add( time, 1, 1 )
   container.columnConstraints =
      Array( ColumnConstraints( 135 ), ColumnConstraints( 80 ), ColumnConstraints( 80 ) )

   /** Set default initial values for date and time pickers.<br>
     * Start date and time is set to one hour ahead of current local time to avoid issue with start time is before current time.<br>
     * End date and time is set to two hours ahead of current local time so that default duration is 1 hour
     */
   def setDefaultValues() =
      if dateLabel == Start then
         time.value = String.format( "%02d" + ":00", LocalDateTime.now.plusHours( 1 ).getHour )
         date.value = LocalDateTime.now().plusHours( 1 ).toLocalDate
      else
         time.value = String.format( "%02d" + ":00", LocalDateTime.now.plusHours( 2 ).getHour )
         date.value = LocalDateTime.now().plusHours( 2 ).toLocalDate
end DateTimePicker

enum DateType:
   case Start
   case End