package ui

import scalafx.scene.control.{Menu, MenuBar, MenuItem}
import ui.AddItem.{addItemScroll, resetItemPage}
import ui.AddItemCategory.*
import ui.BodyContainer.bodyStack
import ui.CalendarPage.calendarContainer
import ui.Helper.displayPage
import ui.ItemList.{itemListPageScroll, renderItemListPage}
import ui.RentItem.{rentItemScrollContainer, resetSavedListPage}
import ui.ViewRenter.renterPageContainer

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

   calendarMenuItem.onAction = ( ( event ) => displayPage( bodyStack, calendarContainer ) )

   addItemCategoryMenuItem.onAction = ( ( e ) =>
      displayPage( bodyStack, addItemCatContainer )
      resetItemCatPage()
   )

   addItemMenuItem.onAction = ( ( e ) =>
      displayPage( bodyStack, addItemScroll )
      resetItemPage()
   )

   createBookingMenuItem.onAction = ( ( e ) =>
      displayPage( bodyStack, rentItemScrollContainer )
      resetSavedListPage()
   )

   viewRenterMenuItem.onAction = ( ( e ) => displayPage( bodyStack, renterPageContainer ) )