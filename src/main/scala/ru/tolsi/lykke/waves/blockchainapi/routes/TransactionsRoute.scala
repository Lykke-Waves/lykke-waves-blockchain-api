package ru.tolsi.lykke.waves.blockchainapi.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, PathMatcher0, PathMatcher1, Route}
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

  private def notImpletementRoute0(routePath: PathMatcher0, method: => Directive0) = path(routePath) { method { complete(StatusCodes.NotImplemented) }}
  private def notImpletementRoute1(routePath: PathMatcher1[String], method: => Directive0) = path(routePath) { _ => method { complete(StatusCodes.NotImplemented) }}

  val route: Route = pathPrefix("transactions") {
    pathPrefix("broadcast") {
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
      } ~ notImpletementRoute1("single" / Segment, get) ~
        notImpletementRoute1("many-inputs" / Segment, get) ~
        notImpletementRoute1("many-outputs" / Segment, get)
    } ~ notImpletementRoute0("single", post) ~ notImpletementRoute0("many-inputs", post)~ notImpletementRoute0("many-outputs", post)
  } ~ notImpletementRoute0("transactions", put)
}
