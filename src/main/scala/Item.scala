import java.io.File
import scala.collection.mutable.Set

class Item(val name: String, val desc: String, val ownedQuantity: Int, val category: ItemCategory, val ratePerHour: Double) :
   private var itemName = name
   private var itemDesc = desc
   private var totalQuantity = ownedQuantity
   private var availableQuantity = ownedQuantity
   private var itemCategory = category
   private var itemRate = ratePerHour
   private val itemId = "item1"
   private val incidents = Set[Incident]() 
   private val dependencies = Set[Item]()
   private val images = Set[File]()
   private val comments = Set[String]()
   
   def addComment(newComment: String) =
      comments += newComment
      
   def reportIncident(newIncident: Incident) =
      incidents += newIncident
      if newIncident.incidentType == IncidentType.Loss
         then this.makeUnavailable()
      
   def addDependency(childItem: Item) =
      dependencies += childItem

   def isAvailable: Boolean =
      availableQuantity > 0
      
//   decrease quantity when item is rented out or reported loss 
   def makeUnavailable ( ) = 
      availableQuantity -= 1
      
//   increase quantity when item is returned
   def freeItem () =
      availableQuantity += 1
      
   