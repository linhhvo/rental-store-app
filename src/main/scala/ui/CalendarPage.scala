package ui

import com.calendarfx.model.*
import com.calendarfx.model.Calendar.Style
import com.calendarfx.view.SourceView
import com.calendarfx.view.page.{MonthPage, WeekPage}
import logic.RentalStore.{activeItems, bookings}
import scalafx.scene.control.ControlIncludes.jfxControl2sfx
import scalafx.scene.control.{Tab, TabPane}
import scalafx.scene.layout.{ColumnConstraints, GridPane, RowConstraints}

class CalendarPage:
   /** CALENDAR PAGE */

   // initialize calendar views
   private val monthView = MonthPage()
   private val weekView = WeekPage()

   monthView.bind( weekView, true )

   // add calendar source to store item reservations
   private val rentalReservations = CalendarSource( "Reservations" )

   // create calendar for each item
   private val itemCalendar = activeItems.map( i => i.itemId -> new Calendar( i.name ) ).toMap
   private var styleNum = 1
   itemCalendar.foreach( c =>
      c._2.setStyle( Style.values()( styleNum % 7 ) )
      styleNum += 1
      rentalReservations.getCalendars.add( c._2 )
   )

   // set rental reservations as the only calendar source; remove Default calendar source
   monthView.getCalendarSources.setAll( rentalReservations )

   // add calendar entries
   bookings.foreach( b =>
      // add entry to each item calendar
      val matchedCalendars =
         b.items.map( bi => ( itemCalendar.getOrElse( bi.itemId, null ), bi.rentQuantity ) )
      matchedCalendars.foreach( ( calendar, quantity ) =>
         // create new entry from each booking
         val bookingEntry = new Entry[String]( s"$quantity units" )
         bookingEntry.setInterval( Interval( b.bookingStartTime, b.bookingEndTime ) )
         bookingEntry.setCalendar( calendar )
      )
   )

   // sidebar to show list of rental items
   private val sideBar = SourceView()
   sideBar.bind( monthView )

   // tab pane to toggle between week view and month view

   private val weekTab = new Tab:
      text = "Week"
      content = weekView

   private val monthTab = new Tab:
      text = "Month"
      content = monthView

   private val calendarTabPane = new TabPane:
      tabs = Seq( weekTab, monthTab )

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