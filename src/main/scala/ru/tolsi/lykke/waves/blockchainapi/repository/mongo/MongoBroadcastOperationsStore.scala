package ru.tolsi.lykke.waves.blockchainapi.repository.mongo

import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import ru.tolsi.lykke.waves.blockchainapi.repository.{BroadcastOperation, BroadcastOperationsStore}
import salat.dao.SalatDAO
import salat.global._

import scala.concurrent.Future

class MongoBroadcastOperationsStore(dbName: String) extends BroadcastOperationsStore {

  private object MongoBroadcastOperationsDAO extends SalatDAO[BroadcastOperation, String](collection = MongoConnection()(dbName)("broadcast_operations"))

  override def addBroadcastOperation(operation: BroadcastOperation): Future[Boolean] = Future.successful(
    // todo is it works?
    MongoBroadcastOperationsDAO.findOneById(operation.operationId).map(_ => false).getOrElse {
      MongoBroadcastOperationsDAO.insert(operation)
      true
    })

  // todo is it needed?
  override def isOperationExists(operation: BroadcastOperation): Future[Boolean] = Future.successful(
    // todo is it works?
    MongoBroadcastOperationsDAO.findOne(MongoDBObject("$or" ->
      MongoDBList(MongoDBObject("operationId" -> MongoDBObject("$eq" -> operation.operationId)),
        MongoDBObject("signedTransaction" -> MongoDBObject("$eq" -> operation.signedTransaction))))).isDefined)

  override def removeBroadcastOperation(id: String): Future[Boolean] = Future.successful {
    MongoBroadcastOperationsDAO.remove(id).getN > 1
  }
}
