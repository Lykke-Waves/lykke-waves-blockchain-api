package ru.tolsi.lykke.waves.blockchainapi.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json.Json
import ru.tolsi.lykke.waves.blockchainapi.repository.{FromAddressTransactionsStore, ToAddressTransactionsStore}

//  [POST] /api/transactions/history/from/{address}/observation
//  [POST] /api/transactions/history/to/{address}/observation
//  [DELETE] /api/transactions/history/from/{address}/observation
//  [DELETE] /api/transactions/history/to/{address}/observation
//  [GET] /api/transactions/history/from/{address}?take=integer&[afterHash=string]
//  [GET] /api/transactions/history/to/{address}?take=integer&[afterHash=string]
case class TransactionsHistoryRoute(fromStore: FromAddressTransactionsStore, toStore: ToAddressTransactionsStore) extends PlayJsonSupport {
  val route: Route = path("transactions" / "history") {
    path("from") {
      // todo move out duplicated routes code
      pathEnd {
        get {
          parameters('take.as[Int], 'continuation.as[Option[String]]) { case (take, continuation) =>
            complete(fromStore.getAddressTransactions(take, continuation).map(Json.toJson))
          }
        }
      } ~ path(Segment / "observation") { address =>
        post {
          complete(fromStore.addObservation(address).map(Json.toJson))
        } ~ delete {
          complete(fromStore.removeObservation(address).map(Json.toJson))
        }
      }
    } ~ path("to") {
      pathEnd {
        get {
          parameters('take.as[Int], 'continuation.as[Option[String]]) { case (take, continuation) =>
            complete(toStore.getAddressTransactions(take, continuation).map(Json.toJson))
          }
        }
      } ~ path(Segment / "observation") { address =>
        post {
          complete(toStore.addObservation(address).map(Json.toJson))
        } ~ delete {
          complete(toStore.removeObservation(address).map(Json.toJson))
        }
      }
    }
  }
}
