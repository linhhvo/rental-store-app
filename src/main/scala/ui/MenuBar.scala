package ui

import scalafx.scene.control.{Menu, MenuBar, MenuItem}
import ui.AddItem.{addItemScroll, resetItemPage}
import ui.AddItemCategory.*
import ui.BodyContainer.bodyStack
import ui.ItemGrid.{itemGridPageScroll, renderItemGridPage}
import ui.RentItem.{rentItemScrollContainer, resetSavedListPage}
import ui.ReportPage.{reportPageContainer, resetReportPage}
import ui.ReturnItem.{resetReturnItemPage, returnItemScrollContainer}
import ui.ViewRenter.{renterPageContainer, resetRenterPage}
import ui.util.Helper.displayPage

object MenuBar:
   /* MENU BAR */

   // Menu to navigate pages
   private val itemGridMenuItem = MenuItem( "Items List" )
   private val calendarMenuItem = MenuItem( "Calendar" )
   private val reportsMenuItem = MenuItem( "Reports" )

   private val navigationMenu = new Menu:
      text = "Pages"
      items = Array( itemGridMenuItem, calendarMenuItem, reportsMenuItem )

   // Menu to perform actions
   private val addItemCategoryMenuItem = MenuItem( "Manage Item Category" )
   private val addItemMenuItem = MenuItem( "Add Item" )
   private val returnItemMenuItem = MenuItem( "Return Item" )
   private val createBookingMenuItem = MenuItem( "Rent Item" )
   private val viewRenterMenuItem = MenuItem( "Manage Renter" )

   private val actionsMenu = new Menu:
      text = "Actions"
      items = Array(
          createBookingMenuItem,
          returnItemMenuItem,
          addItemCategoryMenuItem,
          addItemMenuItem,
          viewRenterMenuItem
      )

   val menuBar = new MenuBar:
      menus = Array( navigationMenu, actionsMenu )

   /* MENU BAR ACTIONS */

   // display item grid page
   itemGridMenuItem.onAction = ( ( event ) =>
      displayPage( bodyStack, itemGridPageScroll )
      itemGridPageScroll.setContent( renderItemGridPage )
   )

   // display availability calendar page
   calendarMenuItem.onAction = ( ( event ) =>
      val newCalendar = CalendarPage()
      if bodyStack.children.size == 9 then bodyStack.children.remove( 8 )
      bodyStack.children.add( newCalendar.calendarContainer )
      displayPage( bodyStack, newCalendar.calendarContainer )
   )

   // display summary reports page
   reportsMenuItem.onAction = ( ( e ) =>
      displayPage( bodyStack, reportPageContainer )
      resetReportPage()
   )

   // display item category page
   addItemCategoryMenuItem.onAction = ( ( e ) =>
      displayPage( bodyStack, addItemCatContainer )
      resetItemCatPage()
   )

   // display form to add a new item
   addItemMenuItem.onAction = ( ( e ) =>
      displayPage( bodyStack, addItemScroll )
      resetItemPage()
   )

   // display form to return items
   returnItemMenuItem.onAction = ( ( e ) =>
      displayPage( bodyStack, returnItemScrollContainer )
      resetReturnItemPage()
   )

   // display form to confirm and create a new rental booking
   createBookingMenuItem.onAction = ( ( e ) =>
      displayPage( bodyStack, rentItemScrollContainer )
      resetSavedListPage()
   )

   // display renter page
   viewRenterMenuItem.onAction = ( ( e ) =>
      displayPage( bodyStack, renterPageContainer )
      resetRenterPage()
   )