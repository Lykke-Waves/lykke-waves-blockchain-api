package ru.tolsi.lykke.waves.blockchainapi.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json._
import ru.tolsi.lykke.common.http.ErrorMessage
import ru.tolsi.lykke.common.repository.{Balance, BalancesStore}

import scala.util.control.NonFatal
import scala.util.{Failure, Success}

//  [POST] /api/balances/{address}/observation
//  [DELETE] /api/balances/{address}/observation
//  [GET] /api/balances?take=integer&[continuation=string]
object BalancesRoute {
  private implicit val BalaceWrites: Writes[Balance] = Json.writes[Balance]
}

case class BalancesRoute(store: BalancesStore) extends PlayJsonSupport with StrictLogging {

  import BalancesRoute._

  val route: Route = pathPrefix("balances") {
    pathEnd {
      get {
        parameters('take.as[Int], 'continuation.as[String] ?) { case (take, continuation) =>
          onComplete(store.getBalances(take + 1, continuation)) {
            case Success(balancesAndOneMore) =>
              complete {
                val continuationAndBalances = if (balancesAndOneMore.lengthCompare(take + 1) == 0) {
                  // we ask the one more subsequent element only to determine if it exists
                  val toReturn = balancesAndOneMore.init
                  (toReturn.last.addressAndAsset, toReturn)
                } else {
                  // there are no subsequent elements
                  ("", balancesAndOneMore)
                }
                Json.toJson(TakeResponseObject(continuationAndBalances._1, Json.toJson(continuationAndBalances._2).as[JsArray]))
              }
            case Failure(NonFatal(f)) =>
              logger.error("Read balances from database error", f)
              complete(StatusCodes.InternalServerError -> Json.toJson(ErrorMessage("Read balances from database error")))
          }
        }
      }
    } ~ path(Segment / "observation") { address =>
      post {
        onComplete(store.addObservation(address)) {
          case Success(result) =>
            complete {
              // todo move out common logic
              if (result) StatusCodes.OK else StatusCodes.Conflict
            }
          case Failure(NonFatal(f)) =>
            logger.error("Add observation to database error", f)
            complete(StatusCodes.InternalServerError -> Json.toJson(ErrorMessage("Add observation to database error")))
        }
      } ~ delete {
        onComplete(store.removeObservation(address)) {
          case Success(result) =>
            complete {
              if (result) StatusCodes.OK else StatusCodes.NoContent
            }
          case Failure(NonFatal(f)) =>
            logger.error("Remove observation from database error", f)
            complete(StatusCodes.InternalServerError -> Json.toJson(ErrorMessage("Remove observation from database error")))
        }
      }
    }
  }
}
