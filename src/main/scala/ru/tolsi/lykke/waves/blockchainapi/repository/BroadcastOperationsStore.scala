package ru.tolsi.lykke.waves.blockchainapi.repository

import salat.annotations.Key

import scala.concurrent.Future

case class BroadcastOperation(@Key("_id") operationId: String, signedTransaction: String)

trait BroadcastOperationsStore {
  def addBroadcastOperation(operation: BroadcastOperation): Future[Boolean]

  def removeBroadcastOperation(id: String): Future[Boolean]
}
