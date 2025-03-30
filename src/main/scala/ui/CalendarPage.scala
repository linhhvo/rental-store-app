package ui

import com.calendarfx.model.*
import com.calendarfx.view.SourceView
import com.calendarfx.view.page.{MonthPage, WeekPage}
import logic.RentalStore.ownedItems
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
   ownedItems.foreach( i =>
      val itemCalendar = Calendar( i.name )
//      itemCalendar.setReadOnly( true )
      rentalReservations.getCalendars.add( itemCalendar )
   )

   // add calendar source to calendar view
   monthView.getCalendarSources.addAll( rentalReservations )
   // set rental reservations as the only calendar source; remove Default calendar source
   monthView.getCalendarSources.setAll( rentalReservations )

   // sidebar
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

   // set column contraints
   val column0 = new ColumnConstraints:
      percentWidth = 20

   val column1 = new ColumnConstraints:
      percentWidth = 80

   val row0 = new RowConstraints:
      percentHeight = 100

   calendarContainer.columnConstraints = Array( column0, column1 )
   calendarContainer.rowConstraints = Array( row0 )