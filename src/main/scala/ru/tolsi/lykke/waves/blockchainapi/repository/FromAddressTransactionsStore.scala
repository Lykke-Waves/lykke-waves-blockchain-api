package ru.tolsi.lykke.waves.blockchainapi.repository

import scala.concurrent.Future

trait FromAddressTransactionsStore {
  def addObservation(address: String): Future[Boolean]
  def removeObservation(address: String): Future[Boolean]
  def getAddressTransactions(address: String, take: Int, continuationId: Option[String]): Future[Boolean]
}
