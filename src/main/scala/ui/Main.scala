package ui

import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.layout.*
import scalafx.scene.{Node, Scene}
import ui.AddItemCategory.addItemCatContainer
import ui.Calendar.calendarPageContainer
import ui.ItemList.{itemListPageScroll, itemPageContainer}
import ui.MenuBar.menuBar

object Main extends JFXApp3:
   def start() =
      val bodyHeight = 700
      val bodyWidth = 700
      stage = new PrimaryStage:
         title = "Cookie Rental Company"
         height = bodyHeight
         width = bodyWidth
         resizable = true

      /** BODY CONTAINER */

      // Create a pane to stack different views. The view changes based on selected menu option
      val bodyStack = StackPane()
      bodyStack.getChildren.addAll( itemListPageScroll,
                                    calendarPageContainer,
                                    addItemCatContainer,
                                    itemPageContainer
      )

      /** ROOT */
      val root = new BorderPane {
         top = menuBar
         center = bodyStack
      }
      stage.scene = Scene( parent = root )

   end start

end Main