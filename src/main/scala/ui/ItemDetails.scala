package ui

import logic.*
import logic.IOHandler.writeSavedItems
import logic.RentalStore.deleteItem
import logic.SavedItems.{addToCart, savedItems}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.geometry.Pos.BaselineLeft
import scalafx.scene.control.*
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.Alert.AlertType.{Confirmation, Information}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.text.FontWeight.Bold
import scalafx.scene.text.{Font, FontWeight}
import ui.Helper.{displayAlert, toggleDisplay}
import upickle.default.*

import java.io.FileInputStream

object ItemDetails:
   private val smallWidth = 240
   private val btnWidth = 125
   private val smallHeight = 75

   /** User message alert */
   private val messageAlert = new Alert( Information )

   /** Rent item */

   // select rent quantity to add to saved list
   private val rentQuantity = Spinner( 1, 100, 1, 1 )
   rentQuantity.maxWidth = 125
   rentQuantity.editable = true

   // button to add item to saved list
   private val submitItemBtn = Button( "Add To List" )

   // group rent item quantity and button with label
   private val rentItemContainer = new VBox:
      spacing = 10
      children = Array( Label( "Quantity" ), rentQuantity, submitItemBtn )
      margin = Insets( 5, 0, 5, 0 )

   // button to show/hide input fields to add item to saved list
   private val rentItemBtn = Button( "Rent Item" )
   rentItemBtn.prefWidth = btnWidth
   rentItemBtn.onAction = ( ( e ) => toggleDisplay( rentItemContainer ) )
   rentItemBtn.margin = Insets( 20, 0, 0, 0 )

   /** Delete item button */

   private val deleteItemBtn = Button( "Delete Item" )
   deleteItemBtn.prefWidth = btnWidth
   deleteItemBtn.margin = Insets( 15, 0, 15, 0 )

   /** Add item comment */

   // input field to enter comment
   private val cmtText = TextArea()
   cmtText.promptText = "Enter comment"
   cmtText.maxWidth = smallWidth
   cmtText.maxHeight = smallHeight

   // button to add comment to item
   private val submitCmtBtn = Button( "Submit" )

   // group comment text field and button
   private val addCmtContainer = new VBox:
      spacing = 10
      children = Array( cmtText, submitCmtBtn )
      margin = Insets( 10, 0, 5, 0 )

   // button to show/hide comment field
   private val addCmtBtn = Button( "Add Comment" )
   addCmtBtn.prefWidth = btnWidth
   addCmtBtn.onAction = ( ( e ) => toggleDisplay( addCmtContainer ) )

   /** Report item incident */

   // dropdown menu to select incident type
   private val incTypeList =
      ObservableBuffer[IncidentType]( IncidentType.Loss, IncidentType.Damage )
   private val incTypeChoiceBox = ChoiceBox( incTypeList )
   incTypeChoiceBox.maxWidth = smallWidth

   // date picker to select incident date
   private val incidentDatePicker = DatePicker()
   incidentDatePicker.promptText = "Select incident date"
   incidentDatePicker.maxWidth = smallWidth

   // text field to add incident comment
   private val incidentCmt = TextArea()
   incidentCmt.maxWidth = smallWidth
   incidentCmt.maxHeight = smallHeight
   incidentCmt.promptText = "Enter incident detail"

   // text field to add cost to repair
   private val repairCostField = TextField()
   repairCostField.maxWidth = smallWidth
   repairCostField.promptText = "Enter repair cost"

   // button to add incident to item
   private val submitIncidentBtn = Button( "Submit" )

   // group incident input fields with labels
   private val incidentContainer = new VBox:
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
          submitIncidentBtn
      )
      margin = Insets( 5, 0, 5, 0 )

   // button to show/hide incident input fields
   private val reportIncidentBtn = Button( "Report Incident" )
   reportIncidentBtn.margin = Insets( 15, 0, 0, 0 )
   reportIncidentBtn.prefWidth = btnWidth
   reportIncidentBtn.onAction = ( ( e ) => toggleDisplay( incidentContainer ) )

   /** Item details container */
   private val itemDetailsContainer = VBox( 5 )
   itemDetailsContainer.padding = Insets( 10, 0, 0, 0 )

   /** Page container */
   private val itemPageContainer = HBox( 20 )
   itemPageContainer.padding = Insets( 20 )

   // label fields for item details
   private val nameLabel = Label( "Item: " )
   nameLabel.font = Font( null, FontWeight( Bold ), 14 )
   private val catLabel = Label( "Category: " )
   catLabel.font = Font( null, FontWeight( Bold ), 14 )
   private val descLabel = Label( "Description: " )
   descLabel.font = Font( null, FontWeight( Bold ), 14 )
   private val rateLabel = Label( "Rate Per Hour: " )
   rateLabel.font = Font( null, FontWeight( Bold ), 14 )
   private val avaiLabel = Label( "Quantity available for rent: " )
   avaiLabel.font = Font( null, FontWeight( Bold ), 14 )
   private val cmtLabel = Label( "Comments:" )
   cmtLabel.font = Font( null, FontWeight( Bold ), 14 )
   private val childrenLabel = Label( "Bundle with: " )
   childrenLabel.font = Font( null, FontWeight( Bold ), 14 )

   /** method to display details specific to item passed in parameter */
   def displayItemPage( item: Item ) =
      resetItemActionFields()

      // set action to rent item
      submitItemBtn.onAction = (
          ( e ) =>
             try
                // if item has children, also add child items to saved list with the same quantity
                addToCart( item.withAllChildren(), rentQuantity.getValue ) 
                writeSavedItems( savedItems )
                // show user success message
                displayAlert( messageAlert, "Added to saved list" )
             catch
                case DuplicateItem( m )    => displayAlert( messageAlert, m )
                case ItemNotAvailable( m ) => displayAlert( messageAlert, m )
      )

      // set action to delete item
      deleteItemBtn.onAction = (
          ( e ) =>
             // ask user to confirm that they want to delete the item
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
                case _                      => // Do nothing
      )

      // set action to add comment to item
      submitCmtBtn.onAction = (
          ( e ) =>
             try
                item.addComment( cmtText.getText )
                // clear input field
                cmtText.clear()
                // display success message to user
                displayAlert( messageAlert, "Comment has been added" )
             catch
                case ItemDoesNotExist( msg ) =>
                   displayAlert( messageAlert, msg )
                case MissingData( msg )      =>
                   displayAlert( messageAlert, msg )
      )

      // set action to add incident to item
      submitIncidentBtn.onAction = (
          ( e ) =>
             // check that all input fields have entered data
             if incTypeChoiceBox.getSelectionModel.getSelectedItem != null
                && incidentDatePicker.getValue != null
                && incidentCmt.getText.nonEmpty
                && repairCostField.getText.nonEmpty
             then
                // create new incident
                val newIncident = Incident( incidentDatePicker.getValue,
                                            incTypeChoiceBox.getValue,
                                            incidentCmt.getText,
                                            repairCostField.getText.toDouble
                )
                try
                   // add incident to item
                   item.reportIncident( newIncident )
                   // display success message to user
                   displayAlert( messageAlert, "Incident has been reported" )

                   // reset and clear input fields
                   incidentCmt.clear()
                   repairCostField.clear()
                   incTypeChoiceBox.getSelectionModel.clearSelection()
                   incidentDatePicker.setValue( null )
                catch case ItemDoesNotExist( msg ) => displayAlert( messageAlert, msg )
             else displayAlert( messageAlert, "Missing information" )
      )

      val itemName = HBox( nameLabel, Label( item.name ) )
      itemName.alignment = BaselineLeft
      val itemCat = HBox( catLabel, Label( item.category ) )
      itemCat.alignment = BaselineLeft
      val itemDesc = HBox( descLabel, Label( item.desc ) )
      itemDesc.alignment = BaselineLeft
      val itemRate = HBox( rateLabel, Label( item.ratePerHour.toString ) )
      itemRate.alignment = BaselineLeft
      val itemAvai = HBox( avaiLabel, Label( item.availableQuantity.toString ) )
      itemAvai.alignment = BaselineLeft
      val itemCmt = VBox( cmtLabel, Label( "- " + item.comments.mkString( "\n- " ) ) )
      if item.comments.isEmpty then
         itemCmt.visible = false
         itemCmt.managed = false
      val itemChildren =
         HBox( childrenLabel, Label( item.getChildrenDetails().map( _.name ).mkString( ", " ) ) )
      if item.children.isEmpty then
         itemChildren.visible = false
         itemChildren.managed = false

      val detailsGrid =
         itemDetailsContainer.getChildren.setAll(
             itemName,
             itemCat,
             itemDesc,
             itemRate,
             itemAvai,
             itemCmt,
             itemChildren,
             rentItemBtn,
             rentItemContainer,
             deleteItemBtn,
             addCmtBtn,
             addCmtContainer,
             reportIncidentBtn,
             incidentContainer
         )

      val itemImage = FileInputStream( "src/main/resources/images/" + item.image )
      val itemImageView = ImageView( Image( itemImage, 0, 400, true, true ) )

      itemPageContainer.getChildren.setAll( itemImageView, itemDetailsContainer )

      itemPageContainer

   /** method to hide action details when an item details page is first generated */
   private def resetItemActionFields() =
      rentItemContainer.visible = false
      rentItemContainer.managed = false

      addCmtContainer.visible = false
      addCmtContainer.managed = false

      incidentContainer.visible = false
      incidentContainer.managed = false

      // clear all input fields
      cmtText.clear()
      incidentCmt.clear()
      repairCostField.clear()
      incTypeChoiceBox.getSelectionModel.clearSelection()
      incidentDatePicker.setValue( null )