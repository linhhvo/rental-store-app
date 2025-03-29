package ui

import logic.ItemCategory.getItemCategoryList
import logic.RentalStore.getAllItems
import logic.{DuplicateItem, InvalidItemCategory, Item, RentalStore}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.*
import scalafx.scene.control.Alert.AlertType.Information
import scalafx.scene.layout.{GridPane, VBox}
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter
import ui.Helper.displayAlert
import ui.Main.stage

object AddItem:
   // pre define sizes for formfields
   val big = 350
   val med = 155
   val small = 60

   /** ITEM NAME */

   // text field to enter item name
   val nameTextField = TextField()
   nameTextField.maxWidth = big

   // item name group with label
   val itemName = new VBox:
      spacing = 5
      children = Array( Label( "Name*" ), nameTextField )

   /** ITEM DESCRIPTION */

   // text field to enter item description
   val descTextField = TextArea()
   descTextField.maxWidth = big
   descTextField.maxHeight = med

   // item desc group with label
   val itemDesc = new VBox:
      spacing = 5
      children = Array( Label( "Description*" ), descTextField )

   /** ITEM QUANTITY */

   // item quantity selection, initial amount is 1
   val qntSpinner = Spinner( 1, 100, 1, 1 )
   qntSpinner.maxWidth = med
   qntSpinner.editable = true

   // item quanity group with label
   val itemQuantity = new VBox:
      spacing = 5
      children = Array( Label( "Quantity*" ), qntSpinner )

   /** ITEM RENTAL RATE */

   // item rental rate per hour
   val rphLabel = Label( "Rate Per Hour*" )
   val rphTextField = TextField()
   rphTextField.maxWidth = small

   // item rental rate per day
   val rpdLabel = Label( "Rate Per Day*" )
   val rpdTextField = TextField()
   rpdTextField.maxWidth = small

   // item rental rate per week
   val rpwLabel = Label( "Rate Per Week*" )
   val rpwTextField = TextField()
   rpwTextField.maxWidth = small

   // container for all rental rates
   val itemRate = GridPane()
   itemRate.add( rphLabel, 0, 0 )
   itemRate.add( rphTextField, 1, 0 )
   itemRate.add( rpdLabel, 0, 1 )
   itemRate.add( rpdTextField, 1, 1 )
   itemRate.add( rpwLabel, 0, 2 )
   itemRate.add( rpwTextField, 1, 2 )
   itemRate.hgap = 5
   itemRate.vgap = 5
   itemRate.margin = Insets( 5, 0, 0, 0 )

   /** ITEM IMAGE */

   val imageChooser = FileChooser()
   imageChooser.title = "Select an image"
   imageChooser.extensionFilters.add( ExtensionFilter( "JPG", "*.jpg" ) )
   val uploadImageBtn = Button( "Upload an image" )
   uploadImageBtn.onAction = ( ( e ) =>
      val image = imageChooser.showOpenDialog( stage )
      if image != null then println( "image name: " + image.getName )
      else println( "no image selected" )
   )

   /** ADD BUTTON */

   val addItemBtn = Button( "Add Item" )
   addItemBtn.margin = Insets( 15, 0, 0, 0 )

   // set up alert to display user messages
   val messageAlert = new Alert( Information )

   // get category list, parent item list, and container
   var dataToReset = renderAddItemPage

   // action to add new item to system
   addItemBtn.onAction = (
       ( e ) =>
          // check that all information is entered
          if isAllFieldsNotEmpty
          then // create new item
             val newItem = Item(
                 nameTextField.getText,
                 descTextField.getText,
                 qntSpinner.getValue,
                 dataToReset._1.getValue,
                 rphTextField.getText.toDouble,
                 rpdTextField.getText.toDouble,
                 rpwTextField.getText.toDouble
             )
             try
                // if no parent item is selected, add item without parent
                if dataToReset._2.getValue == null then RentalStore.addItem( newItem, None )
                else // if parent item is selected
                   RentalStore.addItem( newItem, Some( dataToReset._2.getValue ) )
                // display success message to user and clear page
                displayAlert( messageAlert, "Item added to the system" )
                resetItemPage()
             catch
                // if item category does not exist in the system
                case InvalidItemCategory( msg ) =>
                   displayAlert( messageAlert, msg )
                // if new item already exists in the system
                case DuplicateItem( msg )       =>
                   displayAlert( messageAlert, msg )
          // if any of the fields is missing input
          else displayAlert( messageAlert, "Missing item data" )
   )

   // Scroll container for add item page
   val addItemScroll = ScrollPane()
   addItemScroll.setContent( dataToReset._3 )
   addItemScroll.visible = false
   addItemScroll.padding = Insets( 20 )

   /** function to render page; update item category list data and parent item list data when there
     * are changes from adding/deleting category or adding/deleting item; render container
     */
   def renderAddItemPage =
      /** ITEM CATEGORY */

      // drop down menu to select item category
      val categoryList = new ObservableBuffer[String]()
      categoryList ++= getItemCategoryList
      val catChoiceBox = ChoiceBox( categoryList )
      catChoiceBox.maxWidth = med

      // item category list group with label
      val itemCategory = new VBox:
         spacing = 5
         children = Array( Label( "Category*" ), catChoiceBox )

      /** ITEM DEPENDENCY */

      // get a list of existing item names as potential parent item
      val parentList = new ObservableBuffer[String]()
      parentList ++= getAllItems.map( item => item.name )
      // drop down menu
      val depChoiceBox = ChoiceBox( parentList )
      depChoiceBox.maxWidth = med

      // group drop down parent item list menu and label
      val itemDependencies = new VBox:
         spacing = 5
         children = Array( Label( "Parent Item" ), depChoiceBox )

      // overall container
      val addItemContainer = new VBox:
         children = Array( itemName,
                           itemDesc,
                           itemQuantity,
                           itemCategory,
                           itemRate,
                           itemDependencies,
                           uploadImageBtn,
                           addItemBtn
         )
         spacing = 10

      ( catChoiceBox, depChoiceBox, addItemContainer )

   /** function to reset page clear entered information from input fields; update data and re-render
     * container
     */
   def resetItemPage() =
      nameTextField.clear()
      descTextField.clear()
      rphTextField.clear()
      rpdTextField.clear()
      rpwTextField.clear()
      dataToReset = renderAddItemPage
      addItemScroll.setContent( dataToReset._3 )

   /** function to check if all input fields have information */
   def isAllFieldsNotEmpty =
      nameTextField.getText.nonEmpty && descTextField.getText.nonEmpty && dataToReset._1.getSelectionModel.getSelectedItem != null && rphTextField.getText.nonEmpty && rpdTextField.getText.nonEmpty && rpwTextField.getText.nonEmpty