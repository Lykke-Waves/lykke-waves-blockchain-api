package ru.tolsi.lykke.waves.blockchainapi.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json._
import ru.tolsi.lykke.waves.blockchainapi.repository.{AddressTransactionsStore, FromAddressTransactionsStore, ToAddressTransactionsStore}

import scala.concurrent.ExecutionContext.Implicits.global

//  [POST] /api/transactions/history/from/{address}/observation
//  [POST] /api/transactions/history/to/{address}/observation
//  [DELETE] /api/transactions/history/from/{address}/observation
//  [DELETE] /api/transactions/history/to/{address}/observation
//  [GET] /api/transactions/history/from/{address}?take=integer&[afterHash=string]
//  [GET] /api/transactions/history/to/{address}?take=integer&[afterHash=string]
object TransactionsHistoryRoute {
  private implicit val BalaceWrites: Writes[AddressTransactionsStore.Transaction] = Json.writes[AddressTransactionsStore.Transaction]
}

case class TransactionsHistoryRoute(fromStore: FromAddressTransactionsStore, toStore: ToAddressTransactionsStore) extends PlayJsonSupport {

  import TransactionsHistoryRoute._
  val route: Route = path("transactions" / "history") {
    path("from") {
      // todo move out duplicated routes code
      path(Segment / "observation") { address =>
        post {
          complete(fromStore.addObservation(address).map(JsBoolean))
        } ~ delete {
          complete(fromStore.removeObservation(address).map(JsBoolean))
        }
      } ~ path(Segment) { address =>
        get {
          parameters('take.as[Int], 'continuation.as[Option[String]]) { case (take, continuation) =>
            complete(fromStore.getAddressTransactions(address, take, continuation).map(Json.toJson(_)))
          }
        }
      }
    } ~ path("to") {
      path(Segment / "observation") { address =>
        post {
          complete(toStore.addObservation(address).map(JsBoolean))
        } ~ delete {
          complete(toStore.removeObservation(address).map(JsBoolean))
        }
      } ~ path(Segment) { address =>
        get {
          parameters('take.as[Int], 'continuation.as[Option[String]]) { case (take, continuation) =>
            complete(toStore.getAddressTransactions(address, take, continuation).map(Json.toJson(_)))
          }
        }
      }
    }
  }
}
