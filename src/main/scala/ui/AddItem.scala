package ui

import logic.*
import logic.ItemCategory.itemCategoryList
import logic.RentalStore.activeItems
import logic.util.{DuplicateItem, InvalidInput, ItemDoesNotExist}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.geometry.Pos.BaselineLeft
import scalafx.scene.control.*
import scalafx.scene.control.Alert.AlertType.Information
import scalafx.scene.layout.{GridPane, HBox, VBox}
import scalafx.scene.text.FontWeight.{Black, Bold}
import scalafx.scene.text.{Font, FontWeight}
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter
import ui.util.Helper.displayAlert
import ui.Main.stage
import ui.util.Validator.{validateLongTextInput, validateNumInput, validateShortTextInput}

import java.io.File

object AddItem:
   // pre define sizes for formfields
   private val lgWidth = 350
   private val medWidth = 155
   private val smWidth = 60

   /** ITEM NAME */

   // text field to enter item name
   private val nameTextField = TextField()
   nameTextField.maxWidth = lgWidth

   // item name group with label
   private val nameLabel = Label( "Name" )
   nameLabel.font = Font( null, FontWeight( Black ), 14 )
   private val itemName = new VBox:
      spacing = 5
      children = Array( nameLabel, nameTextField )

   /** ITEM DESCRIPTION */

   // text field to enter item description
   private val descTextField = TextArea()
   descTextField.maxWidth = lgWidth
   descTextField.maxHeight = medWidth
   descTextField.wrapText = true

   // item desc group with label
   private val descLabel = Label( "Description" )
   descLabel.font = Font( null, FontWeight( Bold ), 14 )
   private val itemDesc = new VBox:
      spacing = 5
      children = Array( descLabel, descTextField )

   /** ITEM QUANTITY */

   // item quantity selection, initial amount is 1
   private val qntSpinner = Spinner( 1, 100, 1, 1 )
   qntSpinner.maxWidth = medWidth
   qntSpinner.editable = true

   // item quanity group with label
   private val qntLabel = Label( "Quantity" )
   qntLabel.font = Font( null, FontWeight( Bold ), 14 )
   private val itemQuantity = new VBox:
      spacing = 5
      children = Array( qntLabel, qntSpinner )

   /** ITEM RENTAL RATE */

   // item rental rate per hour
   private val rphLabel = Label( "Rate Per Hour" )
   private val rphTextField = TextField()
   rphTextField.maxWidth = smWidth

   // item rental rate per day
   private val rpdLabel = Label( "Rate Per Day" )
   private val rpdTextField = TextField()
   rpdTextField.maxWidth = smWidth

   // item rental rate per week
   private val rpwLabel = Label( "Rate Per Week" )
   private val rpwTextField = TextField()
   rpwTextField.maxWidth = smWidth

   // container for all rental rates
   private val rateLabel = Label( "Rental Rate" )
   rateLabel.font = Font( null, FontWeight( Bold ), 14 )

   private val itemRate = GridPane()
   itemRate.add( rateLabel, 0, 0, 2, 1 )
   itemRate.add( rphLabel, 0, 1 )
   itemRate.add( rphTextField, 1, 1 )
   itemRate.add( rpdLabel, 0, 2 )
   itemRate.add( rpdTextField, 1, 2 )
   itemRate.add( rpwLabel, 0, 3 )
   itemRate.add( rpwTextField, 1, 3 )
   itemRate.hgap = 5
   itemRate.vgap = 5
   itemRate.margin = Insets( 5, 0, 0, 0 )

   /** ITEM IMAGE */

   private val imageChooser = FileChooser()
   imageChooser.title = "Select an image"
   imageChooser.extensionFilters.add( ExtensionFilter( "Images", Seq( "*.jpg", "*.png" ) ) )

   // initialize the selected image file
   private var selectedImage = File( "placeholder" )

   // label to show user message if selecting image was successful or not
   private val imageLabel = Label( null )

   private val uploadImageBtn = Button( "Select image" )
   uploadImageBtn.onAction = ( ( e ) =>
      // prompt user to select an image from local storage
      selectedImage = imageChooser.showOpenDialog( stage )
      // display user message
      if selectedImage != null && selectedImage.getName != "placeholder" then
         imageLabel.text = s"Image ${selectedImage.getName} has been added"
      else imageLabel.text = "No image has been added"
   )

   // group image chooser and label in container
   private val itemImage = new HBox( uploadImageBtn, imageLabel )
   itemImage.spacing = 5
   itemImage.alignment = BaselineLeft
   itemImage.margin = Insets( 10, 0, 0, 0 )

   /** ADD BUTTON */

   private val addItemBtn = Button( "Add Item" )
   addItemBtn.margin = Insets( 15, 0, 0, 0 )

   // set up alert to display user messages
   private val messageAlert = new Alert( Information )

   // get category list, parent item list, and container
   private var dataToReset = renderAddItemPage

   // action to add new item to system
   addItemBtn.onAction = (
       ( e ) =>
          // check that all information is entered
          if isAllFieldsNotEmpty
          then // create new item
             try
                val newItem = Item(
                    validateShortTextInput( nameTextField.getText, "Item Name" ),
                    validateLongTextInput( descTextField.getText, "Item Description" ),
                    qntSpinner.getValue,
                    dataToReset._1.getValue,
                    validateNumInput( rphTextField.getText, "Rate Per Hour" ),
                    validateNumInput( rpdTextField.getText, "Rate Per Day" ),
                    validateNumInput( rpwTextField.getText, "Rate Per Week" )
                )

                // if no parent item is selected, add item without parent
                if dataToReset._2.getValue == null then RentalStore.addItem( newItem, None )
                else // if parent item is selected
                   RentalStore.addItem( newItem, Some( dataToReset._2.getValue ) )

                // copy selected image to resources folder
                val newImage = File(
                    nameTextField.getText.trim.toLowerCase + ".png"
                )
                os.copy.over( os.Path( selectedImage ),
                              os.pwd / os.RelPath( "src/main/resources/images/" + newImage.getName )
                )

                // display success message to user and clear page
                displayAlert( messageAlert, "Item added to the system" )
                resetItemPage()
             catch
                case InvalidInput( m )       => displayAlert( messageAlert, m )
                // if item category does not exist in the system
                case ItemDoesNotExist( msg ) =>
                   displayAlert( messageAlert, msg )
                // if new item already exists in the system
                case DuplicateItem( msg )    =>
                   displayAlert( messageAlert, msg )
                case _                       => displayAlert( messageAlert, "Cannot add item" )
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
   private def renderAddItemPage =
      /** ITEM CATEGORY */

      // drop down menu to select item category
      val categoryList = new ObservableBuffer[String]()
      categoryList ++= getItemCategoryList
      val catChoiceBox = ChoiceBox( categoryList )
      catChoiceBox.maxWidth = medWidth

      // item category list group with label
      val catLabel = Label( "Category" )
      catLabel.font = Font( null, FontWeight( Bold ), 14 )
      val itemCategory = new VBox:
         spacing = 5
         children = Array( catLabel, catChoiceBox )

      /** ITEM DEPENDENCY */

      // get a list of existing item names as potential parent item
      val parentList = new ObservableBuffer[String]()
      parentList ++= activeItems.map( item => item.name )
      // drop down menu
      val depChoiceBox = ChoiceBox( parentList )
      depChoiceBox.maxWidth = medWidth

      // group drop down parent item list menu and label
      val parentLabel = Label( "Parent Item" )
      parentLabel.font = Font( null, FontWeight( Bold ), 14 )
      val itemDependencies = new VBox:
         spacing = 5
         children = Array( parentLabel, depChoiceBox )

      // overall container
      val addItemContainer = new VBox:
         children = Array( itemName,
                           itemDesc,
                           itemQuantity,
                           itemCategory,
                           itemRate,
                           itemDependencies,
                           itemImage,
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
      imageLabel.text = null

   /** function to check if all input fields have information */
   private def isAllFieldsNotEmpty =
      nameTextField.getText.nonEmpty
         && descTextField.getText.nonEmpty
         && dataToReset._1.getSelectionModel.getSelectedItem != null
         && rphTextField.getText.nonEmpty
         && rpdTextField.getText.nonEmpty
         && rpwTextField.getText.nonEmpty
         && selectedImage != null && selectedImage.getName != "placeholder"