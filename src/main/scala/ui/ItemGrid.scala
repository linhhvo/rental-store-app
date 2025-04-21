package ui

import logic.Inventory.activeItems
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, Label, ScrollPane}
import scalafx.scene.layout.{Border, FlowPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.Font
import ui.ItemDetails.displayItemPage
import ui.util.Helper.getItemImageView

object ItemGrid:

   // Create a page for each item details and actions
   val itemPageContainer = new ScrollPane:
      visible = false

   // Container for the list of items (landing page itself)
   val itemGridPageScroll = new ScrollPane:
      content = renderItemGridPage
      fitToWidth = true
      fitToHeight = true

   /** Create UI components for each item and display in a grid format */
   def renderItemGridPage =
      val itemElements = activeItems.map( item =>
         val itemImageView = getItemImageView( item )

         val itemNameLabel = new Label:
            text = item.name
            font = Font( "Arial", 18 )

         // button to open item details page
         val viewItemBtn = new Button:
            text = "View Item"
            onAction = ( ( e ) =>
               itemPageContainer.content = displayItemPage( item )
               itemPageContainer.visible = true
            )

         // group all details into a grid
         new VBox( 5 ):
            children = Array( itemImageView, itemNameLabel, viewItemBtn )
            padding = Insets( 10 )
            border = Border.stroke( Color.Gray )
      )

      // Create a container for the page
      val itemGridPageContainer = new FlowPane:
         hgap = 15
         vgap = 15
         children = itemElements
         padding = Insets( 20 )

      itemGridPageContainer