package ru.tolsi.lykke.waves.blockchainapi.repository.mongo

import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.commons.MongoDBObject
import ru.tolsi.lykke.waves.blockchainapi.repository.{AddressTransactionsStore, Observation, Transaction}
import salat.dao.SalatDAO
import salat.global._

import scala.concurrent.Future

abstract class MongoAddressTransactionsStore(collection: MongoCollection, observationsCollection: MongoCollection, val field: String) extends AddressTransactionsStore {
  self: AddressTransactionsStore =>

  private object MongoAddressTransactionsDAO extends SalatDAO[Transaction, String](collection)

  private object MongoAddressTransactionsObservationsDAO extends SalatDAO[Observation, String](observationsCollection)

  override def addObservation(address: String): Future[Boolean] = Future.successful(
    MongoAddressTransactionsObservationsDAO.findOneById(address).map(_ => false).getOrElse {
      MongoAddressTransactionsObservationsDAO.insert(Observation(address))
      true
    })

  override def removeObservation(address: String): Future[Boolean] = Future.successful {
    val r1 = MongoAddressTransactionsObservationsDAO.removeById(address)
    val r2 = MongoAddressTransactionsDAO.remove(MongoDBObject(field -> address))
    r1.getN > 0 && r2.wasAcknowledged()
  }

  override def getAddressTransactions(address: String, take: Int, continuationId: Option[String] = None): Future[Seq[Transaction]] = Future.successful {
    val cur = (continuationId match {
      case Some(continuationId) =>
        MongoAddressTransactionsDAO.find(ref = MongoDBObject(field -> address, "_id" -> MongoDBObject("$gt" -> continuationId)))
      case None =>
        MongoAddressTransactionsDAO.find(MongoDBObject(field -> address))
    }).sort(orderBy = MongoDBObject("_id" -> 1))
      .limit(take)
    try {
      cur.toList
    } finally {
      cur.close()
    }
  }

  override def addTransaction(transaction: Transaction): Future[Boolean] = Future.successful(
    MongoAddressTransactionsDAO.findOne(MongoDBObject("hash" -> transaction.hash)).map(_ => false).getOrElse {
      MongoAddressTransactionsDAO.insert(transaction)
      true
    })
}
