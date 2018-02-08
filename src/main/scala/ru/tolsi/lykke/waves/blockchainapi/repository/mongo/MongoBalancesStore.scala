package ru.tolsi.lykke.waves.blockchainapi.repository.mongo

import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import ru.tolsi.lykke.waves.blockchainapi.repository.{Balance, BalancesStore}
import salat.dao.SalatDAO
import salat.global._

import scala.concurrent.Future

class MongoBalancesStore(dbName: String) extends BalancesStore {

  private object MongoBalancesDAO extends SalatDAO[Balance, String](collection = MongoConnection()(dbName)("balances"))

  private object MongoBalancesObservationsDAO extends SalatDAO[String, String](collection = MongoConnection()(dbName)("balances_observations"))

  override def addObservation(address: String): Future[Boolean] = Future.successful(
    // todo is it works?
    MongoBalancesObservationsDAO.findOneById(address).map(_ => false).getOrElse {
      MongoBalancesObservationsDAO.insert(address)
      true
    })

  override def removeObservation(address: String): Future[Boolean] = Future.successful {
    val result1 = MongoBalancesObservationsDAO.remove(address)
    // todo remove saved balances here?
    val result2 = MongoBalancesDAO.removeById(address)
    result1.wasAcknowledged() && result1.getN > 1 &&
      result2.wasAcknowledged()
  }

  override def getBalances(take: Int, continuationId: Option[String]): Future[Seq[Balance]] = Future.successful {
    val cur = continuationId match {
      case Some(continuationId) =>
        val Array(address, assetId) = continuationId.split(":")
        // todo may be take by one address until "take" value?
        MongoBalancesDAO.find(ref = MongoDBObject("address" -> MongoDBObject("$gte" -> address), "assetId" -> MongoDBObject("$gt" -> assetId)))
          .sort(orderBy = MongoDBObject("address" -> -1, "assetId" -> -1)) // sort by _id desc
          .limit(take)
      case None =>
        MongoBalancesDAO.find(MongoDBObject.empty)
          .sort(orderBy = MongoDBObject("address" -> -1, "assetId" -> -1)) // sort by _id desc
          .limit(take)
    }
    try {
      cur.toList
    } finally {
      cur.close()
    }
  }

  override def updateBalance(address: String, assetId: String, change: Long): Future[Balance] = Future.successful {
    // todo it works?
    val result = MongoBalancesDAO.update(MongoDBObject("$and" -> MongoDBList(MongoDBObject("address" -> MongoDBObject("$eq" -> address)), MongoDBObject("assetId" -> MongoDBObject("$eq" -> assetId)))),
      MongoDBObject("balance" -> MongoDBObject("$inc" -> change)), upsert = true)
    MongoBalancesDAO.findOneById(result.getUpsertedId.asInstanceOf[String]).get
  }
}
