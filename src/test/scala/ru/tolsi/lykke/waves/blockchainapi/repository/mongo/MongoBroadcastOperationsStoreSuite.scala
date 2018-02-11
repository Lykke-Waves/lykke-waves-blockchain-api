package ru.tolsi.lykke.waces.blockchainapi.repository.mongo

import com.github.fakemongo.Fongo
import com.mongodb.casbah.MongoCollection
import org.scalatest.{AsyncFunSuite, BeforeAndAfterEach, Matchers}
import ru.tolsi.lykke.waves.blockchainapi.repository.BroadcastOperation
import ru.tolsi.lykke.waves.blockchainapi.repository.mongo.MongoBroadcastOperationsStore


class MongoBroadcastOperationsStoreSuite extends AsyncFunSuite with Matchers with BeforeAndAfterEach {
  val fongo = new Fongo("test mongo server")

  val dbName = "test"

  test("MongoBroadcastOperationsStore.addBroadcastOperation should return true if not exists") {
    val db = fongo.getDB(dbName)
    val store = new MongoBroadcastOperationsStore(new MongoCollection(db.getCollection("assets")))
    store.addBroadcastOperation(BroadcastOperation("i1", "s1")).map(_ shouldBe true)
  }

  test("MongoBroadcastOperationsStore.addObservation should return false if exists") {
    val db = fongo.getDB(dbName)
    val store = new MongoBroadcastOperationsStore(new MongoCollection(db.getCollection("assets")))
    store.addBroadcastOperation(BroadcastOperation("i1", "s1")).flatMap(_ =>
      store.addBroadcastOperation(BroadcastOperation("i1", "s1")).map(_ shouldBe false))
  }

  test("MongoBroadcastOperationsStore.addObservation should return false if exists by signed tx") {
    val db = fongo.getDB(dbName)
    val store = new MongoBroadcastOperationsStore(new MongoCollection(db.getCollection("assets")))
    store.addBroadcastOperation(BroadcastOperation("i1", "s1")).flatMap(_ =>
      store.addBroadcastOperation(BroadcastOperation("i2", "s1")).map(_ shouldBe false))
  }

  test("MongoBroadcastOperationsStore.removeBroadcastOperation should return true if exists") {
    val db = fongo.getDB(dbName)
    val store = new MongoBroadcastOperationsStore(new MongoCollection(db.getCollection("assets")))

    store.addBroadcastOperation(BroadcastOperation("i1", "s1")).flatMap(_ =>
      store.removeBroadcastOperation("i1").map(_ shouldBe true))
  }

  test("MongoBroadcastOperationsStore.removeBroadcastOperation should return false if not exists") {
    val db = fongo.getDB(dbName)
    val store = new MongoBroadcastOperationsStore(new MongoCollection(db.getCollection("assets")))

    store.removeBroadcastOperation("i1").map(_ shouldBe false)
  }

  override protected def afterEach(): Unit = {
    fongo.dropDatabase(dbName)
  }
}