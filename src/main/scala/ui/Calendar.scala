package ui

import scalafx.geometry.Insets
import scalafx.scene.control.Label
import scalafx.scene.layout.HBox

object Calendar:
   /** CALENDAR PAGE */

   // Pane for calendar page
   val calendarPageContainer = HBox( Label( "calendar goes here" ) )
   calendarPageContainer.margin = Insets( 20 )
   calendarPageContainer.setVisible( false )