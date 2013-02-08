package io.prediction.commons.appdata

import org.specs2._
import org.specs2.specification.Step

import com.mongodb.casbah.Imports._
import org.scala_tools.time.Imports._

class ItemsSpec extends Specification { def is =
  "PredictionIO App Data Items Specification"                                 ^
                                                                              p ^
  "Items can be implemented by:"                                              ^ endp ^
    "1. MongoItems"                                                           ^ mongoItems ^ end

  def mongoItems =                                                            p ^
    "MongoItems should"                                                       ^
      "behave like any Items implementation"                                  ^ items(newMongoItems) ^
                                                                              Step(MongoConnection()(mongoDbName).dropDatabase())

  def items(items: Items) = {                                                 t ^
    "inserting and getting an item"                                           ! insert(items) ^
    "updating an item"                                                        ! update(items) ^
    "deleting an item"                                                        ! delete(items) ^
    "deleting items by appid"                                                 ! deleteByAppid(items) ^
                                                                              bt
  }

  val mongoDbName = "predictionio_appdata_mongoitems_test"
  def newMongoItems = new mongodb.MongoItems(MongoConnection()(mongoDbName))

  def insert(items: Items) = {
    val appid = 0
    val id = "insert"
    val item = Item(
      id         = id,
      appid      = appid,
      ct         = DateTime.now,
      itypes     = List("fresh", "meat"),
      startt     = Some(DateTime.now.hour(23).minute(13)),
      endt       = None,
      price      = Some(49.394),
      profit     = None,
      latlng     = Some((47.8948, -29.79783)),
      inactive   = None,
      attributes = Some(Map("foo" -> "bar"))
    )
    items.insert(item)
    items.get(appid, id) must beSome(item)
  }

  def update(items: Items) = {
    val appid = 1
    val id = "update"
    val item = Item(
      id         = id,
      appid      = appid,
      ct         = DateTime.now,
      itypes     = List("slash", "dot"),
      startt     = None,
      endt       = None,
      price      = None,
      profit     = None,
      latlng     = None,
      inactive   = None,
      attributes = Some(Map("foo" -> "baz"))
    )

    val updatedItem = item.copy(
      endt       = Some(DateTime.now.minute(47)),
      price      = Some(99.99),
      latlng     = Some((43, 48.378)),
      attributes = Some(Map("raw" -> "beef"))
    )
    items.insert(item)
    items.update(updatedItem)
    items.get(appid, id) must beSome(updatedItem)
  }

  def delete(items: Items) = {
    val appid = 2
    val id = "delete"
    val item = Item(
      id         = id,
      appid      = appid,
      ct         = DateTime.now,
      itypes     = List("fresh", "meat"),
      startt     = Some(DateTime.now.hour(23).minute(13)),
      endt       = None,
      price      = Some(49.394),
      profit     = None,
      latlng     = Some((47.8948, -29.79783)),
      inactive   = None,
      attributes = Some(Map("foo" -> "bar"))
    )
    items.delete(item)
    items.get(appid, id) must beNone
  }
  
  def deleteByAppid(items: Items) = {
    // insert a few items with appid1 and a few items with appid2.
    // delete all items of appid1.
    // items of appid1 should be deleted and items of appid2 should still exist.
    // delete all items of appid2
    // items of appid2 should be deleted
    
    val appid1 = 10
    val appid2 = 11
    
    val ida = "deleteByAppid-ida"
    val idb = "deleteByAppid-idb"
    val idc = "deleteByAppid-idc"
    
    val item1a = Item(
      id         = ida,
      appid      = appid1,
      ct         = DateTime.now,
      itypes     = List("fresh", "meat"),
      startt     = Some(DateTime.now.hour(23).minute(13)),
      endt       = None,
      price      = Some(49.394),
      profit     = None,
      latlng     = Some((47.8948, -29.79783)),
      inactive   = None,
      attributes = Some(Map("foo" -> "bar"))
    )
    val item1b = item1a.copy(
      id         = idb,
      price      = Some(1.23)
    )
    val item1c = item1a.copy(
      id         = idc,
      price      = Some(2.45)
    )
    
    val item2a = item1a.copy(
      appid      = appid2
    )
    val item2b = item1b.copy(
      appid      = appid2
    )
    val item2c = item1c.copy(
      appid      = appid2
    )
    
    items.insert(item1a)
    items.insert(item1b)
    items.insert(item1c)
    items.insert(item2a)
    items.insert(item2b)
    items.insert(item2c)
    
    val g1_1a = items.get(appid1, ida)
    val g1_1b = items.get(appid1, idb)
    val g1_1c = items.get(appid1, idc)
    
    val g1_2a = items.get(appid2, ida)
    val g1_2b = items.get(appid2, idb)
    val g1_2c = items.get(appid2, idc)
    
    items.deleteByAppid(appid1)
    
    val g2_1a = items.get(appid1, ida)
    val g2_1b = items.get(appid1, idb)
    val g2_1c = items.get(appid1, idc)
    
    val g2_2a = items.get(appid2, ida)
    val g2_2b = items.get(appid2, idb)
    val g2_2c = items.get(appid2, idc)
    
    items.deleteByAppid(appid2)
    
    val g3_2a = items.get(appid2, ida)
    val g3_2b = items.get(appid2, idb)
    val g3_2c = items.get(appid2, idc)
    
    (g1_1a, g1_1b, g1_1c) must be_==((Some(item1a), Some(item1b), Some(item1c))) and
      ((g1_2a, g1_2b, g1_2c) must be_==((Some(item2a), Some(item2b), Some(item2c)))) and
      ((g2_1a, g2_1b, g2_1c) must be_==((None, None, None))) and
      ((g2_2a, g2_2b, g2_2c) must be_==((Some(item2a), Some(item2b), Some(item2c)))) and
      ((g3_2a, g3_2b, g3_2c) must be_==((None, None, None)))
    
  }
}
