package ru.tolsi.lykke.waves.blockchainapi.repository.mongo

import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import ru.tolsi.lykke.waves.blockchainapi.repository.{BroadcastOperation, BroadcastOperationsStore}
import salat.dao.SalatDAO
import salat.global._
import scala.concurrent.ExecutionContext.Implicits.global


import scala.concurrent.Future

class MongoBroadcastOperationsStore(collection: MongoCollection) extends BroadcastOperationsStore {

  private object MongoBroadcastOperationsDAO extends SalatDAO[BroadcastOperation, String](collection)

  override def addBroadcastOperation(operation: BroadcastOperation): Future[Boolean] = {
    isOperationExists(operation).map {
      case true => false
      case false => MongoBroadcastOperationsDAO.insert(operation)
        true
    }
  }

  private def isOperationExists(operation: BroadcastOperation): Future[Boolean] = Future.successful(
    MongoBroadcastOperationsDAO.findOne(MongoDBObject("$or" ->
      MongoDBList(MongoDBObject("operationId" -> MongoDBObject("$eq" -> operation.operationId)),
        MongoDBObject("signedTransaction" -> MongoDBObject("$eq" -> operation.signedTransaction))))).isDefined)

  override def removeBroadcastOperation(id: String): Future[Boolean] = Future.successful {
    MongoBroadcastOperationsDAO.removeById(id).getN > 0
  }
}
