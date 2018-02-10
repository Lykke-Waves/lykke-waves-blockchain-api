package ru.tolsi.lykke.waves.blockchainapi.repository

import salat.annotations.Key

import scala.concurrent.Future

object Balance {
  def apply(address: String, assetId: String, balance: Long, block: Long): Balance = new Balance(address + "-" + assetId,
    address, assetId, balance, block)
}

case class Balance(@Key("_id") addressAndAsset: String, address: String, assetId: String, balance: Long, block: Long)

trait BalancesStore {
  def addObservation(address: String): Future[Boolean]

  def removeObservation(address: String): Future[Boolean]

  def getBalances(take: Int, continuationId: Option[String]): Future[Seq[Balance]]

  def updateBalance(address: String, assetId: String, change: Long, block: Long): Future[Boolean]
}
