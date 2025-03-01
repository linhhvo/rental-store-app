package ui

import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.*
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.*
import scalafx.scene.paint.Color

object Main extends JFXApp3:
   def start() =
      val bodyHeight = 500
      val bodyWidth = 500
      val dataFilePath = "src/main/resources/data"
//      val imageFilePath = "src/main/resources/images"
      stage = new PrimaryStage:
         title = "Cookie Rental Company"
         height = bodyHeight
         width = bodyWidth
         resizable = true

      // Retrieve items data from json file
      val jsonItems = ujson.read( os.read( os.pwd / os.RelPath( dataFilePath ) / "items.json" ) )
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

      // Menu to navigate pages
      val navigationMenu = Menu( "Pages" )

      val itemListMenuItem = MenuItem( "Items List" )
      itemListMenuItem.onAction = ( ( event ) =>
         itemListPageScroll.setVisible( true )
         calendarPageContainer.setVisible( false )
      )

      val calendarMenuItem = MenuItem( "Calendar" )
      calendarMenuItem.onAction = ( ( event ) =>
         calendarPageContainer.setVisible( true )
         itemListPageScroll.setVisible( false )
      )
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

      // Create a pane to stack different views. The view changes based on selected menu option
      val bodyStack = StackPane()
      bodyStack.getChildren.addAll( itemListPageScroll, calendarPageContainer )

      // Root pane
      val root = new BorderPane {
         top = menuBar
         center = bodyStack
      }
      stage.scene = Scene( parent = root )

   end start
end Main