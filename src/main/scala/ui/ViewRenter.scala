package ui

import logic.Renter.renters
import scalafx.geometry.Insets
import scalafx.geometry.Pos.BaselineLeft
import scalafx.scene.control.{ChoiceBox, Label, ScrollPane}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.text.FontWeight.Bold
import scalafx.scene.text.{Font, FontWeight}
import ui.utils.RenterPicker

import scala.collection.mutable.Buffer

object ViewRenter:
   private val renterPicker = RenterPicker( renters )

   // renter name
   private val nameLabel = Label( "Name: " )
   nameLabel.font = Font( null, FontWeight( Bold ), 14 )

   // renter email
   private val emailLabel = Label( "Email: " )
   emailLabel.font = Font( null, FontWeight( Bold ), 14 )

   // renter's rented items
   private val rentedLabel = Label( "Rented Items:" )
   rentedLabel.font = Font( null, FontWeight( Bold ), 14 )

   // details container
   private val detailsContainer = new VBox()
   detailsContainer.padding = Insets( 20, 0, 0, 0 )
   detailsContainer.spacing = 5

   // group containers into one
   private val pageContainer = VBox( renterPicker.container, detailsContainer )

   // display relevant information as renter is selected
   renterPicker.picker
      .valueProperty()
      .addListener( ( observable, oldValue, newValue ) =>
         if newValue != null then renderRenterInfo( newValue )
         else detailsContainer.children.clear()
      )

   val renterPageContainer = ScrollPane()
   renterPageContainer.setContent( pageContainer )
   renterPageContainer.setPadding( Insets( 20 ) )
   renterPageContainer.setVisible( false )

   /** get and generate renter details */
   private def renderRenterInfo( selectedRenter: String ) =
      val renter = renters.find( _.name == selectedRenter )
      renter match
         case Some( r ) =>
            // display renter name
            val renterName = HBox( nameLabel, Label( r.name ) )
            renterName.alignment = BaselineLeft

            // display renter email
            val renterEmail = HBox( emailLabel, Label( r.email ) )
            renterEmail.alignment = BaselineLeft

            // generate list of renter's rented items
            val rentedItems =
               if r.getRentedItems.isEmpty then Buffer( Label( "No item rented" ) )
               else r.getRentedItems.map( i => Label( i.name ) )

            // container for rented items list
            val itemsList = new VBox:
               children = rentedItems
               padding = Insets( 0, 0, 0, 10 )

            // add all label fields to container
            detailsContainer.children = Array( renterName, renterEmail, rentedLabel, itemsList )
         case _         => detailsContainer.children = Label( "This user doesn't exist." )

   def resetRenterPage() =
      renterPicker.picker.getSelectionModel.clearSelection()