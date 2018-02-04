package ru.tolsi.lykke.waves.blockchainapi.repository

import scala.concurrent.Future

case class BroadcastOperation(operationId: String, signedTransaction: String)
trait BroadcastOperationsStore {
  def addBroadcastOperation(operation: BroadcastOperation): Future[Boolean]
  def isOperationExists(operation: BroadcastOperation): Future[Boolean]
}
