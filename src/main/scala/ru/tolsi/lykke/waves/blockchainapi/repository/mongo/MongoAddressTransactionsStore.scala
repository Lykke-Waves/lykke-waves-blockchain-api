package ru.tolsi.lykke.waves.blockchainapi.repository.mongo

import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.commons.MongoDBObject
import ru.tolsi.lykke.waves.blockchainapi.repository.AddressTransactionsStore.Transaction
import ru.tolsi.lykke.waves.blockchainapi.repository.{AddressTransactionsStore, Observation}
import salat.dao.SalatDAO
import salat.global._

import scala.concurrent.Future

abstract class MongoAddressTransactionsStore(collection: MongoCollection, observationsCollection: MongoCollection) extends AddressTransactionsStore {
  self: AddressTransactionsStore =>

  private object MongoAddressTransactionsDAO extends SalatDAO[Transaction, String](collection)

  private object MongoAddressTransactionsObservationsDAO extends SalatDAO[Observation, String](observationsCollection)

  override def addObservation(address: String): Future[Boolean] = Future.successful(
    // todo is it works?
    MongoAddressTransactionsObservationsDAO.findOneById(address).map(_ => false).getOrElse {
      MongoAddressTransactionsObservationsDAO.insert(Observation(address))
      true
    })

  override def removeObservation(address: String): Future[Boolean] = Future.successful {
    MongoAddressTransactionsObservationsDAO.removeById(address).getN > 0
  }

  override def getAddressTransactions(address: String, take: Int, continuationId: Option[String]): Future[Seq[Transaction]] = Future.successful {
    val cur = continuationId match {
      case Some(continuationId) =>
        val Array(address, timestamp) = continuationId.split(":")
        // todo may be take by one address until "take" value?
        MongoAddressTransactionsDAO.find(ref = MongoDBObject("address" -> MongoDBObject("$eq" -> address), "timestamp" -> MongoDBObject("$gt" -> timestamp)))
          .sort(orderBy = MongoDBObject("timestamp" -> -1)) // sort by _id desc
          .limit(take)
      case None =>
        MongoAddressTransactionsDAO.find(MongoDBObject("$eq" -> address))
          .sort(orderBy = MongoDBObject("timestamp" -> -1)) // sort by _id desc
          .limit(take)
    }
    try {
      cur.toList
    } finally {
      cur.close()
    }
  }

  override def addTransaction(transaction: Transaction): Future[Boolean] = Future.successful(
    // todo is it works?
    MongoAddressTransactionsDAO.findOneById(transaction.hash).map(_ => false).getOrElse {
      MongoAddressTransactionsDAO.insert(transaction)
      true
    })
}
