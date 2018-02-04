package ru.tolsi.lykke.waves.blockchainapi.repository

object AddressTransactionsStore {

  case class Transaction(operationId: Option[String],
                         timestamp: Long,
                         fromAddress: String,
                         toAddress: String,
                         assetId: Option[String],
                         amount: Long,
                         hash: String)

}
