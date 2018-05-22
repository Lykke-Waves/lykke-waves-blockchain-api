package ru.tolsi.lykke.waves.blockchainapi.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json._
import ru.tolsi.lykke.common.http.ErrorMessage
import ru.tolsi.lykke.common.repository.{FromAddressTransactionsStore, ToAddressTransactionsStore, Transaction}

import scala.util.{Failure, Success}
import scala.util.control.NonFatal

//  [POST] /api/transactions/history/from/{address}/observation
//  [POST] /api/transactions/history/to/{address}/observation
//  [DELETE] /api/transactions/history/from/{address}/observation
//  [DELETE] /api/transactions/history/to/{address}/observation
//  [GET] /api/transactions/history/from/{address}?take=integer&[afterHash=string]
//  [GET] /api/transactions/history/to/{address}?take=integer&[afterHash=string]
object TransactionsHistoryRoute {
  private implicit val BalaceWrites: Writes[Transaction] = Json.writes[Transaction]
}

case class TransactionsHistoryRoute(fromStore: FromAddressTransactionsStore, toStore: ToAddressTransactionsStore) extends PlayJsonSupport with StrictLogging {

  import TransactionsHistoryRoute._

  val route: Route = pathPrefix("transactions" / "history") {
    pathPrefix("from") {
      path(Segment / "observation") { address =>
        post {
          onComplete(fromStore.addObservation(address)) {
            case Success(result) =>
              complete {
                if (result) StatusCodes.OK else StatusCodes.Conflict
              }
            case Failure(NonFatal(f)) =>
              logger.error("Add observation to database error", f)
              complete(StatusCodes.InternalServerError -> Json.toJson(ErrorMessage("Add observation to database error")))
          }
        } ~ delete {
          onComplete(fromStore.removeObservation(address)) {
            case Success(result) =>
              complete {
                if (result) StatusCodes.OK else StatusCodes.NoContent
              }
            case Failure(NonFatal(f)) =>
              logger.error("Delete observation from database error", f)
              complete(StatusCodes.InternalServerError -> Json.toJson(ErrorMessage("Add observation from database error")))
          }
        }
      } ~ path(Segment) { address =>
        get {
          parameters('take.as[Int], 'continuation.as[String] ?) { case (take, continuation) =>
            onComplete(fromStore.getAddressTransactions(address, take + 1, continuation)) {
              case Success(transactionsAndOneMore) =>
                complete {
                  val continuationAndTransactions = if (transactionsAndOneMore.lengthCompare(take + 1) == 0) {
                    // we ask the one more subsequent element only to determine if it exists
                    val toReturn = transactionsAndOneMore.init
                    (toReturn.last.addressAndTimestampAndHash, toReturn)
                  } else {
                    // there are no subsequent elements
                    ("", transactionsAndOneMore)
                  }
                  Json.toJson(TakeResponseObject(continuationAndTransactions._1, Json.toJson(continuationAndTransactions._2).as[JsArray]))
                }
              case Failure(NonFatal(f)) =>
                logger.error("Get observation data from database error", f)
                complete(StatusCodes.InternalServerError -> Json.toJson(ErrorMessage("Get observation data from database error")))
            }
          }
        }
      }
    } ~ pathPrefix("to") {
      path(Segment / "observation") { address =>
        post {
          onComplete(toStore.addObservation(address)) {
            case Success(result) =>
              complete {
                if (result) StatusCodes.OK else StatusCodes.Conflict
              }
            case Failure(NonFatal(f)) =>
              logger.error("Add observation to database error", f)
              complete(StatusCodes.InternalServerError -> Json.toJson(ErrorMessage("Add observation to database error")))
          }
        } ~ delete {
          onComplete(toStore.removeObservation(address)) {
            case Success(result) =>
              complete {
                if (result) StatusCodes.OK else StatusCodes.NoContent
              }
            case Failure(NonFatal(f)) =>
              logger.error("Delete observation from database error", f)
              complete(StatusCodes.InternalServerError -> Json.toJson(ErrorMessage("Add observation from database error")))
          }
        }
      } ~ path(Segment) { address =>
        get {
          parameters('take.as[Int], 'continuation.as[String] ?) { case (take, continuation) =>
            onComplete(toStore.getAddressTransactions(address, take + 1, continuation)) {
              case Success(transactionsAndOneMore) =>
                complete {
                  val continuationAndTransactions = if (transactionsAndOneMore.lengthCompare(take + 1) == 0) {
                    // we ask the one more subsequent element only to determine if it exists
                    val toReturn = transactionsAndOneMore.init
                    (toReturn.last.addressAndTimestampAndHash, toReturn)
                  } else {
                    // there are no subsequent elements
                    ("", transactionsAndOneMore)
                  }
                  Json.toJson(TakeResponseObject(continuationAndTransactions._1, Json.toJson(continuationAndTransactions._2).as[JsArray]))
                }
              case Failure(NonFatal(f)) =>
                logger.error("Get observation data from database error", f)
                complete(StatusCodes.InternalServerError -> Json.toJson(ErrorMessage("Get observation data from database error")))
            }
          }
        }
      }
    }
  }
}
