package logic

import upickle.default._

object IOHandler:
   val dataFilePath = "src/main/resources/data/"

   def getDataFromFile( fileName: String ) =
      ujson.read( os.read( os.pwd / os.RelPath( dataFilePath + fileName + ".json" ) ) )

   def writeItems(itemList: Iterable[ujson.Value]) =
      os.write.over( os.pwd / os.RelPath( dataFilePath ) / "items.json", write( itemList, indent = 2 )
      )