package ru.tolsi.lykke.waves.blockchainapi.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json.{Json, Reads}
import ru.tolsi.lykke.common.repository.{BroadcastOperation, BroadcastOperationsStore}

//  [POST] /api/transactions/broadcast
//  [DELETE] /api/transactions/broadcast/{operationId}
// X [POST] /api/transactions/single
// X [POST] /api/transactions/many-inputs
// X [POST] /api/transactions/many-outputs
// X [PUT] /api/transactions
// X [GET] /api/transactions/broadcast/single/{operationId}
// X [GET] /api/transactions/broadcast/many-inputs/{operationId}
// X [GET] /api/transactions/broadcast/many-outputs/{operationId}
object TransactionsRoute {
  private implicit val BroadcastOperationReads: Reads[BroadcastOperation] = Json.reads[BroadcastOperation]
}

case class TransactionsRoute(store: BroadcastOperationsStore) extends PlayJsonSupport {

  import TransactionsRoute._

  val route: Route = pathPrefix("transactions" / "broadcast") {
    pathEnd {
      post {
        entity(as[BroadcastOperation]) { broadcastOperation =>
          onSuccess(store.addBroadcastOperation(broadcastOperation)) { result =>
            complete {
              if (result) StatusCodes.OK else StatusCodes.Conflict
            }
          }
        }
      }
    } ~ path(Segment) { operationId =>
      delete {
        onSuccess(store.removeBroadcastOperation(operationId)) { result =>
          complete {
            if (result) StatusCodes.OK else StatusCodes.NoContent
          }
        }
      }
    }
  }
}
