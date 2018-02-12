package ru.tolsi.lykke.waves.blockchainapi.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json._
import ru.tolsi.lykke.common.repository.{Balance, BalancesStore}

import scala.concurrent.ExecutionContext.Implicits.global

//  [POST] /api/balances/{address}/observation
//  [DELETE] /api/balances/{address}/observation
//  [GET] /api/balances?take=integer&[continuation=string]
object BalancesRoute {
  private implicit val BalaceWrites: Writes[Balance] = Json.writes[Balance]
}

case class BalancesRoute(store: BalancesStore) extends PlayJsonSupport {

  import BalancesRoute._

  val route: Route = pathPrefix("balances") {
    pathEnd {
      get {
        parameters('take.as[Int], 'continuation.as[String] ?) { case (take, continuation) =>
          complete(store.getBalances(take, continuation).map(Json.toJson(_)))
        }
      }
    } ~ path(Segment / "observation") { address =>
      post {
        onSuccess(store.addObservation(address)) { result =>
          complete{
            if (result) StatusCodes.OK else StatusCodes.Conflict
          }
        }
      } ~ delete {
        onSuccess(store.removeObservation(address)) { result =>
          complete {
            if (result) StatusCodes.OK else StatusCodes.NoContent
          }
        }
      }
    }
  }
}
