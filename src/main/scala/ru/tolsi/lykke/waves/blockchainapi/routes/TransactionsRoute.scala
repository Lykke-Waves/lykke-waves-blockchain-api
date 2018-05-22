package ru.tolsi.lykke.waves.blockchainapi.routes

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, PathMatcher0, PathMatcher1, Route}
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json.{JsObject, Json, Reads, Writes}
import ru.tolsi.lykke.common.UnsignedTransferTransaction
import ru.tolsi.lykke.common.api.WavesApi
import ru.tolsi.lykke.common.http.ErrorMessage
import ru.tolsi.lykke.common.repository.{BroadcastOperation, BroadcastOperationsStore}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.util.control.NonFatal

//  [POST] /api/transactions/broadcast
//  [DELETE] /api/transactions/broadcast/{operationId}
//  [POST] /api/transactions/single
// X [POST] /api/transactions/many-inputs
// X [POST] /api/transactions/many-outputs
// X [PUT] /api/transactions
// X [GET] /api/transactions/broadcast/single/{operationId}
// X [GET] /api/transactions/broadcast/many-inputs/{operationId}
// X [GET] /api/transactions/broadcast/many-outputs/{operationId}
object TransactionsRoute {

  case class BroadcastOperationRequest(operationId: String, signedTransaction: String)

  case class OperationResult(errorCode: String)

  case class BuildTransactionRequest(operationId: String, fromAddress: String, fromAddressContext: String, toAddress: String, assetId: String, amount: String, includeFee: Boolean)

  case class TransactionContext(transactionContext: String)

  private implicit val BroadcastOperationRequestReads: Reads[BroadcastOperationRequest] = Json.reads[BroadcastOperationRequest]
  private implicit val BroadcastOperationResultWrites: Writes[OperationResult] = Json.writes[OperationResult]
  private implicit val BuildTransactionRequestReads: Reads[BuildTransactionRequest] = Json.reads[BuildTransactionRequest]
  private implicit val TransactionContextWrites: Writes[TransactionContext] = Json.writes[TransactionContext]
}

case class TransactionsRoute(store: BroadcastOperationsStore, api: WavesApi) extends PlayJsonSupport with StrictLogging {

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
    path("broadcast") {
      post {
        entity(as[BroadcastOperationRequest]) { broadcastOperationRequest =>
          val idOpt = Json.parse(broadcastOperationRequest.signedTransaction).as[JsObject].fields.find(_._1 == "id").map(_._2.as[String])
          idOpt match {
            case Some(id) =>
              onComplete(store.addBroadcastOperation(BroadcastOperation(broadcastOperationRequest.operationId, id, broadcastOperationRequest.signedTransaction))) {
                case Success(result) =>
                  complete {
                    if (result) {
                      api.sendSignedTransaction(broadcastOperationRequest.signedTransaction)
                        .map(_ => StatusCodes.OK -> OperationResult(""))
                        .recover {
                          // todo check it
                          case NonFatal(e) if e.getMessage.contains("199") =>
                            logger.debug("Error on broadcast tx, not enough balance", e)
                            StatusCodes.InternalServerError -> OperationResult("notEnoughBalance")
                          // there can't be amountIsTooSmall error
                          case NonFatal(e) =>
                            logger.error("Error on broadcast tx", e)
                            StatusCodes.InternalServerError -> OperationResult(e.getMessage)
                        }
                    } else StatusCodes.Conflict
                  }
                case Failure(NonFatal(f)) =>
                  logger.error("Add broadcast operation to database error", f)
                  complete(StatusCodes.InternalServerError -> Json.toJson(ErrorMessage("Add broadcast operation to database error")))
              }
            case None =>
              complete(StatusCodes.BadRequest -> Json.toJson(ErrorMessage("Invalid signed transaction", Some(Map("signedTransaction" -> Seq("There're no transaction id"))))))
          }
        }
      } ~ pathPrefix("single") {
        path(Segment) { operationId =>
          delete {
            onComplete(store.removeBroadcastOperation(operationId)) {
              case Success(result) =>
                complete {
                  if (result) StatusCodes.OK else StatusCodes.NoContent
                }
              case Failure(NonFatal(f)) =>
                logger.error("Remove broadcast operation from database error", f)
                complete(StatusCodes.InternalServerError -> Json.toJson(ErrorMessage("Remove broadcast operation from database error")))
            }
          }
        } ~
          notImpletementRoute1("many-inputs" / Segment, get) ~
          notImpletementRoute1("many-outputs" / Segment, get)
      }
    } ~ path("single") {
      post {
        entity(as[BuildTransactionRequest]) { transactionBuildRequest =>
          val amountAfterFee = if (transactionBuildRequest.includeFee && transactionBuildRequest.assetId == "WAVES") {
            transactionBuildRequest.amount.toLong - 100000
          } else {
            transactionBuildRequest.amount.toLong
          }
          onComplete(api.balance(transactionBuildRequest.fromAddress)) {
            case Success(balance) =>
              complete {
                if (amountAfterFee <= 0) {
                  StatusCodes.InternalServerError -> OperationResult("amountIsTooSmall")
                } else if (balance < transactionBuildRequest.amount.toLong) {
                  StatusCodes.InternalServerError -> OperationResult("notEnoughBalance")
                } else {
                  TransactionContext(UnsignedTransferTransaction(
                    transactionBuildRequest.fromAddress,
                    transactionBuildRequest.toAddress,
                    amountAfterFee,
                    100000,
                    if (transactionBuildRequest.assetId != "WAVES") {
                      Some(transactionBuildRequest.assetId)
                    } else {
                      None
                    }
                  ).toJsonString)
                }
              }
            case Failure(NonFatal(f)) =>
              logger.error("Read balance from database error", f)
              complete(StatusCodes.InternalServerError -> Json.toJson(ErrorMessage("Read balance from database error")))
          }
        }
      }
    } ~ notImpletementRoute0("many-inputs", post) ~ notImpletementRoute0("many-outputs", post)
  } ~ notImpletementRoute0("transactions", put)
}
