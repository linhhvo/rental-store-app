package ui

import logic.{DuplicateItemCategory, ItemCategory}
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.geometry.Insets
import scalafx.scene.control.*
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.*
import scalafx.scene.paint.Color
import scalafx.scene.{Node, Scene}

object Main extends JFXApp3:
   def start() =
      val bodyHeight = 500
      val bodyWidth = 500
      val dataFilePath = getClass.getResource( "/data/" ).getPath
//      val imageFilePath = "src/main/resources/images"
      stage = new PrimaryStage:
         title = "Cookie Rental Company"
         height = bodyHeight
         width = bodyWidth
         resizable = true

      // Menu to navigate pages
      val navigationMenu = Menu( "Pages" )

      val itemListMenuItem = MenuItem( "Items List" )
      val calendarMenuItem = MenuItem( "Calendar" )
      val reportsMenuItem = MenuItem( "Reports" )
      navigationMenu.getItems.addAll( itemListMenuItem, calendarMenuItem, reportsMenuItem )

      // Menu to perform actions
      val actionsMenu = new Menu( "Actions" )
      val addItemCategoryMenuItem = MenuItem( "Add Item Category" )
      val addItemMenuItem = MenuItem( "Add Item" )
      val returnItemMenuItem = MenuItem( "Return Item" )
      actionsMenu.getItems.addAll( addItemCategoryMenuItem, addItemMenuItem, returnItemMenuItem )

      val menuBar = new MenuBar:
         menus = Array( navigationMenu, actionsMenu )

      // Retrieve items data from json file
      val jsonItems = ujson.read( os.read( os.Path( dataFilePath + "items.json" ) ) )
      val itemList = jsonItems.arr

      // Create a container for each item
      val itemElements = itemList.map( item =>
         val itemImage =
            getClass.getResourceAsStream( "/images/" + item( "images" )( 0 ).str )
         val itemImageView = ImageView( Image( itemImage, 0, 200, true, true ) )
         new VBox():
            children = Array( itemImageView, Label( item( "name" ).str ) )
            padding = Insets( 10 )
            border = Border.stroke( Color.Gray )
      )

      // Create a pane for the item list
      val itemListPageContainer = new FlowPane()
      itemListPageContainer.setHgap( 15 )
      itemListPageContainer.setVgap( 15 )
      itemListPageContainer.children = itemElements
      itemListPageContainer.setPadding( Insets( 20 ) )

      val itemListPageScroll = ScrollPane()
      itemListPageScroll.setContent( itemListPageContainer )
      itemListPageScroll.fitToWidth = true
      itemListPageScroll.fitToHeight = true

      // Pane for calendar page
      val calendarPageContainer = new HBox( Label( "calendar goes here" ) )
      calendarPageContainer.margin = Insets( 20 )
      calendarPageContainer.setVisible( false )

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
      // Add action for add button
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

      // Create a pane to stack different views. The view changes based on selected menu option
      val bodyStack = StackPane()
      bodyStack.getChildren.addAll( itemListPageScroll, calendarPageContainer, addItemCatContainer )

      // Add actions for menu items to change pages
      itemListMenuItem.onAction = ( ( event ) => this.displayPage( bodyStack, itemListPageScroll ) )
      calendarMenuItem.onAction =
         ( ( event ) => this.displayPage( bodyStack, calendarPageContainer ) )
      addItemCategoryMenuItem.onAction = ( ( e ) =>
         this.displayPage( bodyStack, addItemCatContainer )
         itemCatTextField.clear()
         actionMsg.setText( null )
      )

      // Root pane
      val root = new BorderPane {
         top = menuBar
         center = bodyStack
      }
      stage.scene = Scene( parent = root )

   end start

   // Function to show a page in the stack pane while hiding the rest
   private def displayPage( parentContainer: Pane, targetPage: Node ) =
      parentContainer.children.foreach( page => page.setVisible( false ) )
      targetPage.setVisible( true )

end Main