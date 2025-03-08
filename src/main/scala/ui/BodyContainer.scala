package ui

import scalafx.scene.layout.StackPane
import ui.AddItemCategory.addItemCatContainer
import ui.Calendar.calendarPageContainer
import ui.ItemList.{itemListPageScroll, itemPageContainer}

object BodyContainer:
   /** BODY CONTAINER */

   // Create a pane to stack different views. The view changes based on selected menu option
   val bodyStack = StackPane()
   bodyStack.getChildren.addAll( itemListPageScroll,
                                 calendarPageContainer,
                                 addItemCatContainer,
                                 itemPageContainer
   )
