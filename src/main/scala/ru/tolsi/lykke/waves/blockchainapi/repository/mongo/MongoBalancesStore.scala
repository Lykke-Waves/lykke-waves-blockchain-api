package ru.tolsi.lykke.waves.blockchainapi.repository.mongo

import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import ru.tolsi.lykke.waves.blockchainapi.repository.{Balance, BalancesStore, Observation}
import salat.dao.SalatDAO
import salat.global._

import scala.concurrent.Future

class MongoBalancesStore(collection: MongoCollection, observationsCollection: MongoCollection) extends BalancesStore {

  private object MongoBalancesDAO extends SalatDAO[Balance, String](collection)

  private object MongoBalancesObservationsDAO extends SalatDAO[Observation, String](observationsCollection)

  override def addObservation(address: String): Future[Boolean] = Future.successful(
    // todo is it works?
    MongoBalancesObservationsDAO.findOneById(address).map(_ => false).getOrElse {
      MongoBalancesObservationsDAO.insert(Observation(address))
      true
    })

  override def removeObservation(address: String): Future[Boolean] = Future.successful {
    val result1 = MongoBalancesObservationsDAO.removeById(address)
    // todo remove saved balances here?
    val result2 = MongoBalancesDAO.remove(MongoDBObject("address" -> address))
    result1.wasAcknowledged() && result1.getN > 0 &&
      result2.wasAcknowledged()
  }

  override def getBalances(take: Int, continuationId: Option[String] = None): Future[Seq[Balance]] = Future.successful {
    val cur = (continuationId match {
      case Some(continuationId) =>
        // todo may be take by one address until "take" value?
        MongoBalancesDAO.find(ref = MongoDBObject("_id" -> MongoDBObject("$gt" -> continuationId)))
      case None =>
        MongoBalancesDAO.find(MongoDBObject.empty)
    }).sort(orderBy = MongoDBObject("_id" -> 1)) // sort by _id desc
      .limit(take)
    try {
      cur.toList
    } finally {
      cur.close()
    }
  }

  override def updateBalance(address: String, assetId: String, change: Long, block: Long): Future[Boolean] = Future.successful {
    val result = MongoBalancesDAO.update(MongoDBObject("$and" -> MongoDBList(MongoDBObject("_id" -> (address + "-" + assetId)), MongoDBObject("address" -> address), MongoDBObject("assetId" -> assetId))),
      MongoDBObject("$inc" -> MongoDBObject("balance" -> change), "$max" -> MongoDBObject("block" -> block)), upsert = true)
    result.getN > 0
  }
}
