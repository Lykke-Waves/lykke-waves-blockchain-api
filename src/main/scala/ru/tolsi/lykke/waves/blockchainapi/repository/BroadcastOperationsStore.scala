package ru.tolsi.lykke.waves.blockchainapi.repository

import salat.annotations.Key

import scala.concurrent.Future

case class BroadcastOperation(@Key("operationId") operationId: String, signedTransaction: String)

trait BroadcastOperationsStore {
  def addBroadcastOperation(operation: BroadcastOperation): Future[Boolean]

  def isOperationExists(operation: BroadcastOperation): Future[Boolean]
}
