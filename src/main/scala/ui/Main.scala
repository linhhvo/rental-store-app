package ui

import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Label, Menu, MenuBar, MenuItem}
import scalafx.scene.layout.*
import scalafx.scene.paint.Color

object Main extends JFXApp3:
   def start() =
      stage = new PrimaryStage:
         title = "Cookie Rental Company"
         height = 1000
         width = 1000
         resizable = true

      val navigation = new Menu( "Navigation" ):
         items = Array( MenuItem( "Browse Items" ),
                        MenuItem( "Return" ),
                        MenuItem( "Calendar" ),
                        MenuItem( "Reports" )
         )

      val menuBar = new MenuBar:
         menus = Array( navigation )

      val jsonItems = ujson.read( os.read( os.pwd / "src/main/resources/data/items.json" ) )

      val itemList = jsonItems.arr
      val itemChildren = itemList.map( item =>
         new VBox():
            children = Array( Label( item( "name" ).str ) )
            padding = Insets( 10 )
            border = Border.stroke( Color.Black )
      )

      val itemListContainer = new FlowPane()
      itemListContainer.setHgap( 15 )
      itemListContainer.setVgap( 15 )
      itemListContainer.children = itemChildren
      itemListContainer.setPadding( Insets( 20 ) )

      val mainWindow = new StackPane()
      mainWindow.getChildren.add( itemListContainer )

      val root = new BorderPane {
         top = menuBar
         center = mainWindow
      }
      stage.scene = Scene( parent = root )

   end start
end Main