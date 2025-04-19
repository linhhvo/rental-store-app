package ui

import com.calendarfx.model.*
import com.calendarfx.model.Calendar.Style
import com.calendarfx.view.SourceView
import com.calendarfx.view.page.{MonthPage, WeekPage}
import logic.RentalStore.{activeItems, bookings}
import scalafx.scene.control.ControlIncludes.jfxControl2sfx
import scalafx.scene.control.TabPane.TabClosingPolicy.Unavailable
import scalafx.scene.control.{Tab, TabPane}
import scalafx.scene.layout.{ColumnConstraints, GridPane, RowConstraints}

class CalendarPage:
   /** CALENDAR PAGE */

   // initialize calendar views
   private val monthView = MonthPage()
   private val weekView = WeekPage()

   monthView.bind( weekView, true )

   // add a calendar source to store item reservations
   private val rentalReservations = CalendarSource( "Reservations" )

   // create a calendar for each item
   private val itemCalendarList = activeItems.map( i => i.itemId -> new Calendar( i.name ) ).toMap

   // assign different color styles to each calendar
   private var styleNum = 1
   itemCalendarList.foreach( c =>
      c._2.setStyle( Style.values()( styleNum % 7 ) )
      styleNum += 1
      rentalReservations.getCalendars.add( c._2 )
   )

   // set rental reservations as the only calendar source; remove the Default calendar source
   monthView.getCalendarSources.setAll( rentalReservations )

   /* Add reservations to each item calendar:
    * - Filter the bookings that contain the item
    * - Group the bookings by start and end time
    * - Calculate the total rented quantity for the item if multiple bookings have the same date
    * range
    * - Create a calendar entry for each booking date range and add to the calendar */
   itemCalendarList.foreach( ( itemId, cal ) =>
      bookings
         .filter( b => b.items.exists( _.itemId == itemId ) )
         .groupMapReduce( b => ( b.bookingStartTime, b.bookingEndTime ) )( b =>
            b.items.find( _.itemId == itemId ).map( _.rentQuantity ).getOrElse( 0 )
         )( _ + _ )
         .foreach( ( key, value ) =>
            val entry = new Entry( s"$value unit(s)", Interval( key._1, key._2 ) )
            entry.setCalendar( cal )
         )
   )

   // disable functionalities to modify calendar entries directly on GUI
   itemCalendarList.foreach( ( id, cal ) => cal.setReadOnly( true ) )

   // sidebar to show a list of rental items
   private val sideBar = SourceView()
   sideBar.bind( monthView )

   private val weekTab = new Tab:
      text = "Week"
      content = weekView

   private val monthTab = new Tab:
      text = "Month"
      content = monthView

   // tab pane to toggle between week view and month view
   private val calendarTabPane = new TabPane:
      tabs = Seq( weekTab, monthTab )
      tabClosingPolicy = Unavailable

   // set column and row contraints
   private val column0 = new ColumnConstraints:
      percentWidth = 20

   private val column1 = new ColumnConstraints:
      percentWidth = 80

   private val row0 = new RowConstraints:
      percentHeight = 100

   // overall container of the page
   val calendarContainer = GridPane()
   calendarContainer.add( sideBar, 0, 0 )
   calendarContainer.add( calendarTabPane, 1, 0 )

   calendarContainer.columnConstraints = Array( column0, column1 )
   calendarContainer.rowConstraints = Array( row0 )

end CalendarPage