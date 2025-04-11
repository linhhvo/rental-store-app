package ui

import logic.enums.IncidentType.Loss
import logic.Item
import logic.RentalStore.inventory
import scalafx.geometry.Insets
import scalafx.geometry.Orientation.Horizontal
import scalafx.geometry.Pos.BaselineLeft
import scalafx.scene.control.*
import scalafx.scene.layout.Priority.Always
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.text.FontWeight.Bold
import scalafx.scene.text.{Font, FontWeight}
import ui.util.Helper

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.collection.mutable.Buffer

object ReportPage:
   /** Time interval filter */
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

   /** button to display summary reports */
   val generateReportBtn = new Button:
      text = "Generate Reports"
      margin = Insets( 0, 0, 0, 10 )

   /** group date pickers and button into a container */
   val filterContainer = new HBox:
      children = Array( fromLabel, fromDate, toLabel, toDate, generateReportBtn )
      spacing = 10
      margin = Insets( 20 )
      alignment = BaselineLeft

   /** button action to generate reports based on selected date range filter */
   generateReportBtn.onAction = ( ( e ) =>
      val filterStart = fromDate.getValue
      val filterEnd = toDate.getValue

      val itemsByCategory = inventory.groupBy( _.category )

      /** Populate incidents report */
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

      // set content for incident tab container
      incidentsReport.content = incidentSummaryContainer
   )

   /** Tab container for reports */
   private val financeReport = new Tab:
      text = "Finance"

   private val incidentsReport = new Tab:
      text = "Incidents"

   private val tabContainer = new TabPane:
      tabs = Seq( financeReport, incidentsReport )
      vgrow = Always

   /** Container to group filter and tab container */
   private val pageContainer = VBox( filterContainer, tabContainer )

   /** Page scroll container */
   val reportPageContainer = new ScrollPane:
      content = pageContainer
      fitToWidth = true
      fitToHeight = true
      visible = false

   /** create a container to display incident information for an item */
   private def incidentsByItem( targetItem: Item, start: LocalDate, end: LocalDate ): Buffer[VBox] =
      // if filter is empty, get all inciddents
      // otherwise, get incidents that are within the filter date range inclusively
      val incidents =
         if start == null || end == null then targetItem.incidents.toBuffer
         else
            targetItem.incidents
               .filter( inc =>
                  ( inc.incidentDate.isAfter( start )
                     || inc.incidentDate.isEqual( start ) )
                     && ( inc.incidentDate.isBefore( end )
                        || inc.incidentDate.isEqual( end ) )
               )
               .toBuffer

      // generate container to display incident details for each incident
      incidents.map( inc =>
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
            margin = Insets( 12, 0, 0, 15 )
      )

   /** create a container to display items and their incidents for an item category */
   private def incidentsByCategory( items: Buffer[Item],
                                    start: LocalDate,
                                    end:   LocalDate
   ): Buffer[VBox] =
      items.map( i =>
         val filteredIncidents = incidentsByItem( i, start, end )
         if filteredIncidents.nonEmpty then
            new VBox:
               children = Array( Label( "Item " + i.name ) ) ++ filteredIncidents
               margin = Insets( 0, 0, 5, 0 )
         else
            new VBox:
               managed = false
               visible = false
      )

   /** clear filter date fields and clear report content */
   def resetReportPage() =
      toDate.value = null
      fromDate.value = null
      incidentsReport.content = null