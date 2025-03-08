package ui

import scalafx.scene.control.{Menu, MenuBar, MenuItem}
import ui.AddItem.addItemContainer
import ui.AddItemCategory.{actionMsg, addItemCatContainer, itemCatTextField}
import ui.BodyContainer.bodyStack
import ui.Calendar.calendarPageContainer
import ui.Helper.displayPage
import ui.ItemList.itemListPageScroll

object MenuBar:
   /** MENU BAR */
   // Menu to navigate pages
   val navigationMenu = Menu( "Pages" )
   val itemListMenuItem = MenuItem( "Items List" )
   val calendarMenuItem = MenuItem( "Calendar" )
   val reportsMenuItem = MenuItem( "Reports" )
   navigationMenu.getItems.addAll( itemListMenuItem, calendarMenuItem, reportsMenuItem )

   // Menu to perform actions
   val actionsMenu = Menu( "Actions" )
   val addItemCategoryMenuItem = MenuItem( "Add Item Category" )
   val addItemMenuItem = MenuItem( "Add Item" )
   val returnItemMenuItem = MenuItem( "Return Item" )
   actionsMenu.getItems.addAll( addItemCategoryMenuItem, addItemMenuItem, returnItemMenuItem )

   val menuBar = new MenuBar:
      menus = Array( navigationMenu, actionsMenu )

   /** MENU BAR ACTIONS */

   // Add actions for menu items to change pages
   itemListMenuItem.onAction = ( ( event ) => displayPage( bodyStack, itemListPageScroll ) )
   calendarMenuItem.onAction = ( ( event ) => displayPage( bodyStack, calendarPageContainer ) )
   addItemCategoryMenuItem.onAction = ( ( e ) =>
      displayPage( bodyStack, addItemCatContainer )
      itemCatTextField.clear()
      actionMsg.setText( null )
   )
   addItemMenuItem.onAction = ( ( e ) => displayPage( bodyStack, addItemContainer ) )