package ui

import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.layout.*
import ui.BodyContainer.bodyStack
import ui.MenuBar.menuBar

object Main extends JFXApp3:
   def start() =
      val bodyHeight = 950
      val bodyWidth = 1100
      stage = new PrimaryStage:
         title = "Cookie Rental Company"
         height = bodyHeight
         width = bodyWidth
         resizable = true

      /** ROOT */
      val root = new BorderPane {
         top = menuBar
         center = bodyStack
      }
      stage.scene = Scene( parent = root )

   end start

end Main