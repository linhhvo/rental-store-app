package ui

import logic.*
import logic.ItemList.activeItems
import logic.ItemCategory.itemCategoryList
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
import ui.Main.stage
import ui.util.Helper.displayAlert
import ui.util.Validator.{validateLongTextInput, validateNumInput, validateShortTextInput}

import java.io.File

object AddItem:
   // pre define sizes for formfields
   private val lgWidth = 350
   private val medWidth = 155
   private val smWidth = 60

   /* ITEM NAME */

   // text field to enter item name
   private val nameTextField = new TextField:
      maxWidth = lgWidth

   // item name group with label
   private val nameLabel = new Label:
      text = "Name"
      font = Font( null, FontWeight( Black ), 14 )

   private val itemName = new VBox:
      spacing = 5
      children = Array( nameLabel, nameTextField )

   /* ITEM DESCRIPTION */

   // text field to enter item description
   private val descTextField = new TextArea:
      maxWidth = lgWidth
      maxHeight = medWidth
      wrapText = true

   // item desc group with label
   private val descLabel = new Label:
      text = "Description"
      font = Font( null, FontWeight( Bold ), 14 )

   private val itemDesc = new VBox:
      spacing = 5
      children = Array( descLabel, descTextField )

   /* ITEM QUANTITY */

   // item quantity selection, initial amount is 1
   private val qntSpinner = new Spinner( 1, 100, 1, 1 ):
      maxWidth = medWidth
      editable = true

   // item quanity group with label
   private val qntLabel = new Label:
      text = "Quantity"
      font = Font( null, FontWeight( Bold ), 14 )

   private val itemQuantity = new VBox:
      spacing = 5
      children = Array( qntLabel, qntSpinner )

   /* ITEM RENTAL RATE */

   // item rental rate per hour
   private val rphLabel = Label( "Rate Per Hour" )
   private val rphTextField = new TextField:
      maxWidth = smWidth

   // item rental rate per day
   private val rpdLabel = Label( "Rate Per Day" )
   private val rpdTextField = new TextField:
      maxWidth = smWidth

   // item rental rate per week
   private val rpwLabel = Label( "Rate Per Week" )
   private val rpwTextField = new TextField:
      maxWidth = smWidth

   // container for all rental rates
   private val rateLabel = new Label:
      text = "Rental Rate"
      font = Font( null, FontWeight( Bold ), 14 )

   private val itemRate = new GridPane:
      hgap = 5
      vgap = 5
      margin = Insets( 5, 0, 0, 0 )

   itemRate.add( rateLabel, 0, 0, 2, 1 )
   itemRate.add( rphLabel, 0, 1 )
   itemRate.add( rphTextField, 1, 1 )
   itemRate.add( rpdLabel, 0, 2 )
   itemRate.add( rpdTextField, 1, 2 )
   itemRate.add( rpwLabel, 0, 3 )
   itemRate.add( rpwTextField, 1, 3 )

   /* ITEM IMAGE */

   private val imageChooser = new FileChooser:
      title = "Select an image"
      extensionFilters.add( ExtensionFilter( "Images", Seq( "*.jpg", "*.png" ) ) )

   // initialize the selected image file
   private var selectedImage = File( "placeholder" )

   // label to show user message if selecting image was successful or not
   private val imageLabel = Label( null )

   private val uploadImageBtn = new Button:
      text = "Select image"
      onAction = ( ( e ) =>
         selectedImage = imageChooser.showOpenDialog( stage )
         if selectedImage != null && selectedImage.getName != "placeholder" then
            imageLabel.text = s"Image ${selectedImage.getName} has been added"
         else imageLabel.text = "No image has been added"
      )

   // group image chooser and label in container
   private val itemImage = new HBox:
      children = Array( uploadImageBtn, imageLabel )
      spacing = 5
      alignment = BaselineLeft
      margin = Insets( 10, 0, 0, 0 )

   /* ADD BUTTON */

   // set up alert to display user messages
   private val messageAlert = new Alert( Information )

   private val addItemBtn = new Button:
      text = "Add Item"
      margin = Insets( 15, 0, 0, 0 )

   // get category list, parent item list, and container
   private var dataToReset = renderAddItemPage

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
                if dataToReset._2.getValue == null then ItemList.addItem(newItem, None)
                else // if parent item is selected
                   ItemList.addItem(newItem, Some(dataToReset._2.getValue))

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

   // scroll container for add item page
   val addItemScroll = new ScrollPane:
      content = dataToReset._3
      visible = false
      padding = Insets( 20 )

   /** Render page<br>
     * Update item category list data and parent item list data when there are changes from adding/deleting category or adding/deleting item
     */
   private def renderAddItemPage =
      /* ITEM CATEGORY */

      // drop down menu to select item category
      val categoryList = ObservableBuffer.empty[String]
      categoryList ++= itemCategoryList
      val catChoiceBox = new ChoiceBox[String]:
         items = categoryList
         maxWidth = medWidth

      // item category list group with label
      val catLabel = new Label:
         text = "Category"
         font = Font( null, FontWeight( Bold ), 14 )

      val itemCategory = new VBox:
         spacing = 5
         children = Array( catLabel, catChoiceBox )

      /* ITEM DEPENDENCY */

      // get a list of existing item names as potential parent item
      val parentList = ObservableBuffer.empty[String]
      parentList ++= activeItems.map( item => item.name )
      // drop down menu
      val depChoiceBox = new ChoiceBox[String]:
         items = parentList
         maxWidth = medWidth

      // group drop down parent item list menu and label
      val parentLabel = new Label:
         text = "Parent Item"
         font = Font( null, FontWeight( Bold ), 14 )

      val itemDependencies = new VBox:
         spacing = 5
         children = Array( parentLabel, depChoiceBox )

      // overall container
      val addItemContainer = new VBox:
         children = Array(
             itemName,
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

   /** Reset page<br>
     * Clear entered information from input fields
     * Update data and re-render container
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

   /** Check if all input fields have information */
   private def isAllFieldsNotEmpty =
      nameTextField.getText.nonEmpty
         && descTextField.getText.nonEmpty
         && dataToReset._1.getSelectionModel.getSelectedItem != null
         && rphTextField.getText.nonEmpty
         && rpdTextField.getText.nonEmpty
         && rpwTextField.getText.nonEmpty
         && selectedImage != null && selectedImage.getName != "placeholder"