package logic

import upickle.default.*

import scala.collection.mutable.Buffer

object IOHandler:
   val dataFilePath = "src/main/resources/data/"
   
   def getDataFromFile(fileName: String) = 
      ujson.read( os.read( os.pwd / os.RelPath( dataFilePath + fileName + ".json") ) )