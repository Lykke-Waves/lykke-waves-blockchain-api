package ru.tolsi.lykke.waves.blockchainapi.repository

import scala.concurrent.Future

case class Balance(address: String, assetId: String, balance: Long, block: Long)

trait BalancesStore {
  def addObservation(address: String): Future[Boolean]

  def removeObservation(address: String): Future[Boolean]

  def getBalances(take: Int, continuationId: Option[String]): Future[Seq[Balance]]
}
