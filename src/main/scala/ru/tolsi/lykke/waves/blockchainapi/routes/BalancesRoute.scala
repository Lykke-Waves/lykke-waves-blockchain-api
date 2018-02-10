package ru.tolsi.lykke.waves.blockchainapi.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json.{JsBoolean, Json}
import ru.tolsi.lykke.waves.blockchainapi.repository.BalancesStore

//  [POST] /api/balances/{address}/observation
//  [DELETE] /api/balances/{address}/observation
//  [GET] /api/balances?take=integer&[continuation=string]
case class BalancesRoute(store: BalancesStore) extends PlayJsonSupport {
  val route: Route = path("balances") {
    pathEnd {
      get {
        parameters('take.as[Int], 'continuation.as[Option[String]]) { case (take, continuation) =>
          complete(store.getBalances(take, continuation).map(Json.toJson))
        }
      }
    } ~ path(Segment / "observation") { address =>
      post {
        complete(store.addObservation(address).map(JsBoolean))
      } ~ delete {
        complete(store.removeObservation(address).map(JsBoolean))
      }
    }
  }
}
