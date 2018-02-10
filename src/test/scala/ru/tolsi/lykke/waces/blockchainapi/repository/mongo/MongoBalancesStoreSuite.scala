package ru.tolsi.lykke.waces.blockchainapi.repository.mongo

import com.github.fakemongo.Fongo
import com.mongodb.casbah.MongoCollection
import org.scalatest.{AsyncFunSuite, BeforeAndAfterEach, Matchers}
import ru.tolsi.lykke.waves.blockchainapi.repository.Balance
import ru.tolsi.lykke.waves.blockchainapi.repository.mongo.MongoBalancesStore

import scala.concurrent.Future


class MongoBalancesStoreSuite extends AsyncFunSuite with Matchers with BeforeAndAfterEach {
  val fongo = new Fongo("test mongo server")

  val dbName = "test"

  test("MongoBalancesStore.addObservation should return true if not exists") {
    val db = fongo.getDB(dbName)
    val store = new MongoBalancesStore(new MongoCollection(db.getCollection("assets")), new MongoCollection(db.getCollection("assets_observations")))

    store.addObservation("abc").map(_ shouldBe true)
  }

  test("MongoBalancesStore.addObservation should return false if exists") {
    val db = fongo.getDB(dbName)
    val store = new MongoBalancesStore(new MongoCollection(db.getCollection("assets")), new MongoCollection(db.getCollection("assets_observations")))

    store.addObservation("abc").flatMap(_ =>
      store.addObservation("abc").map(_ shouldBe false))
  }

  test("MongoBalancesStore.removeObservation should return true if exists") {
    val db = fongo.getDB(dbName)
    val store = new MongoBalancesStore(new MongoCollection(db.getCollection("assets")), new MongoCollection(db.getCollection("assets_observations")))

    store.addObservation("abc").flatMap(_ =>
      store.removeObservation("abc").map(_ shouldBe true))
  }

  test("MongoBalancesStore.removeObservation should remove all the balances") {
    val db = fongo.getDB(dbName)
    val store = new MongoBalancesStore(new MongoCollection(db.getCollection("assets")), new MongoCollection(db.getCollection("assets_observations")))

    val updates = for {i <- 0 to 20} yield ("account", "asset" + i, 1L)

    val updatesF = Future.sequence(updates.zipWithIndex.map { case ((acc, asset, change), i) => store.updateBalance(acc, asset, change, i) })

    store.addObservation("account").flatMap(o => {
      o shouldBe true
      updatesF.flatMap(statuses => {
        statuses.forall(identity) shouldBe true
        store.removeObservation("account").flatMap(o => {
          o shouldBe true
          store.getBalances(10).map(_ shouldBe 'empty)
        })
      })
    })
  }

  test("MongoBalancesStore.removeObservation should return false if not exists") {
    val db = fongo.getDB(dbName)
    val store = new MongoBalancesStore(new MongoCollection(db.getCollection("assets")), new MongoCollection(db.getCollection("assets_observations")))

    store.removeObservation("abc").map(_ shouldBe false)
  }

  test("MongoBalancesStore.updateBalance should works correct") {
    val db = fongo.getDB(dbName)
    val store = new MongoBalancesStore(new MongoCollection(db.getCollection("assets")), new MongoCollection(db.getCollection("assets_observations")))

    val updates = Seq.fill(20)("account", "asset", 1L)

    val updatesF = Future.sequence(updates.zipWithIndex.map { case ((acc, asset, change), i) => store.updateBalance(acc, asset, change, i) })

    updatesF.flatMap(statuses => {
      statuses.forall(identity) shouldBe true
      store.getBalances(1).map(_.head shouldBe Balance("account", "asset", 20, 19))
    })
  }

  test("MongoBalancesStore.getBalance should return correct sized lists") {
    val db = fongo.getDB(dbName)
    val store = new MongoBalancesStore(new MongoCollection(db.getCollection("assets")), new MongoCollection(db.getCollection("assets_observations")))

    val updates = for {i <- 0 to 20} yield ("account" + (i % 10), "asset" + i, 1L)

    val updatesF = Future.sequence(updates.zipWithIndex.map { case ((acc, asset, change), i) => store.updateBalance(acc, asset, change, i) })

    updatesF.flatMap(statuses => {
      statuses.forall(identity) shouldBe true
      store.getBalances(10).map(_ shouldBe updates.zipWithIndex.sortBy(i => i._1 + "-" + i._2).take(10).map {
        case ((acc, asset, change), i) => Balance(acc, asset, change, i)
      })
    })
  }

  test("MongoBalancesStore.getBalance should return correct sized lists with continuation") {
    val db = fongo.getDB(dbName)
    val store = new MongoBalancesStore(new MongoCollection(db.getCollection("assets")), new MongoCollection(db.getCollection("assets_observations")))

    val updates = for {i <- 0 to 20} yield ("account" + (i % 10), "asset" + i, 1L)

    val updatesF = Future.sequence(updates.zipWithIndex.map { case ((acc, asset, change), i) => store.updateBalance(acc, asset, change, i) })

    updatesF.flatMap(statuses => {
      statuses.forall(identity) shouldBe true
      store.getBalances(10).flatMap(l => store.getBalances(10, Some(l.last.addressAndAsset)).map(
        _ shouldBe updates.zipWithIndex.sortBy(i => i._1 + "-" + i._2).slice(10, 20).map {
          case ((acc, asset, change), i) => Balance(acc, asset, change, i)
        }))
    })
  }

  override protected def afterEach(): Unit = {
    fongo.dropDatabase(dbName)
  }
}