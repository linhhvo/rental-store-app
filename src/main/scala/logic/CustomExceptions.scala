package logic

case class InvalidInput ( msg: String ) extends Exception(msg)

case class DuplicateItemCategory( msg: String ) extends Exception( msg )

case class ItemDoesNotExist( msg: String ) extends Exception( msg )

case class DuplicateItem( msg: String ) extends Exception( msg )

case class MissingData( msg: String ) extends Exception( msg )

case class InvalidDateTime( msg: String ) extends Exception( msg )

case class ItemNotAvailable( msg: String ) extends Exception( msg )

