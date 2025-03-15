package ui

import logic.*
import logic.RentalStore.{deleteItem, updateItemComment, updateItemIncident}
import logic.SavedItems.addToCart
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.*
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.Alert.AlertType.{Confirmation, Information}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, VBox}
import ui.Helper.{displayAlert, toggleDisplay}
import upickle.default.*

object ItemDetails:
   val smallWidth = 240
   val smallHeight = 75

   def displayItemPage( item: Item ) =
      /** User message alert */
      val messageAlert = new Alert( Information )

      /** Rent item */
      val rentItemBtn = Button( "Rent Item" )

      val rentQuantity = Spinner( 1, 100, 1, 1 )
      rentQuantity.maxWidth = 125
      rentQuantity.editable = true

      val submitItemBtn = Button( "Add To List" )

      val rentItemContainer = new VBox:
         spacing = 7
         children = Array( Label( "Quantity" ), rentQuantity, submitItemBtn )
         visible = false
         managed = false
         margin = Insets( 5, 0, 5, 0 )

      rentItemBtn.onAction = ( ( e ) => toggleDisplay( rentItemContainer ) )

      submitItemBtn.onAction = (
          ( e ) =>
             try
                addToCart( item, rentQuantity.getValue )
                displayAlert( messageAlert, "Added to saved list" )
             catch case DuplicateItem( msg ) => displayAlert( messageAlert, msg )
      )

      /** Delete item button */

      val deleteItemBtn = Button( "Delete Item" )
      deleteItemBtn.margin = Insets( 15, 0, 15, 0 )
      deleteItemBtn.onAction = (
          ( e ) =>
             new Alert( Confirmation ) {
                headerText = "Do you want to delete this item?"
                buttonTypes = Seq( ButtonType.Yes, ButtonType.No )
             }.showAndWait() match
                case Some( ButtonType.Yes ) =>
                   try
                      deleteItem( item )
                      displayAlert( messageAlert, "Item has been deleted from the system" )
                   catch
                      case ItemDoesNotExist( msg ) =>
                         displayAlert( messageAlert, msg )
                case _                      => println( "cancel" )
      )

      /** Add item comment */

      val cmtText = TextArea()
      cmtText.promptText = "Enter comment"
      cmtText.maxWidth = smallWidth
      cmtText.maxHeight = smallHeight

      val submitCmtBtn = Button( "Submit" )
      submitCmtBtn.onAction = (
          ( e ) =>
             try
                updateItemComment( item, cmtText.getText )
                cmtText.clear()
                displayAlert( messageAlert, "Comment has been added" )
             catch
                case ItemDoesNotExist( msg ) =>
                   displayAlert( messageAlert, msg )
                case MissingData( msg )      =>
                   displayAlert( messageAlert, msg )
      )

      val addCmtContainer = new VBox:
         spacing = 10
         children = Array( cmtText, submitCmtBtn )
         visible = false
         managed = false
         margin = Insets( 5, 0, 5, 0 )

      val addCmtBtn = Button( "Add Comment" )
      addCmtBtn.onAction = ( ( e ) => toggleDisplay( addCmtContainer ) )

      /** Report item incident */

      val incTypeList = ObservableBuffer[IncidentType]( IncidentType.Loss, IncidentType.Damage )
      val incTypeChoiceBox = ChoiceBox( incTypeList )
      incTypeChoiceBox.maxWidth = smallWidth

      val incidentDatePicker = DatePicker()
      incidentDatePicker.promptText = "Select incident date"
      incidentDatePicker.maxWidth = smallWidth

      val incidentCmt = TextArea()
      incidentCmt.maxWidth = smallWidth
      incidentCmt.maxHeight = smallHeight
      incidentCmt.promptText = "Enter incident detail"

      val repairCostField = TextField()
      repairCostField.maxWidth = smallWidth
      repairCostField.promptText = "Enter repair cost"

      val incSubmitBtn = Button( "Submit" )
      incSubmitBtn.onAction = ( ( e ) =>
         if incTypeChoiceBox.getSelectionModel.getSelectedItem != null
            && incidentDatePicker.getValue != null
            && incidentCmt.getText.nonEmpty
            && repairCostField.getText.nonEmpty
         then
            val newIncident = Incident( incidentDatePicker.getValue,
                                        incTypeChoiceBox.getValue,
                                        incidentCmt.getText,
                                        repairCostField.getText.toDouble
            )
            try updateItemIncident( item, newIncident )
            catch case ItemDoesNotExist( msg ) => displayAlert( messageAlert, msg )
            incidentCmt.clear()
         else displayAlert( messageAlert, "Missing information" )
         repairCostField.clear()
         incTypeChoiceBox.getSelectionModel.clearSelection()
         incidentDatePicker.setValue( null )
      )

      val incidentContainer = new VBox:
         spacing = 7
         children = Array(
             Label( "Incident Type" ),
             incTypeChoiceBox,
             Label( "Incident Date" ),
             incidentDatePicker,
             Label( "Incident Detail" ),
             incidentCmt,
             Label( "Repair Cost" ),
             repairCostField,
             incSubmitBtn
         )
         visible = false
         managed = false
         margin = Insets( 5, 0, 5, 0 )

      val reportDmgBtn = Button( "Report Incident" )
      reportDmgBtn.margin = Insets( 15, 0, 0, 0 )

      reportDmgBtn.onAction = ( ( e ) => toggleDisplay( incidentContainer ) )

      /** Item details container */
      val itemNameLabel = Label( item.name )

      val itemDetailsContainer = VBox( 5 )
      itemDetailsContainer.padding = Insets( 10, 0, 0, 0 )
      itemDetailsContainer.getChildren.addAll(
          itemNameLabel,
          Label( "Category: " + item.category ),
          Label( "Description: " + item.desc ),
          Label( "Rate Per Hour: " + item.ratePerHour ),
          Label( "Status: " + ( if item.isAvailable then "Available" else "Not Available" ) ),
          if item.comments.nonEmpty then Label( "Comments:\n- " + item.comments.mkString( "\n- " ) )
          else Label( null ),
          rentItemBtn,
          rentItemContainer,
          deleteItemBtn,
          addCmtBtn,
          addCmtContainer,
          reportDmgBtn,
          incidentContainer
      )

      /** Page container */
      val itemPageContainer = HBox( 20 )

      val itemImage = getClass.getResourceAsStream( "/images/" + item.images )
      val itemImageView = ImageView( Image( itemImage, 0, 400, true, true ) )

      itemPageContainer.getChildren.addAll( itemImageView, itemDetailsContainer )
      itemPageContainer.padding = Insets( 20 )

      itemPageContainer