package ru.tolsi.lykke.waves.blockchainapi.repository

import scala.concurrent.Future

case class Asset(assetId: String, name: String, address: String, amount: Long, accuracy: Short)

trait AssetsStore {
  def registerAsset(asset: Asset): Future[Unit]

  def getAssets(take: Int, continuationId: Option[String]): Future[Seq[Asset]]

  def getAsset(assetId: String): Future[Option[Asset]]
}
