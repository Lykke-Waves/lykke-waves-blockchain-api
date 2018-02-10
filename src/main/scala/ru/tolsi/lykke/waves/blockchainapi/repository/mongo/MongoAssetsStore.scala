package ru.tolsi.lykke.waves.blockchainapi.repository.mongo

import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.commons.MongoDBObject
import ru.tolsi.lykke.waves.blockchainapi.repository.{Asset, AssetsStore}
import salat.dao.SalatDAO
import salat.global._

import scala.concurrent.Future

class MongoAssetsStore(collection: MongoCollection) extends AssetsStore {

  private object MongoAssetsDAO extends SalatDAO[Asset, String](collection)

  override def registerAsset(asset: Asset): Future[Unit] = Future.successful(MongoAssetsDAO.insert(asset))

  override def getAssets(take: Int, continuationId: Option[String] = None): Future[Seq[Asset]] = Future.successful {
    val cur = MongoAssetsDAO.find(ref = MongoDBObject("_id" -> MongoDBObject("$gt" -> continuationId)))
      .sort(orderBy = MongoDBObject("_id" -> 1))
      .limit(take)
    try {
      cur.toList
    } finally {
      cur.close()
    }
  }

  override def getAsset(assetId: String): Future[Option[Asset]] = Future.successful(MongoAssetsDAO.findOneById(assetId))
}
