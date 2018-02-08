package ru.tolsi.lykke.waves.blockchainapi.repository.mongo

import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.commons.MongoDBObject
import ru.tolsi.lykke.waves.blockchainapi.repository.{Asset, AssetsStore}
import salat.dao.SalatDAO
import salat.global._

import scala.concurrent.Future

class MongoAssetsStore(dbName: String) extends AssetsStore {

  private object MongoAssetsDAO extends SalatDAO[Asset, String](collection = MongoConnection()(dbName)("assets"))

  override def registerAsset(asset: Asset): Future[Unit] = Future.successful(MongoAssetsDAO.insert(asset))

  override def getAssets(take: Int, continuationId: Option[String]): Future[Seq[Asset]] = Future.successful {
    val cur = MongoAssetsDAO.find(ref = MongoDBObject("assetId" -> MongoDBObject("$gt" -> continuationId)))
      .sort(orderBy = MongoDBObject("assetId" -> -1)) // sort by _id desc
      .limit(take)
    try {
      cur.toList
    } finally {
      cur.close()
    }
  }

  override def getAsset(assetId: String): Future[Option[Asset]] = Future.successful(MongoAssetsDAO.findOneById(assetId))
}
