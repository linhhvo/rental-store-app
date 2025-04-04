package ui

import logic.Renter.renters
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.{ChoiceBox, Label, ScrollPane}
import scalafx.scene.layout.VBox
import scalafx.scene.text.FontWeight.Bold
import scalafx.scene.text.{Font, FontWeight}

import scala.collection.mutable.Buffer

object ViewRenter:

   private val pickerLabel = Label( "Select renter" )
   pickerLabel.font = Font( null, FontWeight( Bold ), 14 )

   // get list of renters
   private val renterList = new ObservableBuffer[String]()
   renterList ++= renters.map( r => r.name )

   // dropdown menu to select renter
   private val renterPicker = ChoiceBox( renterList )

   // group renter selection with label
   private val pickerContainer = new VBox:
      children = Array( pickerLabel, renterPicker )
      spacing = 5

   // renter name
   private val nameLabel = Label( "Name" )
   nameLabel.font = Font( null, FontWeight( Bold ), 14 )

   // renter email
   private val emailLabel = Label( "Email" )
   emailLabel.font = Font( null, FontWeight( Bold ), 14 )

   // renter's rented items
   private val rentedLabel = Label( "Rented Items" )
   rentedLabel.font = Font( null, FontWeight( Bold ), 14 )

   // details container
   private val detailsContainer = new VBox()
   detailsContainer.padding = Insets( 20, 0, 0, 0 )
   detailsContainer.spacing = 5

   // group containers into one
   private val pageContainer = VBox( pickerContainer, detailsContainer )

   // display relevant information as renter is selected
   renterPicker
      .valueProperty()
      .addListener( ( observable, oldValue, newValue ) => renderRenterInfo( newValue ) )

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
            val renterName = Label( r.name )
            renterName.padding = Insets( 0, 0, 0, 10 )

            // display renter email
            val renterEmail = Label( r.email )
            renterEmail.padding = Insets( 0, 0, 0, 10 )

            // generate list of renter's rented items
            val rentedItems =
               if r.getRentedItems.isEmpty then Buffer( Label( "No item rented" ) )
               else r.getRentedItems.map( i => Label( i.name ) )

            // container for rented items list
            val itemsList = new VBox:
               children = rentedItems
               padding = Insets( 0, 0, 0, 10 )

            // add all label fields to container
            detailsContainer.children =
               Array( nameLabel, renterName, emailLabel, renterEmail, rentedLabel, itemsList )
         case _         => detailsContainer.children = Label( "This user doesn't exist." )