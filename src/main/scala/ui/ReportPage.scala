package ui

import scalafx.geometry.Insets
import scalafx.geometry.Pos.BaselineLeft
import scalafx.scene.control.*
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.text.FontWeight.Bold
import scalafx.scene.text.{Font, FontWeight}

object ReportPage:
   /** Time interval filter */
   private val filter = createIntervalFilter

   /** Tab container for reports */
   private val financeReport = new Tab:
      text = "Income/Expense"
      content = Label( "Income/expense" )

   private val incidentsReport = new Tab:
      text = "Incidents"
      content = Label( "incidents" )

   private val tabContainer = new TabPane:
      tabs = Seq( financeReport, incidentsReport )

   /** Container to group filter and tab container */
   private val pageContainer = VBox( filter._1, tabContainer )

   /** Page scroll container */
   val reportPageContainer = new ScrollPane:
      content = pageContainer
      fitToWidth = true
      fitToHeight = true
      visible = false

   /** create two datetime pickers to filter reports */
   private def createIntervalFilter =
      val fromLabel = new Label:
         text = "From"
         font = Font( null, FontWeight( Bold ), 14 )

      val toLabel = new Label:
         text = "To"
         font = Font( null, FontWeight( Bold ), 14 )

      val fromDate = new DatePicker:
         maxWidth = 125
         converter = Helper.converter
         editable = false

      val toDate = new DatePicker:
         maxWidth = 125
         converter = Helper.converter
         editable = false

      val container = new HBox:
         children = Array( fromLabel, fromDate, toLabel, toDate )
         spacing = 10
         margin = Insets( 20 )
         alignment = BaselineLeft

      ( container, fromDate, toDate )