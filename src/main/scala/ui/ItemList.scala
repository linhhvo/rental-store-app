package ui

import logic.RentalStore.getAvailableItems
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, Label, ScrollPane}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{Border, FlowPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.Font
import ui.ItemDetails.displayItemPage
import upickle.default.*

object ItemList:
   /** ITEM LIST PAGE */

   // Retrieve items data from json file
//   val jsonItems = IOHandler.getDataFromFile( "items" )
   val itemList = getAvailableItems

   // Create a container for each item
   val itemPageContainer = ScrollPane()
   val itemElements = itemList.map( item =>
      val itemImage =
         getClass.getResourceAsStream( "/images/" + item( "images" ).str )
      val itemImageView = ImageView( Image( itemImage, 0, 200, true, true ) )
      val itemNameLabel = Label( item( "name" ).str )
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

   itemPageContainer.setVisible( false )

   // Create a pane for the item list
   val itemListPageContainer = FlowPane()
   itemListPageContainer.setHgap( 15 )
   itemListPageContainer.setVgap( 15 )
   itemListPageContainer.children = itemElements
   itemListPageContainer.setPadding( Insets( 20 ) )

   val itemListPageScroll = ScrollPane()
   itemListPageScroll.setContent( itemListPageContainer )
   itemListPageScroll.fitToWidth = true
   itemListPageScroll.fitToHeight = true