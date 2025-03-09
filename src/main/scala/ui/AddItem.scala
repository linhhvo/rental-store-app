package ui

import logic.ItemCategory.getItemCategoryList
import logic.{DuplicateItem, InvalidItemCategory, Item, RentalStore}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.*
import scalafx.scene.control.Alert.AlertType.Information
import scalafx.scene.layout.VBox
import ui.Helper.displayAlert

object AddItem:
   val longWidth = 350
   val smallWidth = 150

   val nameTextField = TextField()
   nameTextField.maxWidth = longWidth

   val itemName = new VBox:
      spacing = 5
      children = Array( Label( "Name" ), nameTextField )

   val descTextField = TextArea()
   descTextField.maxWidth = longWidth
   descTextField.maxHeight = smallWidth

   val itemDesc = new VBox:
      spacing = 5
      children = Array( Label( "Description" ), descTextField )

   val qntSpinner = Spinner( 1, 100, 1, 1 )
   qntSpinner.maxWidth = smallWidth
   qntSpinner.editable = true

   val itemQuantity = new VBox:
      spacing = 5
      children = Array( Label( "Quantity" ), qntSpinner )

   val categoryList = new ObservableBuffer[String]()
   categoryList ++= getItemCategoryList
   val catChoiceBox = ChoiceBox( categoryList )
   catChoiceBox.maxWidth = smallWidth

   val itemCategory = new VBox:
      spacing = 5
      children = Array( Label( "Category" ), catChoiceBox )

   val rateTextField = TextField()
   rateTextField.maxWidth = smallWidth

   val itemRate = new VBox:
      spacing = 5
      children = Array( Label( "Rate Per Hour" ), rateTextField )

//   val itemDependencies = new VBox:
//      spacing = 5
//      val depList = new ObservableBuffer[String]()
//      depList ++= getAvailableItems.map( item => item( "name" ).str )
//      val depChoiceBox = ChoiceBox( depList )
//      depChoiceBox.maxWidth = smallWidth
//      children = Array( Label( "Dependent Item" ), depChoiceBox )

   val addItemBtn = Button( "Add Item" )
   addItemBtn.margin = Insets( 15, 0, 0, 0 )

   val messageAlert = new Alert( Information )

   addItemBtn.onAction = (
       ( e ) =>
          if nameTextField.getText.nonEmpty
             && descTextField.getText.nonEmpty
             && catChoiceBox.getSelectionModel.getSelectedItem != null
             && rateTextField.getText.nonEmpty
          then
             val newItem = Item( nameTextField.getText,
                                 descTextField.getText,
                                 qntSpinner.getValue,
                                 catChoiceBox.getValue,
                                 rateTextField.getText.toDouble
             )
             try
                RentalStore.addItem( newItem )
                displayAlert( messageAlert, "Item added to the system" )
                resetItemPage()
             catch
                case InvalidItemCategory( msg ) =>
                   displayAlert( messageAlert, msg )
                case DuplicateItem( msg )       =>
                   displayAlert( messageAlert, msg )
          else displayAlert( messageAlert, "Missing item data" )
   )
//   val itemImage = new VBox:
//      spacing = 5
//      val imageChooser = FileChooser()

   val addItemContainer = new VBox:
      children = Array( itemName, itemDesc, itemQuantity, itemCategory, itemRate, addItemBtn )
      visible = false
      margin = Insets( 20 )
      spacing = 10

   def resetItemPage() =
      nameTextField.clear()
      descTextField.clear()
      rateTextField.clear()
      catChoiceBox.getSelectionModel.clearSelection()