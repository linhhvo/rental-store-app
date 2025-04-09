package ui

import logic.RentalStore.activeItems
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, Label, ScrollPane}
import scalafx.scene.layout.{Border, FlowPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.Font
import ui.Helper.getItemImageView
import ui.ItemDetails.displayItemPage

object ItemList:
   /** ITEM LIST PAGE */

   // Create a page for each item details and actions
   val itemPageContainer = ScrollPane()
   itemPageContainer.setVisible( false )

   // Container for the list of items (landing page itself)
   val itemListPageScroll = ScrollPane()
   itemListPageScroll.setContent( renderItemListPage )
   itemListPageScroll.fitToWidth = true
   itemListPageScroll.fitToHeight = true

   def renderItemListPage =
      // Create UI element for each item
      val itemElements = activeItems.map( item =>
         // show item image
         val itemImageView = getItemImageView( item )

         // show item name
         val itemNameLabel = Label( item.name )
         itemNameLabel.font = Font( "Arial", 18 )

         // button to open item details page
         val viewItemBtn = Button( "View Item" )
         viewItemBtn.onAction = ( ( e ) =>
            itemPageContainer.setContent( displayItemPage( item ) )
            itemPageContainer.setVisible( true )
         )

         // group all details into a container
         new VBox( 5 ):
            children = Array( itemImageView, itemNameLabel, viewItemBtn )
            padding = Insets( 10 )
            border = Border.stroke( Color.Gray )
      )

      // Create a container for the item list
      val itemListPageContainer = FlowPane()
      itemListPageContainer.setHgap( 15 )
      itemListPageContainer.setVgap( 15 )
      itemListPageContainer.children = itemElements
      itemListPageContainer.setPadding( Insets( 20 ) )

      itemListPageContainer