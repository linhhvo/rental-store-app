package ui

import logic.Item
import scalafx.geometry.Insets
import scalafx.scene.Node
import scalafx.scene.control.Label
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, Pane, VBox}

object Helper:
   // Function to show a page in the stack pane while hiding the rest
   def displayPage( parentContainer: Pane, targetPage: Node ) =
      parentContainer.children.foreach( page => page.setVisible( false ) )
      targetPage.setVisible( true )

   def displayItemPage( item: Item ) =
      val itemPageContainer = HBox( 20 )
      val itemDetailsContainer = VBox( 5 )

      val itemImage = getClass.getResourceAsStream( "/images/" + item.images.head )
      val itemImageView = ImageView( Image( itemImage, 0, 400, true, true ) )
      val itemNameLabel = Label( item.name )

      itemPageContainer.getChildren.addAll( itemImageView, itemDetailsContainer )
      itemPageContainer.padding = Insets( 20 )
      itemDetailsContainer.padding = Insets( 10, 0, 0, 0 )
      itemDetailsContainer.getChildren.addAll(
          itemNameLabel,
          Label( "Category: " + item.category ),
          Label( "Description: " + item.desc ),
          Label( "Rate Per Hour: " + item.ratePerHour ),
          Label( "Status: " + ( if item.isAvailable then "Available" else "Not Available" ) ),
          if item.comments.nonEmpty then Label( "Comments:\n- " + item.comments.mkString( "\n- " ) )
          else Label( null )
      )

      itemPageContainer