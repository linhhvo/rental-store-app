package ui

import logic.*
import logic.Inventory.deleteItem
import logic.SavedList.addToCart
import logic.enums.IncidentType
import logic.util.*
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.geometry.Pos.BaselineLeft
import scalafx.scene.control.*
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.Alert.AlertType.{Confirmation, Information}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.text.FontWeight.Bold
import scalafx.scene.text.{Font, FontWeight}
import ui.util.Helper.{displayAlert, getItemImageView, toggleDisplay}
import ui.util.Validator.{validateLongTextInput, validateNumInput}
import upickle.default.*

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ItemDetails:
   private val smallWidth = 240
   private val btnWidth = 125
   private val smallHeight = 75

   private val messageAlert = new Alert( Information )

   /* RENT ITEM */

   // select rent quantity to add to saved list
   private var rentQuantity = Spinner( 1, 100, 1, 1 )

   // button to add item to saved list
   private val submitItemBtn = Button( "Add To List" )

   // group rent item quantity and button with label
   private val rentItemContainer = new VBox:
      spacing = 10
      margin = Insets( 5, 0, 5, 0 )

   // button to show/hide input fields to add item to saved list
   private val rentItemBtn = new Button:
      text = "Rent Item"
      prefWidth = btnWidth
      onAction = ( ( e ) => toggleDisplay( rentItemContainer ) )
      margin = Insets( 20, 0, 0, 0 )

   /* DELETE ITEM */

   private val deleteItemBtn = new Button:
      text = "Delete Item"
      prefWidth = btnWidth
      margin = Insets( 15, 0, 15, 0 )

   /* ADD ITEM COMMENT */

   // input field to enter comment
   private val cmtText = new TextArea:
      promptText = "Enter comment"
      prefWidth = smallWidth
      maxHeight = smallHeight
      wrapText = true

   // button to add comment to item
   private val submitCmtBtn = Button( "Submit" )

   // group comment text field and button
   private val addCmtContainer = new VBox:
      spacing = 10
      children = Array( cmtText, submitCmtBtn )
      margin = Insets( 10, 0, 5, 0 )

   // button to show/hide comment field
   private val addCmtBtn = new Button:
      text = "Add Comment"
      prefWidth = btnWidth
      onAction = ( ( e ) => toggleDisplay( addCmtContainer ) )

   /* REPORT ITEM INCIDENT */

   // dropdown menu to select incident type
   private val incTypeList =
      ObservableBuffer[IncidentType]( IncidentType.Loss, IncidentType.Damage )

   private val incTypeChoiceBox = new ChoiceBox[IncidentType]:
      items = incTypeList
      prefWidth = smallWidth

   // date picker to select incident date
   private val incidentDatePicker = new DatePicker:
      promptText = "Select incident date"
      prefWidth = smallWidth

   // select quantity of item affected
   private var affectedQuantity = Spinner( 1, 100, 1, 1 )

   // text field to add incident comment
   private val incidentCmt = new TextArea:
      prefWidth = smallWidth
      maxHeight = smallHeight
      wrapText = true
      promptText = "Enter incident detail"

   // text field to add cost to repair
   private val repairCostField = new TextField:
      prefWidth = smallWidth
      promptText = "Enter repair cost"

   // button to add incident to item
   private val submitIncidentBtn = new Button:
      text = "Submit"
      margin = Insets( 5, 0, 0, 0 )

   // group incident input fields with labels
   private val incidentContainer = new VBox:
      spacing = 7
      margin = Insets( 5, 0, 5, 0 )

   // button to show/hide incident input fields
   private val reportIncidentBtn = new Button:
      text = "Report Incident"
      margin = Insets( 15, 0, 0, 0 )
      prefWidth = btnWidth
      onAction = ( ( e ) => toggleDisplay( incidentContainer ) )

   /* Container to group all item details texts */
   private val itemDetailsContainer = new VBox:
      spacing = 5
      padding = Insets( 10, 0, 0, 0 )

   /* Container to group all item action buttons and input fields */
   private val itemActionsContainer = new VBox:
      spacing = 5
      padding = Insets( 10, 0, 0, 0 )

   /* Page container */
   private val itemPageContainer = new HBox:
      spacing = 20
      padding = Insets( 20 )

   /* Labels for item details */
   private val nameLabel = new Label:
      text = "Item: "
      font = Font( null, FontWeight( Bold ), 14 )

   private val catLabel = new Label:
      text = "Category: "
      font = Font( null, FontWeight( Bold ), 14 )

   private val descLabel = new Label:
      text = "Description: "
      font = Font( null, FontWeight( Bold ), 14 )

   private val rphLabel = new Label:
      text = "Rate Per Hour: "
      font = Font( null, FontWeight( Bold ), 14 )

   private val rpdLabel = new Label:
      text = "Rate Per Day: "
      font = Font( null, FontWeight( Bold ), 14 )

   private val rpwLabel = new Label:
      text = "Rate Per Week: "
      font = Font( null, FontWeight( Bold ), 14 )

   private val statusLabel1 = new Label:
      text = "Quantity available for rent: "
      font = Font( null, FontWeight( Bold ), 14 )

   private val statusLabel2 = new Label:
      text = "Currently rented: "
      font = Font( null, FontWeight( Bold ), 14 )

   private val statusLabel3 = new Label:
      text = "Item will be available after: "
      font = Font( null, FontWeight( Bold ), 14 )

   private val cmtLabel = new Label:
      text = "Comments:"
      font = Font( null, FontWeight( Bold ), 14 )

   private val childrenLabel = new Label:
      text = "Bundle with: "
      font = Font( null, FontWeight( Bold ), 14 )

   /** Display details specific to item passed in parameter */
   def displayItemPage( item: Item ) =
      // clear all input fields
      resetItemActionFields()

      // update rent item quantity selection spinner
      rentQuantity = new Spinner( 0, math.max( item.ownedQuantity - item.lostQuantity, 0 ), 0, 1 ):
         prefWidth = 125
         editable = true

      // render rent item container to include updated spinner
      rentItemContainer.children = Array( Label( "Quantity" ), rentQuantity, submitItemBtn )

      // update incident affected quantity spinner
      affectedQuantity =
         new Spinner( 0, math.max( item.ownedQuantity - item.lostQuantity, 0 ), 0, 1 ):
            prefWidth = 125
            editable = true

      // render incident container to include update spinner
      incidentContainer.children = Array(
          Label( "Incident Type" ),
          incTypeChoiceBox,
          Label( "Incident Date" ),
          incidentDatePicker,
          Label( "Affected Quantity" ),
          affectedQuantity,
          Label( "Incident Detail" ),
          incidentCmt,
          Label( "Repair Cost" ),
          repairCostField,
          submitIncidentBtn
      )

      /** Rent item
        * If the item has children, also add child items to a saved list with the same
        * quantity.
        * Then update saved list external data file and show user success message alert.
        * If there are errors, display an error message to user.
        */
      submitItemBtn.onAction = (
          ( e ) =>
             try
                addToCart( item.withAllChildren(), rentQuantity.getValue )
                displayAlert( messageAlert, "Added to saved list" )
             catch
                case DuplicateItem( m )    => displayAlert( messageAlert, m )
                case ItemNotAvailable( m ) => displayAlert( messageAlert, m )
                case MissingData( m )      => displayAlert( messageAlert, m )
                case _                     => println( "cannot add item to saved list" )
      )

      /** Delete item
        * Display confirmation alert to ask user to confirm that they want to delete the
        * item.
        * Remove item from UI and display success message to user. Do nothing if user selects No
        */
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
                case _                      => // Do nothing
      )

      /* Add comment to item and clear input text field if success */
      submitCmtBtn.onAction = (
          ( e ) =>
             try
                item.addComment( validateLongTextInput( cmtText.getText, "Comment" ) )
                cmtText.clear()
                displayAlert( messageAlert, "Comment has been added" )
             catch
                case InvalidInput( m )       => displayAlert( messageAlert, m )
                case ItemDoesNotExist( msg ) =>
                   displayAlert( messageAlert, msg )
                case MissingData( msg )      =>
                   displayAlert( messageAlert, msg )
      )

      /** Add incident to item
        * Verify that user has entered all required information for the incident then add new incident to the item.
        * Clear all input text fields after successful addition
        */
      submitIncidentBtn.onAction = (
          ( e ) =>
             if incTypeChoiceBox.getSelectionModel.getSelectedItem != null
                && incidentDatePicker.getValue != null
                && incidentCmt.getText.nonEmpty
                && repairCostField.getText.nonEmpty
             then
                try
                   val newIncident = Incident(
                       incTypeChoiceBox.getValue,
                       incidentDatePicker.getValue,
                       affectedQuantity.getValue,
                       validateLongTextInput( incidentCmt.getText, "Incident Detail" ),
                       validateNumInput( repairCostField.getText, "Repair Cost" )
                   )
                   item.reportIncident( newIncident, affectedQuantity.getValue )

                   displayAlert( messageAlert, "Incident has been reported" )

                   incidentCmt.clear()
                   repairCostField.clear()
                   incTypeChoiceBox.getSelectionModel.clearSelection()
                   incidentDatePicker.setValue( null )
                catch
                   case MissingData( m )        => displayAlert( messageAlert, m )
                   case InvalidInput( m )       => displayAlert( messageAlert, m )
                   case ItemDoesNotExist( msg ) => displayAlert( messageAlert, msg )
                   case _                       => println( "cannot report incident" )
             else displayAlert( messageAlert, "Missing information" )
      )

      // group label and item data into separate containers
      val itemName = new HBox:
         children = Array( nameLabel, Label( item.name ) )
         alignment = BaselineLeft

      val itemCat = new HBox:
         children = Array( catLabel, Label( item.category ) )
         alignment = BaselineLeft

      val itemDesc = new HBox:
         children = Array( descLabel, Label( item.desc ) )
         alignment = BaselineLeft

      val itemRph = new HBox:
         children = Array( rphLabel, Label( item.ratePerHour.toString ) )
         alignment = BaselineLeft

      val itemRpd = new HBox:
         children = Array( rpdLabel, Label( item.ratePerDay.toString ) )
         alignment = BaselineLeft

      val itemRpw = new HBox:
         children = Array( rpwLabel, Label( item.ratePerWeek.toString ) )
         alignment = BaselineLeft

      // display item availability status
      val status1 = new HBox:
         children = Array( statusLabel1, Label( math.max( item.availableQuantity, 0 ).toString ) )
         alignment = BaselineLeft
         margin = Insets( 10, 0, 0, 0 )

      // show the number of units rented at the current time
      val rentedQntNow =
         item.rentedQntByDateRange( LocalDateTime.now, LocalDateTime.now.plusHours( 1 ) )
      val status2 = new HBox:
         children = Array( statusLabel2, Label( rentedQntNow.toString ) )
         alignment = BaselineLeft

      // show the earliest return date if item is not available at the current time
      val status3 = new HBox:
         alignment = BaselineLeft
      if item.availableQuantity <= 0 then
         status3.children = Array( Label( "All units of this item are lost " ) )
      else if ( item.availableQuantity - rentedQntNow ) <= 0
      then
         status3.children = Array(
             statusLabel3,
             Label(
                 item.getEarliestReturnDate
                    .format( DateTimeFormatter.ofPattern( "dd-MM-yyyy HH:mm" ) )
             )
         )

      // display item comments
      val itemCmt = VBox( cmtLabel, Label( "- " + item.comments.mkString( "\n- " ) ) )
      if item.comments.isEmpty then
         itemCmt.visible = false
         itemCmt.managed = false

      // display item's dependencies
      val itemChildren =
         HBox( childrenLabel, Label( item.getChildrenDetails().map( _.name ).mkString( ", " ) ) )
      if item.children.isEmpty then
         itemChildren.visible = false
         itemChildren.managed = false

      // add all labels to item details container
      itemDetailsContainer.children = Array(
          itemName,
          itemCat,
          itemDesc,
          itemRph,
          itemRpd,
          itemRpw,
          itemChildren,
          itemCmt,
          status1,
          status2,
          status3
      )

      // group all item actions
      itemActionsContainer.children = Array(
          rentItemBtn,
          rentItemContainer,
          deleteItemBtn,
          addCmtBtn,
          addCmtContainer,
          reportIncidentBtn,
          incidentContainer
      )

      val itemImageView = getItemImageView( item )

      // group all details into an overall page container
      itemPageContainer.children =
         Array( itemImageView, itemDetailsContainer, itemActionsContainer )
      itemPageContainer

   /** Hide action details and clear all input text fields when an item details page is first generated */
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