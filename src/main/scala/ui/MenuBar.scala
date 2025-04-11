package ui

import scalafx.scene.control.{Menu, MenuBar, MenuItem}
import ui.AddItem.{addItemScroll, resetItemPage}
import ui.AddItemCategory.*
import ui.BodyContainer.bodyStack
import ui.util.Helper.displayPage
import ui.ItemList.{itemListPageScroll, renderItemListPage}
import ui.RentItem.{rentItemScrollContainer, resetSavedListPage}
import ui.ReportPage.{reportPageContainer, resetReportPage}
import ui.ReturnItem.{resetReturnItemPage, returnItemScrollContainer}
import ui.ViewRenter.{renterPageContainer, resetRenterPage}

object MenuBar:
   /** MENU BAR */
   // Menu to navigate pages
   private val navigationMenu = Menu( "Pages" )
   private val itemListMenuItem = MenuItem( "Items List" )
   private val calendarMenuItem = MenuItem( "Calendar" )
   private val reportsMenuItem = MenuItem( "Reports" )
   navigationMenu.getItems.addAll( itemListMenuItem, calendarMenuItem, reportsMenuItem )

   // Menu to perform actions
   private val actionsMenu = Menu( "Actions" )
   private val addItemCategoryMenuItem = MenuItem( "Manage Item Category" )
   private val addItemMenuItem = MenuItem( "Add Item" )
   private val returnItemMenuItem = MenuItem( "Return Item" )
   private val createBookingMenuItem = MenuItem( "Rent Item" )
   private val viewRenterMenuItem = MenuItem( "Manage Renter" )
   actionsMenu.getItems.addAll( createBookingMenuItem,
                                returnItemMenuItem,
                                addItemCategoryMenuItem,
                                addItemMenuItem,
                                viewRenterMenuItem
   )

   val menuBar = new MenuBar:
      menus = Array( navigationMenu, actionsMenu )

   /** MENU BAR ACTIONS */

   // Add actions for menu items to change pages
   itemListMenuItem.onAction = ( ( event ) =>
      displayPage( bodyStack, itemListPageScroll )
      itemListPageScroll.setContent( renderItemListPage )
   )

   calendarMenuItem.onAction = ( ( event ) =>
      val newCalendar = CalendarPage()
      if bodyStack.children.size == 9 then bodyStack.children.remove( 8 )
      bodyStack.children.add( newCalendar.calendarContainer )
      displayPage( bodyStack, newCalendar.calendarContainer )
   )

   reportsMenuItem.onAction = ( ( e ) =>
      displayPage( bodyStack, reportPageContainer )
      resetReportPage()
   )

   addItemCategoryMenuItem.onAction = ( ( e ) =>
      displayPage( bodyStack, addItemCatContainer )
      resetItemCatPage()
   )

   addItemMenuItem.onAction = ( ( e ) =>
      displayPage( bodyStack, addItemScroll )
      resetItemPage()
   )

   returnItemMenuItem.onAction = ( ( e ) =>
      displayPage( bodyStack, returnItemScrollContainer )
      resetReturnItemPage()
   )

   createBookingMenuItem.onAction = ( ( e ) =>
      displayPage( bodyStack, rentItemScrollContainer )
      resetSavedListPage()
   )

   viewRenterMenuItem.onAction = ( ( e ) =>
      displayPage( bodyStack, renterPageContainer )
      resetRenterPage()
   )