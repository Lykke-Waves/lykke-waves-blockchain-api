package ru.tolsi.lykke.waces.blockchainapi.repository.mongo

import com.github.fakemongo.Fongo
import com.mongodb.casbah.MongoCollection
import org.scalatest.{AsyncFunSuite, BeforeAndAfterEach, Matchers}
import ru.tolsi.lykke.waves.blockchainapi.repository.Transaction
import ru.tolsi.lykke.waves.blockchainapi.repository.mongo.MongoToAddressTransactionsStore

import scala.concurrent.Future

class MongoAddressTransactionsStoreSuite extends AsyncFunSuite with Matchers with BeforeAndAfterEach {
  val fongo = new Fongo("test mongo server")

  val dbName = "test"

  test("MongoToAddressTransactionsStore.addObservation should return true if not exists") {
    val db = fongo.getDB(dbName)
    val store = new MongoToAddressTransactionsStore(new MongoCollection(db.getCollection("transactions")),
      new MongoCollection(db.getCollection("transactions_observations")))
    store.addObservation("i1").map(_ shouldBe true)
  }

  test("MongoToAddressTransactionsStore.addObservation should return false if exists") {
    val db = fongo.getDB(dbName)
    val store = new MongoToAddressTransactionsStore(new MongoCollection(db.getCollection("transactions")),
      new MongoCollection(db.getCollection("transactions_observations")))
    store.addObservation("i1").flatMap(_ =>
      store.addObservation("i1").map(_ shouldBe false))
  }

  test("MongoToAddressTransactionsStore.addObservation should return false if exists by signed tx") {
    val db = fongo.getDB(dbName)
    val store = new MongoToAddressTransactionsStore(new MongoCollection(db.getCollection("transactions")),
      new MongoCollection(db.getCollection("transactions_observations")))
    store.addObservation("i1").flatMap(_ =>
      store.addObservation("i1").map(_ shouldBe false))
  }

  test("MongoToAddressTransactionsStore.removeObservation should return true if exists") {
    val db = fongo.getDB(dbName)
    val store = new MongoToAddressTransactionsStore(new MongoCollection(db.getCollection("transactions")),
      new MongoCollection(db.getCollection("transactions_observations")))

    store.addObservation("i1").flatMap(_ =>
      store.removeObservation("i1").map(_ shouldBe true))
  }

  test("MongoToAddressTransactionsStore.removeObservation should return false if not exists") {
    val db = fongo.getDB(dbName)
    val store = new MongoToAddressTransactionsStore(new MongoCollection(db.getCollection("transactions")),
      new MongoCollection(db.getCollection("transactions_observations")))

    store.removeObservation("i1").map(_ shouldBe false)
  }

  test("MongoToAddressTransactionsStore.removeObservation should remove address transactions") {
    val db = fongo.getDB(dbName)
    val store = new MongoToAddressTransactionsStore(new MongoCollection(db.getCollection("transactions")),
      new MongoCollection(db.getCollection("transactions_observations")))

    val transactions = for {i <- 0 to 20} yield Transaction(None, i, "account", "account", None, 1L, "abc" + i, store.field)

    val updatesF = Future.sequence(transactions.map(tx => store.addTransaction(tx)))

    store.addObservation("account").flatMap(_ =>
      updatesF.flatMap(statuses => {
        statuses.forall(identity) shouldBe true
        store.removeObservation("account").flatMap(r => {
          r shouldBe true
          store.getAddressTransactions("account", 10).map(_ shouldBe 'empty)
        })
      })
    )
  }


  test("MongoToAddressTransactionsStore.getAddressTransactions should return correct sized lists") {
    val db = fongo.getDB(dbName)
    val store = new MongoToAddressTransactionsStore(new MongoCollection(db.getCollection("transactions")),
      new MongoCollection(db.getCollection("transactions_observations")))

    val transactions = for {i <- 0 to 20} yield Transaction(None, i, "account", "account", None, 1L, "abc" + i, store.field)

    val updatesF = Future.sequence(transactions.map(tx => store.addTransaction(tx)))

    updatesF.flatMap(statuses => {
      statuses.forall(identity) shouldBe true
      store.getAddressTransactions("account", 10).map(_ shouldBe transactions.sortBy(t => t.toAddress + "-" + t.timestamp).take(10))
    })
  }

  test("MongoToAddressTransactionsStore.getAddressTransactions should return correct sized lists with continuation") {
    val db = fongo.getDB(dbName)
    val store = new MongoToAddressTransactionsStore(new MongoCollection(db.getCollection("transactions")),
      new MongoCollection(db.getCollection("transactions_observations")))

    val transactions = for {i <- 0 to 20} yield Transaction(None, i, "account", "account", None, 1L, "abc" + i, store.field)

    val updatesF = Future.sequence(transactions.map(tx => store.addTransaction(tx)))

    updatesF.flatMap(statuses => {
      statuses.forall(identity) shouldBe true
      store.getAddressTransactions("account", 10).flatMap(txs =>
        store.getAddressTransactions("account", 10, Some(txs.last.addressAndTimestamp))
          .map(s =>
            s shouldBe transactions.sortBy(t => t.toAddress + "-" + t.timestamp).slice(10, 20)))
    })
  }

  override protected def afterEach(): Unit = {
    fongo.dropDatabase(dbName)
  }

}