package ru.tolsi.lykke.waves.blockchainapi.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json._
import ru.tolsi.lykke.common.repository.{FromAddressTransactionsStore, ToAddressTransactionsStore, Transaction}

import scala.concurrent.ExecutionContext.Implicits.global

//  [POST] /api/transactions/history/from/{address}/observation
//  [POST] /api/transactions/history/to/{address}/observation
//  [DELETE] /api/transactions/history/from/{address}/observation
//  [DELETE] /api/transactions/history/to/{address}/observation
//  [GET] /api/transactions/history/from/{address}?take=integer&[afterHash=string]
//  [GET] /api/transactions/history/to/{address}?take=integer&[afterHash=string]
object TransactionsHistoryRoute {
  private implicit val BalaceWrites: Writes[Transaction] = Json.writes[Transaction]
}

case class TransactionsHistoryRoute(fromStore: FromAddressTransactionsStore, toStore: ToAddressTransactionsStore) extends PlayJsonSupport {

  import TransactionsHistoryRoute._

  val route: Route = pathPrefix("transactions" / "history") {
    pathPrefix("from") {
      path(Segment / "observation") { address =>
        post {
          onSuccess(fromStore.addObservation(address)) { result =>
            complete {
              if (result) StatusCodes.OK else StatusCodes.Conflict
            }
          }
        } ~ delete {
          onSuccess(fromStore.removeObservation(address)) { result =>
            complete {
              if (result) StatusCodes.OK else StatusCodes.NoContent
            }
          }
        }
      } ~ path(Segment) { address =>
        get {
          parameters('take.as[Int], 'continuation.as[String] ?) { case (take, continuation) =>
            onSuccess(fromStore.getAddressTransactions(address, take + 1, continuation)) { transactionsAndOneMore =>
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
            }
          }
        }
      }
    } ~ pathPrefix("to") {
      path(Segment / "observation") { address =>
        post {
          onSuccess(toStore.addObservation(address)) { result =>
            complete {
              if (result) StatusCodes.OK else StatusCodes.Conflict
            }
          }
        } ~ delete {
          onSuccess(toStore.removeObservation(address)) { result =>
            complete {
              if (result) StatusCodes.OK else StatusCodes.NoContent
            }
          }
        }
      } ~ path(Segment) { address =>
        get {
          parameters('take.as[Int], 'continuation.as[String] ?) { case (take, continuation) =>
            onSuccess(toStore.getAddressTransactions(address, take + 1, continuation)) { transactionsAndOneMore =>
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
            }
          }
        }
      }
    }
  }
}
