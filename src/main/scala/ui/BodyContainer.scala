package ui

import scalafx.scene.layout.StackPane
import ui.AddItem.addItemScroll
import ui.AddItemCategory.addItemCatContainer
import ui.ItemGrid.{itemGridPageScroll, itemPageContainer}
import ui.RentItem.rentItemScrollContainer
import ui.ReportPage.reportPageContainer
import ui.ReturnItem.returnItemScrollContainer
import ui.ViewRenter.renterPageContainer

object BodyContainer:
   // Create a pane to stack different views. The view changes based on selected menu option
   val bodyStack = new StackPane:
      children = Array(
         itemGridPageScroll,
         addItemCatContainer,
         itemPageContainer,
         addItemScroll,
         rentItemScrollContainer,
         renterPageContainer,
         returnItemScrollContainer,
         reportPageContainer
         )