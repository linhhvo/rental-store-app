package ui

import com.calendarfx.model.*
import com.calendarfx.model.Calendar.Style
import com.calendarfx.view.SourceView
import com.calendarfx.view.page.{MonthPage, WeekPage}
import logic.RentalStore.{bookings, ownedItems}
import scalafx.scene.control.ControlIncludes.jfxControl2sfx
import scalafx.scene.control.{Tab, TabPane}
import scalafx.scene.layout.{ColumnConstraints, GridPane, RowConstraints}

object CalendarPage:
   /** CALENDAR PAGE */

   // initialize calendar views
   val monthView = MonthPage()
   val weekView = WeekPage()

   monthView.bind( weekView, true )

   // add calendar source to store item reservations
   val rentalReservations = CalendarSource( "Reservations" )

   // create calendar for each item
   val itemCalendar = ownedItems.map( i => i.itemId -> new Calendar( i.name ) ).toMap
   var styleNum = 1
   itemCalendar.foreach( c =>
      c._2.setStyle( Style.values()(styleNum % 7) )
      styleNum += 1
      rentalReservations.getCalendars.add( c._2 )
   )

   // add calendar source to calendar view
   monthView.getCalendarSources.addAll( rentalReservations )
   // set rental reservations as the only calendar source; remove Default calendar source
   monthView.getCalendarSources.setAll( rentalReservations )

   // add calendar entries
   bookings.foreach( b =>
      // create new entry from each booking
      val bookingEntry = new Entry[String]( b.renter )
      bookingEntry.setInterval( Interval( b.bookingStartTime, b.bookingEndTime ) )
      // add entry to each item calendar
      val matchedCalendars = b.items.flatMap( bi => itemCalendar.get( bi.itemId ) )
      matchedCalendars.foreach( c => c.addEntry( bookingEntry ) )
   )

   // sidebar to show list of rental items
   val sideBar = SourceView()
   sideBar.bind( monthView )

   // tab pane to toggle between week view and month view
   val calendarTabPane = TabPane()
   val weekTab = Tab()
   weekTab.text = "Week"
   weekTab.content = weekView

   val monthTab = Tab()
   monthTab.text = "Month"
   monthTab.content = monthView

   calendarTabPane.tabs = Seq( weekTab, monthTab )

   // overall container of the page
   val calendarContainer = GridPane()
   calendarContainer.add( sideBar, 0, 0 )
   calendarContainer.add( calendarTabPane, 1, 0 )
   calendarContainer.visible = false

   // set column and row contraints
   val column0 = new ColumnConstraints:
      percentWidth = 20

   val column1 = new ColumnConstraints:
      percentWidth = 80

   val row0 = new RowConstraints:
      percentHeight = 100

   calendarContainer.columnConstraints = Array( column0, column1 )
   calendarContainer.rowConstraints = Array( row0 )