package ui

import scalafx.scene.layout.StackPane
import ui.AddItem.addItemScroll
import ui.AddItemCategory.addItemCatContainer
import ui.ItemList.{itemListPageScroll, itemPageContainer}
import ui.RentItem.rentItemScrollContainer
import ui.ReturnItem.returnItemScrollContainer
import ui.ViewRenter.renterPageContainer

object BodyContainer:
   /** BODY CONTAINER */

   // Create a pane to stack different views. The view changes based on selected menu option
   val bodyStack = StackPane()
   bodyStack.children = Array(
       itemListPageScroll,
       addItemCatContainer,
       itemPageContainer,
       addItemScroll,
       rentItemScrollContainer,
       renterPageContainer,
       returnItemScrollContainer
   )