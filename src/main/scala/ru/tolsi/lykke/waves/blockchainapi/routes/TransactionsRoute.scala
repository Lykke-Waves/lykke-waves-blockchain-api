package ru.tolsi.lykke.waves.blockchainapi.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, PathMatcher0, PathMatcher1, Route}
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json.{JsObject, Json, Reads, Writes}
import ru.tolsi.lykke.common.api.WavesApi
import ru.tolsi.lykke.common.http.ErrorMessage
import ru.tolsi.lykke.common.repository.{BroadcastOperation, BroadcastOperationsStore}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.control.NonFatal

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

  case class BroadcastOperationRequest(operationId: String, signedTransaction: String)

  case class BroadcastOperationResult(errorCode: String)

  private implicit val BroadcastOperationRequestReads: Reads[BroadcastOperationRequest] = Json.reads[BroadcastOperationRequest]
  private implicit val BroadcastOperationResultWrites: Writes[BroadcastOperationResult] = Json.writes[BroadcastOperationResult]
}

case class TransactionsRoute(store: BroadcastOperationsStore, api: WavesApi) extends PlayJsonSupport {

  import TransactionsRoute._

  private def notImpletementRoute0(routePath: PathMatcher0, method: => Directive0) = path(routePath) {
    method {
      complete(StatusCodes.NotImplemented)
    }
  }

  private def notImpletementRoute1(routePath: PathMatcher1[String], method: => Directive0) = path(routePath) { _ =>
    method {
      complete(StatusCodes.NotImplemented)
    }
  }

  val route: Route = pathPrefix("transactions") {
    pathPrefix("broadcast") {
      pathEnd {
        post {
          entity(as[BroadcastOperationRequest]) { broadcastOperationRequest =>
            val idOpt = Json.parse(broadcastOperationRequest.signedTransaction).as[JsObject].fields.find(_._1 == "id").map(_._2.as[String])
            idOpt match {
              case Some(id) =>
                onSuccess(store.addBroadcastOperation(BroadcastOperation(broadcastOperationRequest.operationId, id, broadcastOperationRequest.signedTransaction))) { result =>
                  complete {
                    if (result) {
                      api.sendSignedTransaction(broadcastOperationRequest.signedTransaction)
                        .map(_ => StatusCodes.OK -> BroadcastOperationResult(""))
                        .recover {
                          // todo check it
                          case NonFatal(e) if e.getMessage.contains("199") => StatusCodes.OK -> BroadcastOperationResult("notEnoughBalance")
                          // there can't be amountIsTooSmall error
                          case NonFatal(e) => StatusCodes.InternalServerError -> BroadcastOperationResult(e.getMessage)
                        }
                    } else StatusCodes.Conflict
                  }
                }
              case None =>
                complete(StatusCodes.BadRequest -> Json.toJson(ErrorMessage("Invalid signed transaction", Some(Map("signedTransaction" -> Seq("There're no transaction id"))))))
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
    } ~ notImpletementRoute0("single", post) ~ notImpletementRoute0("many-inputs", post) ~ notImpletementRoute0("many-outputs", post)
  } ~ notImpletementRoute0("transactions", put)
}
