import scala.collection.mutable.Set

class RentalStore:
  private val ownedItems = Set[Item]()
  private val bookings = Set[Booking]()
  
  def addItem(item: Item) = ownedItems += item
  
  def deleteItem(item: Item) = ownedItems -= item
  
  def getAvailableItems = ownedItems
  