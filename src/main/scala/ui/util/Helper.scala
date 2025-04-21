package ui.util

import logic.Item
import scalafx.scene.Node
import scalafx.scene.SceneIncludes.jfxNode2sfx
import scalafx.scene.control.*
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.Pane
import scalafx.util.StringConverter

import java.io.FileInputStream
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

object Helper:

   /** Converter to change how date is displayed in date picker */
   val dateStrConverter = new StringConverter[LocalDate]:
      val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern( "dd-MM-yyyy" )

      override def toString( date: LocalDate ): String =
         if date != null then dateFormatter.format( date ) else ""

      override def fromString( string: String ): LocalDate =
         if string != null && string.nonEmpty then LocalDate.parse( string, dateFormatter )
         else null

   /** Show a page in the stack pane while hiding the rest */
   def displayPage( parentContainer: Pane, targetPage: Node ) =
      parentContainer.children.foreach( _.visible = false )
      targetPage.visible = true

   /** Toggle visibility of a node */
   def toggleDisplay( element: Node ) =
      if element.visible.get() then
         element.visible = false
         element.managed = false
      else
         element.visible = true
         element.managed = true

   /** Show pop-up alert with the appropriate message */
   def displayAlert( alert: Alert, msg: String ) =
      alert.headerText = msg
      alert.showAndWait()

   /** Get item image view */
   def getItemImageView( targetItem:      Item,
                         resourceDirPath: String = "src/main/resources/images/"
   ): Node =
      try
         val itemImage = FileInputStream( resourceDirPath + targetItem.image )
         ImageView( Image( itemImage, 0, 200, true, true ) )
      catch
         case _ =>
            Label( s"Cannot find image for item ${targetItem.name}" )

   /** Combine date and time inputs and convert to LocalDateTime */
   def getDateTime( date: DatePicker, time: ChoiceBox[String] ) =
      val dateTimeStr = date.getValue.toString + " " + time.getValue
      LocalDateTime.parse( dateTimeStr, DateTimeFormatter.ofPattern( "yyyy-MM-dd H:m" ) )