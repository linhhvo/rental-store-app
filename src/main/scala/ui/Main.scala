package ui

import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.layout.*
import scalafx.scene.Scene
import ui.BodyContainer.bodyStack
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


      /** ROOT */
      val root = new BorderPane {
         top = menuBar
         center = bodyStack
      }
      stage.scene = Scene( parent = root )

   end start

end Main