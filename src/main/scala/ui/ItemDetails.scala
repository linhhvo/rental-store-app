package ui

import logic.*
import logic.RentalStore.deleteItem
import logic.SavedItems.{addToCart, savedItems}
import logic.enums.IncidentType
import logic.util.*
import logic.util.IOHandler.writeSavedItems
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

import java.time.format.DateTimeFormatter

object ItemDetails:
   private val smallWidth = 240
   private val btnWidth = 125
   private val smallHeight = 75

   /** User message alert */
   private val messageAlert = new Alert( Information )

   /** Rent item */

   // select rent quantity to add to saved list
   private var rentQuantity = Spinner( 1, 100, 1, 1 )

   // button to add item to saved list
   private val submitItemBtn = Button( "Add To List" )

   // group rent item quantity and button with label
   private val rentItemContainer = new VBox:
      spacing = 10
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
   cmtText.prefWidth = smallWidth
   cmtText.maxHeight = smallHeight
   cmtText.wrapText = true

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
   incTypeChoiceBox.prefWidth = smallWidth

   // date picker to select incident date
   private val incidentDatePicker = DatePicker()
   incidentDatePicker.promptText = "Select incident date"
   incidentDatePicker.prefWidth = smallWidth

   // select quantity of item affected
   private var affectedQuantity = Spinner( 1, 100, 1, 1 )

   // text field to add incident comment
   private val incidentCmt = TextArea()
   incidentCmt.prefWidth = smallWidth
   incidentCmt.maxHeight = smallHeight
   incidentCmt.wrapText = true
   incidentCmt.promptText = "Enter incident detail"

   // text field to add cost to repair
   private val repairCostField = TextField()
   repairCostField.prefWidth = smallWidth
   repairCostField.promptText = "Enter repair cost"

   // button to add incident to item
   private val submitIncidentBtn = Button( "Submit" )
   submitIncidentBtn.margin = Insets( 5, 0, 0, 0 )

   // group incident input fields with labels
   private val incidentContainer = new VBox:
      spacing = 7
      margin = Insets( 5, 0, 5, 0 )

   // button to show/hide incident input fields
   private val reportIncidentBtn = Button( "Report Incident" )
   reportIncidentBtn.margin = Insets( 15, 0, 0, 0 )
   reportIncidentBtn.prefWidth = btnWidth
   reportIncidentBtn.onAction = ( ( e ) => toggleDisplay( incidentContainer ) )

   /** Item details container */
   private val itemDetailsContainer = VBox( 5 )
   itemDetailsContainer.padding = Insets( 10, 0, 0, 0 )

   /** Item details container */
   private val itemActionsContainer = VBox( 5 )
   itemDetailsContainer.padding = Insets( 10, 0, 0, 0 )

   /** Page container */
   private val itemPageContainer = HBox( 20 )
   itemPageContainer.padding = Insets( 20 )

   // create label fields for item details
   private val nameLabel = Label( "Item: " ) // name
   nameLabel.font = Font( null, FontWeight( Bold ), 14 )

   private val catLabel = Label( "Category: " ) // category
   catLabel.font = Font( null, FontWeight( Bold ), 14 )

   private val descLabel = Label( "Description: " ) // description
   descLabel.font = Font( null, FontWeight( Bold ), 14 )

   private val rphLabel = Label( "Rate Per Hour: " ) // rate per hour
   rphLabel.font = Font( null, FontWeight( Bold ), 14 )

   private val rpdLabel = Label( "Rate Per Day: " ) // rate per day
   rpdLabel.font = Font( null, FontWeight( Bold ), 14 )

   private val rpwLabel = Label( "Rate Per Week: " ) // rate per week
   rpwLabel.font = Font( null, FontWeight( Bold ), 14 )

   private val statusLabel1 = Label( "Quantity available for rent: " ) // avaialble quantity
   statusLabel1.font = Font( null, FontWeight( Bold ), 14 )

   private val statusLabel2 = Label( "Currently rented: " ) // rented quantity
   statusLabel2.font = Font( null, FontWeight( Bold ), 14 )

   private val statusLabel3 = Label( "Item will be available after: " ) // earliest return date
   statusLabel3.font = Font( null, FontWeight( Bold ), 14 )

   private val cmtLabel = Label( "Comments:" ) // comment
   cmtLabel.font = Font( null, FontWeight( Bold ), 14 )

   private val childrenLabel = Label( "Bundle with: " ) // children items
   childrenLabel.font = Font( null, FontWeight( Bold ), 14 )

   /** display details specific to item passed in parameter */
   def displayItemPage( item: Item ) =
      // clear all input fields
      resetItemActionFields()

      // update rent item quantity selection spinner
      rentQuantity = Spinner( 0, math.max( item.ownedQuantity - item.lostQuantity, 0 ), 0, 1 )
      rentQuantity.prefWidth = 125
      rentQuantity.editable = true

      // render rent item container to include updated spinner
      rentItemContainer.children = Array( Label( "Quantity" ), rentQuantity, submitItemBtn )

      // update incident affected quantity spinner
      affectedQuantity = Spinner( 0, math.max( item.ownedQuantity - item.lostQuantity, 0 ), 0, 1 )
      affectedQuantity.prefWidth = 125
      affectedQuantity.editable = true

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

      /** button action to rent item: if item has children, also add child items to saved list with
        * the same quantity. Then update saved list external data file and show user sucess message
        * alert. If there are errors, display error message to user.
        */
      submitItemBtn.onAction = (
          ( e ) =>
             try
                addToCart( item.withAllChildren(), rentQuantity.getValue )
                writeSavedItems( savedItems )
                displayAlert( messageAlert, "Added to saved list" )
             catch
                case DuplicateItem( m )    => displayAlert( messageAlert, m )
                case ItemNotAvailable( m ) => displayAlert( messageAlert, m )
                case MissingData( m )      => displayAlert( messageAlert, m )
                case _                     => println( "cannot add item to saved list" )
      )

      /** button action to delete item: display confirmation alert to ask user to confirm that they
        * want to delete the item. Remove item from UI and display success message to user. Do
        * nothing if user selects No
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

      /** action to add comment to item: add comment to item and clear input text field if success
        */
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

      /** button action to add incident to item: verify that user has entered all required
        * information for the incident then add new incident to the item. Clear all input text
        * fields after successful addition
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
      val itemName = HBox( nameLabel, Label( item.name ) )
      itemName.alignment = BaselineLeft

      val itemCat = HBox( catLabel, Label( item.category ) )
      itemCat.alignment = BaselineLeft

      val itemDesc = HBox( descLabel, Label( item.desc ) )
      itemDesc.alignment = BaselineLeft

      val itemRph = HBox( rphLabel, Label( item.ratePerHour.toString ) )
      itemRph.alignment = BaselineLeft

      val itemRpd = HBox( rpdLabel, Label( item.ratePerDay.toString ) )
      itemRpd.alignment = BaselineLeft

      val itemRpw = HBox( rpwLabel, Label( item.ratePerWeek.toString ) )
      itemRpw.alignment = BaselineLeft

      // display item availability status
      val status1 = HBox(
          statusLabel1,
          Label( math.max( item.availableQuantity, 0 ).toString )
      )
      status1.alignment = BaselineLeft
      status1.margin = Insets( 10, 0, 0, 0 )

      val status2 = HBox(
          statusLabel2,
          Label( item.rentedQuantityNow().toString )
      )
      status2.alignment = BaselineLeft

      val status3 = new HBox()
      status3.alignment = BaselineLeft

      // only show the earliest return date if item is not available at the current time
      if item.availableQuantity <= 0 then
         status3.children = Array( Label( "All units of this item are lost " ) )
      else if ( item.availableQuantity - item.rentedQuantityNow() ) <= 0 then
         status3.children = Array(
             statusLabel3,
             Label(
                 item
                    .getEarliestReturnDate()
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
      itemDetailsContainer.children = Array( itemName,
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
      itemActionsContainer.children = Array( rentItemBtn,
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

   /** hide action details and clear all input text fields when an item details page is first
     * generated
     */
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