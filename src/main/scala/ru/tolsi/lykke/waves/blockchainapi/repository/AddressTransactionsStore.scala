package ru.tolsi.lykke.waves.blockchainapi.repository

import ru.tolsi.lykke.waves.blockchainapi.repository.AddressTransactionsStore.Transaction
import salat.annotations.Key

import scala.concurrent.Future

object AddressTransactionsStore {

  case class Transaction(operationId: Option[String],
                         timestamp: Long,
                         fromAddress: String,
                         toAddress: String,
                         assetId: Option[String],
                         amount: Long,
                         @Key("hash") hash: String)

}

trait AddressTransactionsStore {
  def addObservation(address: String): Future[Boolean]

  def removeObservation(address: String): Future[Boolean]

  def getAddressTransactions(address: String, take: Int, continuationId: Option[String]): Future[Seq[Transaction]]

  def addTransaction(transaction: Transaction): Future[Boolean]
}
