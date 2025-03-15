package ui

import scalafx.scene.control.{Button, ChoiceBox, DatePicker, ScrollPane}
import logic.SavedItems.getSavedItems
import scalafx.collections.ObservableBuffer
import scalafx.scene.layout.{HBox, VBox}

object RentItem:
   val rentItemPageContainer = ScrollPane()
   rentItemPageContainer.setVisible( false )

   def renderSavedItems =
      val savedItems = getSavedItems
      
      val hours = ObservableBuffer[Int]() ++ ( 0 to 23 ).toArray
      val mins = ObservableBuffer[Int]() ++ ( 0 to 59 ).toArray
      val rentStartDate = new DatePicker()
      rentStartDate.maxWidth = 125
      val rentStartHour = ChoiceBox( hours )
      val rentStartMin = ChoiceBox( mins )
      val rentStart = new HBox:
         spacing = 7
         children = Array( rentStartDate, rentStartHour, rentStartMin )

      val rentEndDate = new DatePicker()
      rentEndDate.maxWidth = 125
      val rentEndHour = ChoiceBox( hours )
      val rentEndMin = ChoiceBox( mins )
      val rentEnd = new HBox:
         spacing = 7
         children = Array( rentEndDate, rentEndHour, rentEndMin )

      val savedItemsContainer = new VBox():
         children = Array(rentStart, rentEnd)
      
      savedItemsContainer
//      println(
//                 rentStartDate.getValue.toString + " " + rentStartHour.getValue + ":" + rentStartMin.getValue
//             )