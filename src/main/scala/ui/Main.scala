package ui

import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Label, Menu, MenuBar, MenuItem}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.*
import scalafx.scene.paint.Color

object Main extends JFXApp3:
   def start() =
      val dataFilePath = "src/main/resources/data"
      val imageFilePath = "src/main/resources/images"
      stage = new PrimaryStage:
         title = "Cookie Rental Company"
         height = 1000
         width = 1000
         resizable = true
      // Menu bar
      val navigation = new Menu( "Navigation" ):
         items = Array( MenuItem( "Browse Items" ),
                        MenuItem( "Return" ),
                        MenuItem( "Calendar" ),
                        MenuItem( "Reports" )
         )
      val menuBar = new MenuBar:
         menus = Array( navigation )

      // Retrieve items data from json file
      val jsonItems = ujson.read( os.read( os.pwd / os.RelPath( dataFilePath ) / "items.json" ) )
      val itemList = jsonItems.arr

      // Create a container for each item
      val itemChildren = itemList.map( item =>
         val itemImage =
            getClass.getResourceAsStream( "/images/" + item( "images" )( 0 ).str )
         val itemImageView = ImageView( Image( itemImage, 0, 200, true, true ) )
         new VBox():
            children = Array( itemImageView, Label( item( "name" ).str ) )
            padding = Insets( 10 )
            border = Border.stroke( Color.Black )
      )

      // Create a pane for the item list
      val itemListContainer = new FlowPane()
      itemListContainer.setHgap( 15 )
      itemListContainer.setVgap( 15 )
      itemListContainer.children = itemChildren
      itemListContainer.setPadding( Insets( 20 ) )

      // Create a pane to stack different views. The view changes based on selected menu option
      val mainWindow = new StackPane()
      mainWindow.getChildren.add( itemListContainer )

      // Root pane
      val root = new BorderPane {
         top = menuBar
         center = mainWindow
      }
      stage.scene = Scene( parent = root )

   end start
end Main