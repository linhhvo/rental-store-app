package ui

import logic.{DuplicateItemCategory, IOHandler, Item, ItemCategory}
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.geometry.Insets
import scalafx.scene.control.*
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.*
import scalafx.scene.paint.Color
import scalafx.scene.text.Font
import scalafx.scene.{Node, Scene}
import ui.Helper.*
import upickle.default.*

object Main extends JFXApp3:
   def start() =
      val bodyHeight = 700
      val bodyWidth = 700
      stage = new PrimaryStage:
         title = "Cookie Rental Company"
         height = bodyHeight
         width = bodyWidth
         resizable = true

      /** MENU BAR */

      // Menu to navigate pages
      val navigationMenu = Menu( "Pages" )
      val itemListMenuItem = MenuItem( "Items List" )
      val calendarMenuItem = MenuItem( "Calendar" )
      val reportsMenuItem = MenuItem( "Reports" )
      navigationMenu.getItems.addAll( itemListMenuItem, calendarMenuItem, reportsMenuItem )

      // Menu to perform actions
      val actionsMenu = Menu( "Actions" )
      val addItemCategoryMenuItem = MenuItem( "Add Item Category" )
      val addItemMenuItem = MenuItem( "Add Item" )
      val returnItemMenuItem = MenuItem( "Return Item" )
      actionsMenu.getItems.addAll( addItemCategoryMenuItem, addItemMenuItem, returnItemMenuItem )

      val menuBar = new MenuBar:
         menus = Array( navigationMenu, actionsMenu )

      /** ITEM LIST PAGE */

      // Retrieve items data from json file
      val jsonItems = IOHandler.getDataFromFile( "items" )
      val itemList = jsonItems.arr

      // Create a container for each item
      val itemPageContainer = ScrollPane()
      val itemElements = itemList.map( item =>
         val itemImage =
            getClass.getResourceAsStream( "/images/" + item( "images" )( 0 ).str )
         val itemImageView = ImageView( Image( itemImage, 0, 200, true, true ) )
         val itemNameLabel = Label( item( "name" ).str )
         itemNameLabel.font = Font( "Arial", 18 )
         val viewItemBtn = Button( "View Item" )
         viewItemBtn.onAction = ( ( e ) =>
            itemPageContainer.setContent( displayItemPage( read[Item]( item ) ) )
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

      /** CALENDAR PAGE */

      // Pane for calendar page
      val calendarPageContainer = HBox( Label( "calendar goes here" ) )
      calendarPageContainer.margin = Insets( 20 )
      calendarPageContainer.setVisible( false )

      /** ADD ITEM CATEGORY PAGE */

      // Pane for adding item category
      val itemCatTextField = TextField()
      itemCatTextField.maxWidth = 200
      val addItemCatBtn = Button( "Add Category" )
      val actionMsg = Label( null )
      actionMsg.textFill = Color.Red
      val addItemCatContainer =
         VBox( Label( "Enter new item category" ), itemCatTextField, addItemCatBtn, actionMsg )
      addItemCatContainer.margin = Insets( 20 )
      addItemCatContainer.spacing = 10
      addItemCatContainer.setVisible( false )

      // Add action for add item category button
      addItemCatBtn.onAction = (
          ( e ) =>
             if itemCatTextField.getText.nonEmpty then
                try
                   ItemCategory.addItemCategory( itemCatTextField.getText )
                   actionMsg.setText( s"Category ${itemCatTextField.getText} has been added." )
                   itemCatTextField.clear()
                catch
                   case DuplicateItemCategory( errorMsg ) =>
                      actionMsg.setText( errorMsg )
                      itemCatTextField.clear()
             else actionMsg.setText( "Category is empty." )
      )

      /** BODY CONTAINER */

      // Create a pane to stack different views. The view changes based on selected menu option
      val bodyStack = StackPane()
      bodyStack.getChildren.addAll( itemListPageScroll,
                                    calendarPageContainer,
                                    addItemCatContainer,
                                    itemPageContainer
      )

      /** MENU BAR ACTIONS */

      // Add actions for menu items to change pages
      itemListMenuItem.onAction = ( ( event ) => displayPage( bodyStack, itemListPageScroll ) )
      calendarMenuItem.onAction = ( ( event ) => displayPage( bodyStack, calendarPageContainer ) )
      addItemCategoryMenuItem.onAction = ( ( e ) =>
         displayPage( bodyStack, addItemCatContainer )
         itemCatTextField.clear()
         actionMsg.setText( null )
      )

      /** ROOT */
      val root = new BorderPane {
         top = menuBar
         center = bodyStack
      }
      stage.scene = Scene( parent = root )

   end start

end Main