package ui

import logic.*
import logic.RentalStore.deleteItem
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

      // select rent quantity to add to saved list
      val rentQuantity = Spinner( 1, 100, 1, 1 )
      rentQuantity.maxWidth = 125
      rentQuantity.editable = true

      // button to add item to saved list
      val submitItemBtn = Button( "Add To List" )
      submitItemBtn.onAction = (
          ( e ) =>
             try
                addToCart( item, rentQuantity.getValue )
                // if item has children, also add child items to saved list with the same quantity
                item.getChildrenDetails.foreach( c => addToCart( c, rentQuantity.getValue ) )
                // show user success message
                displayAlert( messageAlert, "Added to saved list" )
             catch
                case DuplicateItem( m )    => displayAlert( messageAlert, m )
                case ItemNotAvailable( m ) => displayAlert( messageAlert, m )
      )

      // group rent item quantity and button with label
      val rentItemContainer = new VBox:
         spacing = 7
         children = Array( Label( "Quantity" ), rentQuantity, submitItemBtn )
         visible = false
         managed = false
         margin = Insets( 5, 0, 5, 0 )

      // button to show/hide input fields to add item to saved list
      val rentItemBtn = Button( "Rent Item" )
      rentItemBtn.onAction = ( ( e ) => toggleDisplay( rentItemContainer ) )

      /** Delete item button */

      val deleteItemBtn = Button( "Delete Item" )
      deleteItemBtn.margin = Insets( 15, 0, 15, 0 )
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
                case _                      => println( "cancel" )
      )

      /** Add item comment */

      // input field to enter comment
      val cmtText = TextArea()
      cmtText.promptText = "Enter comment"
      cmtText.maxWidth = smallWidth
      cmtText.maxHeight = smallHeight

      // button to add comment to item
      val submitCmtBtn = Button( "Submit" )
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

      // group comment text field and button
      val addCmtContainer = new VBox:
         spacing = 10
         children = Array( cmtText, submitCmtBtn )
         visible = false
         managed = false
         margin = Insets( 5, 0, 5, 0 )

      // button to show/hide comment field
      val addCmtBtn = Button( "Add Comment" )
      addCmtBtn.onAction = ( ( e ) => toggleDisplay( addCmtContainer ) )

      /** Report item incident */

      // dropdown menu to select incident type
      val incTypeList = ObservableBuffer[IncidentType]( IncidentType.Loss, IncidentType.Damage )
      val incTypeChoiceBox = ChoiceBox( incTypeList )
      incTypeChoiceBox.maxWidth = smallWidth

      // date picker to select incident date
      val incidentDatePicker = DatePicker()
      incidentDatePicker.promptText = "Select incident date"
      incidentDatePicker.maxWidth = smallWidth

      // text field to add incident comment
      val incidentCmt = TextArea()
      incidentCmt.maxWidth = smallWidth
      incidentCmt.maxHeight = smallHeight
      incidentCmt.promptText = "Enter incident detail"

      // text field to add cost to repair
      val repairCostField = TextField()
      repairCostField.maxWidth = smallWidth
      repairCostField.promptText = "Enter repair cost"

      // button to add incident to item
      val submitIncidentBtn = Button( "Submit" )
      submitIncidentBtn.onAction = ( ( e ) =>
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
            catch case ItemDoesNotExist( msg ) => displayAlert( messageAlert, msg )
         else displayAlert( messageAlert, "Missing information" )

         // reset and clear input fields
         incidentCmt.clear()
         repairCostField.clear()
         incTypeChoiceBox.getSelectionModel.clearSelection()
         incidentDatePicker.setValue( null )
      )

      // group incident input fields with labels
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
             submitIncidentBtn
         )
         visible = false
         managed = false
         margin = Insets( 5, 0, 5, 0 )

      // button to show/hide incident input fields
      val reportIncidentBtn = Button( "Report Incident" )
      reportIncidentBtn.margin = Insets( 15, 0, 0, 0 )
      reportIncidentBtn.onAction = ( ( e ) => toggleDisplay( incidentContainer ) )

      /** Item details container */

      val itemNameLabel = Label( item.name )

      val itemDetailsContainer = VBox( 5 )
      itemDetailsContainer.padding = Insets( 10, 0, 0, 0 )
      itemDetailsContainer.getChildren.addAll(
          itemNameLabel,
          Label( "Category: " + item.category ),
          Label( "Description: " + item.desc ),
          Label( "Rate Per Hour: " + item.ratePerHour ),
          Label( s"Available for rent: ${item.availableQuantity}" ),
          if item.comments.nonEmpty then Label( "Comments:\n- " + item.comments.mkString( "\n- " ) )
          else Label( null ),
          rentItemBtn,
          rentItemContainer,
          deleteItemBtn,
          addCmtBtn,
          addCmtContainer,
          reportIncidentBtn,
          incidentContainer
      )

      /** Page container */

      val itemPageContainer = HBox( 20 )

      val itemImage = getClass.getResourceAsStream( "/images/" + item.images )
      val itemImageView = ImageView( Image( itemImage, 0, 400, true, true ) )

      itemPageContainer.getChildren.addAll( itemImageView, itemDetailsContainer )
      itemPageContainer.padding = Insets( 20 )

      itemPageContainer