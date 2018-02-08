package ru.tolsi.lykke.waves.blockchainapi.repository

import salat.annotations.Key

import scala.concurrent.Future

case class Balance(@Key("address") address: String, assetId: String, balance: Long, block: Long)

trait BalancesStore {
  def addObservation(address: String): Future[Boolean]

  def removeObservation(address: String): Future[Boolean]

  def getBalances(take: Int, continuationId: Option[String]): Future[Seq[Balance]]

  def updateBalance(address: String, assetId: String, change: Long): Future[Balance]
}
