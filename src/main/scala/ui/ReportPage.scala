package ui

import logic.ItemList.inventory
import logic.Item
import logic.enums.IncidentType.{Damage, Loss}
import scalafx.geometry.Insets
import scalafx.geometry.Orientation.Horizontal
import scalafx.geometry.Pos.BaselineLeft
import scalafx.scene.control.*
import scalafx.scene.control.TabPane.TabClosingPolicy.Unavailable
import scalafx.scene.layout.Priority.Always
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.text.FontWeight.Bold
import scalafx.scene.text.{Font, FontWeight}
import ui.util.Helper
import ui.util.Helper.displayAlert

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.collection.mutable.Buffer

object ReportPage:

   private val msgAlert = new Alert( Alert.AlertType.Information )

   /* TIME INTERVAL FILTER */
   private val fromLabel = new Label:
      text = "From"
      font = Font( null, FontWeight( Bold ), 14 )

   private val toLabel = new Label:
      text = "To"
      font = Font( null, FontWeight( Bold ), 14 )

   private val fromDate = new DatePicker:
      maxWidth = 125
      converter = Helper.dateStrConverter
      editable = false

   private val toDate = new DatePicker:
      maxWidth = 125
      converter = Helper.dateStrConverter
      editable = false

   // button to display summary reports
   val generateReportBtn = new Button:
      text = "Generate Reports"
      margin = Insets( 0, 0, 0, 10 )

   // group date pickers and button into a container
   val filterContainer = new HBox:
      children = Array( fromLabel, fromDate, toLabel, toDate, generateReportBtn )
      spacing = 10
      margin = Insets( 20 )
      alignment = BaselineLeft

   /* TAB CONTAINER FOR REPORTS */
   private val financeReport = new Tab:
      text = "Finance"

   private val incidentsReport = new Tab:
      text = "Incidents"

   private val tabContainer = new TabPane:
      vgrow = Always
      tabClosingPolicy = Unavailable

   // container to group filter and tab container
   private val pageContainer = VBox( filterContainer, tabContainer )

   // page scroll container
   val reportPageContainer = new ScrollPane:
      content = pageContainer
      fitToWidth = true
      visible = false

   // button action to generate reports based on a selected date range filter
   generateReportBtn.onAction = ( ( e ) =>
      val filterStart = fromDate.getValue
      val filterEnd = toDate.getValue

      if filterStart != null && filterEnd != null && filterStart.isAfter( filterEnd ) then
         displayAlert( msgAlert, "Invalid date range" )
      else
         val itemsByCategory = inventory.groupBy( _.category )

         // populate incidents report
         val incidentSummaryByCategory = itemsByCategory.map( ( cat, items ) =>
            val incidents = incidentsByCategory( items, filterStart, filterEnd )

            if incidents.exists( _.children.nonEmpty ) then
               new VBox:
                  children = Array( Label( "Category " + cat ) )
                     ++ incidents ++ Array( Separator( Horizontal ) )
                  spacing = 10
                  maxWidth = 550
                  margin = Insets( 10, 0, 0, 0 )
            else
               new VBox:
                  managed = false
                  visible = false
         )

         val incidentSummaryContainer = new VBox:
            children = incidentSummaryByCategory
            margin = Insets( 10, 0, 0, 20 )

         if incidentSummaryByCategory.forall( _.children.isEmpty ) then
            incidentSummaryContainer.children = Array(
                Label( "There is no incident in the selected date range." )
            )

         // set content for incident tab container
         incidentsReport.content = incidentSummaryContainer

         // populate financial report
         val financeSummaryByCategory = itemsByCategory.map( ( cat, items ) =>
            new VBox:
               children = Array( Label( "Category " + cat ) )
                  ++ financeByItem( items, filterStart, filterEnd )
                  ++ Array( Separator( Horizontal ) )
               spacing = 10
               margin = Insets( 10, 0, 0, 0 )
         )

         val financeSummaryContainer = new VBox:
            children = financeSummaryByCategory
            margin = Insets( 10, 0, 0, 20 )

         // set content for finance tab container
         financeReport.content = financeSummaryContainer

         // add tabs to TabPane
         tabContainer.tabs = Seq( financeReport, incidentsReport )
   )

   /** Create a container to display incident information for an item */
   private def incidentsByItem( targetItem: Item, start: LocalDate, end: LocalDate ): Buffer[VBox] =
      // if the filter is empty, get all incidents
      // otherwise, get incidents that are within the filter date range inclusively
      val filteredIncidents = targetItem.incidentsByDates( start, end )

      // generate a container to display incident details for each incident
      filteredIncidents.map( inc =>
         val incType = Label( "Type: " + inc.incidentType )
         val incDate = Label(
             "Date: " + inc.incidentDate.format( DateTimeFormatter.ofPattern( "dd-MM-yyyy" ) )
         )
         val incQnt = Label( "Quantity: " + inc.affectedQuantity )
         val incDetails = new Label:
            text = "Details: " + inc.comment
            wrapText = true
            prefWidth = 500

         val incCost =
            if inc.incidentType == Loss then Label( "Replacement Cost: " + inc.repairCost )
            else Label( "Repair Cost: " + inc.repairCost )

         new VBox:
            children = Array( incType, incDate, incQnt, incDetails, incCost )
            margin = Insets( 12, 0, 0, 20 )
      )

   /** Create a container to display items and their incidents for an item category */
   private def incidentsByCategory( items: Buffer[Item],
                                    start: LocalDate,
                                    end:   LocalDate
   ): Buffer[VBox] =
      items.map( i =>
         val filteredIncidents = incidentsByItem( i, start, end )
         val nameLabel = new Label:
            text = "Item " + i.name
            margin = Insets( 0, 0, 0, 10 )

         if filteredIncidents.nonEmpty then
            new VBox:
               children = Array( nameLabel ) ++ filteredIncidents
               margin = Insets( 0, 0, 5, 0 )
         else
            new VBox:
               managed = false
               visible = false
      )

   /** Create a container to display all finance information for an item */
   private def financeByItem( items: Buffer[Item], start: LocalDate, end: LocalDate ) =
      items.map( i =>
         val nameLabel = new Label:
            text = i.name + " - " + i.status
            margin = Insets( 0, 0, 0, -10 )

         new VBox:
            children = Array(
                nameLabel,
                Label( "Demand: " + i.totalDemand( start, end ) + " unit(s)" ),
                Label( "Income: " + i.totalIncome( start, end ) ),
                Label( "Repair Cost: " + i.totalExpense( Damage, start, end ) ),
                Label( "Replacement Cost: " + i.totalExpense( Loss, start, end ) )
            )
            spacing = 5
            margin = Insets( 0, 0, 5, 20 )
      )

   /** Clear filter date fields and clear report content */
   def resetReportPage() =
      toDate.value = null
      fromDate.value = null
      tabContainer.tabs = Seq()