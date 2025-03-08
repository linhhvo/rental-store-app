package ui

import logic.RentalStore.deleteItem
import logic.{Item, ItemDoesNotExist}
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, Label}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, VBox}
import ui.Helper.addAlert
import upickle.default.*

object ItemDetails:
   def displayItemPage( itemJs: ujson.Value ) =
      println( itemJs )
      val item = read[Item]( itemJs )
      val itemPageContainer = HBox( 20 )
      val itemDetailsContainer = VBox( 5 )

      val itemImage = getClass.getResourceAsStream( "/images/" + item.images.head )
      val itemImageView = ImageView( Image( itemImage, 0, 400, true, true ) )
      val itemNameLabel = Label( item.name )

      itemPageContainer.getChildren.addAll( itemImageView, itemDetailsContainer )
      itemPageContainer.padding = Insets( 20 )

      val deleteItemBtn = Button( "Delete Item" )
      val messageAlert = addAlert

      deleteItemBtn.onAction = (
          ( e ) =>
             try
                deleteItem( item )
                messageAlert.headerText = "Item has been deleted from the system."
                messageAlert.showAndWait()
             catch
                case ItemDoesNotExist( msg ) =>
                   messageAlert.headerText = msg
                   messageAlert.showAndWait()
      )

      itemDetailsContainer.padding = Insets( 10, 0, 0, 0 )
      itemDetailsContainer.getChildren.addAll(
          itemNameLabel,
          Label( "Category: " + item.category ),
          Label( "Description: " + item.desc ),
          Label( "Rate Per Hour: " + item.ratePerHour ),
          Label( "Status: " + ( if item.isAvailable then "Available" else "Not Available" ) ),
          if item.comments.nonEmpty then Label( "Comments:\n- " + item.comments.mkString( "\n- " ) )
          else Label( null ),
          deleteItemBtn
      )

      itemPageContainer