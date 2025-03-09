package ui

import logic.RentalStore.deleteItem
import logic.{Item, ItemDoesNotExist}
import scalafx.geometry.Insets
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.Alert.AlertType.Information
import scalafx.scene.control.{Alert, Button, Label, TextArea}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, VBox}
import upickle.default.*

object ItemDetails:
   def displayItemPage( itemJs: ujson.Value ) =
      val item = read[Item]( itemJs )
      val itemPageContainer = HBox( 20 )
      val itemDetailsContainer = VBox( 5 )

      val itemImage = getClass.getResourceAsStream( "/images/" + item.images )
      val itemImageView = ImageView( Image( itemImage, 0, 400, true, true ) )
      val itemNameLabel = Label( item.name )

      itemPageContainer.getChildren.addAll( itemImageView, itemDetailsContainer )
      itemPageContainer.padding = Insets( 20 )

      val messageAlert = new Alert( Information )

      val deleteItemBtn = Button( "Delete Item" )
      deleteItemBtn.margin = Insets( 0, 0, 15, 0 )
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

      val addCmtBtn = Button( "Add Comment" )

      val cmtText = TextArea()
      cmtText.maxWidth = 150
      cmtText.maxHeight = 75

      val submitCmtBtn = Button( "Submit Comment" )

      val addCmtContainer = new VBox:
         spacing = 10
         children = Array( Label( "Enter Comment" ), cmtText, submitCmtBtn )
         visible = false
         managed = false
         margin = Insets( 5, 0, 5, 0 )

      addCmtBtn.onAction = (
          ( e ) =>
             if addCmtContainer.visible.get() then
                addCmtContainer.visible = false
                addCmtContainer.managed = false
             else
                addCmtContainer.visible = true
                addCmtContainer.managed = true
      )

      val reportDmgBtn = Button( "Report Damage/Loss" )
      reportDmgBtn.margin = Insets( 15, 0, 15, 0 )

      itemDetailsContainer.padding = Insets( 10, 0, 0, 0 )
      itemDetailsContainer.getChildren.addAll(
          itemNameLabel,
          Label( "Category: " + item.category ),
          Label( "Description: " + item.desc ),
          Label( "Rate Per Hour: " + item.ratePerHour ),
          Label( "Status: " + ( if item.isAvailable then "Available" else "Not Available" ) ),
          if item.comments.nonEmpty then Label( "Comments:\n- " + item.comments.mkString( "\n- " ) )
          else Label( null ),
          deleteItemBtn,
          addCmtBtn,
          addCmtContainer,
          reportDmgBtn
      )

      itemPageContainer