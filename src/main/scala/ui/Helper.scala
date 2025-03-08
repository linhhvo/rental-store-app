package ui

import scalafx.scene.Node
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, ButtonType}
import scalafx.scene.layout.Pane

object Helper:
   // Function to show a page in the stack pane while hiding the rest
   def displayPage( parentContainer: Pane, targetPage: Node ) =
      parentContainer.children.foreach( page => page.setVisible( false ) )
      targetPage.setVisible( true )

   def addAlert =
      new Alert( AlertType.Confirmation ):
         title = "Alert"
         headerText = ""
         buttonTypes = Array( ButtonType.OK )
