package ui

import logic.RentalStore.ownedItems
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, Label, ScrollPane}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{Border, FlowPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.Font
import ui.ItemDetails.displayItemPage

import java.io.FileInputStream

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
      val itemElements = ownedItems.map( item =>
         val itemImage = FileInputStream("src/main/resources/images/" + item.image)
         val itemImageView = ImageView( Image( itemImage, 0, 200, true, true ) )
         val itemNameLabel = Label( item.name )
         itemNameLabel.font = Font( "Arial", 18 )
         val viewItemBtn = Button( "View Item" )
         viewItemBtn.onAction = ( ( e ) =>
            itemPageContainer.setContent( displayItemPage( item ) )
            itemPageContainer.setVisible( true )
         )

         new VBox( 5 ):
            children = Array( itemImageView, itemNameLabel, viewItemBtn )
            padding = Insets( 10 )
            border = Border.stroke( Color.Gray )
      )

      // Create a pane for the item list
      val itemListPageContainer = FlowPane()
      itemListPageContainer.setHgap( 15 )
      itemListPageContainer.setVgap( 15 )
      itemListPageContainer.children = itemElements
      itemListPageContainer.setPadding( Insets( 20 ) )

      itemListPageContainer