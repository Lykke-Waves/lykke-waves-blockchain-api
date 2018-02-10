package ru.tolsi.lykke.waves.blockchainapi.repository

import salat.annotations.Key

import scala.concurrent.Future

object Transaction {
  def apply(operationId: Option[String],
            timestamp: Long,
            fromAddress: String,
            toAddress: String,
            assetId: Option[String],
            amount: Long,
            hash: String,
            field: String
           ) = new Transaction(
    (field match {
      case "toAddress" => toAddress
      case "fromAddress" => fromAddress
    }) + "-" + timestamp,
    operationId, timestamp, fromAddress, toAddress, assetId, amount, hash
  )
}

case class Transaction(@Key("_id") addressAndTimestamp: String,
                       operationId: Option[String],
                       timestamp: Long,
                       fromAddress: String,
                       toAddress: String,
                       assetId: Option[String],
                       amount: Long,
                       hash: String)

trait AddressTransactionsStore {
  def addObservation(address: String): Future[Boolean]

  def removeObservation(address: String): Future[Boolean]

  def getAddressTransactions(address: String, take: Int, continuationId: Option[String]): Future[Seq[Transaction]]

  def addTransaction(transaction: Transaction): Future[Boolean]
}
