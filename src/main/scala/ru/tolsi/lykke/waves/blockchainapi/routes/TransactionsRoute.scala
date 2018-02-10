package ru.tolsi.lykke.waves.blockchainapi.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json.JsBoolean
import ru.tolsi.lykke.waves.blockchainapi.repository.{BroadcastOperation, BroadcastOperationsStore}


//  [POST] /api/transactions/broadcast
//  [DELETE] /api/transactions/broadcast/{operationId}
// X [POST] /api/transactions/single
// X [POST] /api/transactions/many-inputs
// X [POST] /api/transactions/many-outputs
// X [PUT] /api/transactions
// X [GET] /api/transactions/broadcast/single/{operationId}
// X [GET] /api/transactions/broadcast/many-inputs/{operationId}
// X [GET] /api/transactions/broadcast/many-outputs/{operationId}
case class TransactionsRoute(store: BroadcastOperationsStore) extends PlayJsonSupport {
  val route: Route = path("transactions" / "broadcast") {
    pathEnd {
      post {
        entity(as[BroadcastOperation]) { broadcastOperation =>
          complete(store.addBroadcastOperation(broadcastOperation).map(JsBoolean))
        }
      }
    } ~ path(Segment) { operationId =>
      delete {
        complete(store.removeBroadcastOperation(operationId).map(JsBoolean))
      }
    }
  }
}
