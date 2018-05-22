package ru.tolsi.lykke.waves.blockchainapi.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json._
import ru.tolsi.lykke.common.http.ErrorMessage
import ru.tolsi.lykke.common.repository.{Asset, AssetsStore}

import scala.util.control.NonFatal
import scala.util.{Failure, Success}

//  [GET] /api/assets?take=integer&[continuation=string]
//  [GET] /api/assets/{assetId}
object AssetsRoute {
  private implicit val AssetWrites: Writes[Asset] = Json.writes[Asset]
}

case class AssetsRoute(store: AssetsStore) extends PlayJsonSupport with StrictLogging {

  import AssetsRoute._

  val route: Route = pathPrefix("assets") {
    pathEnd {
      get {
        parameters('take.as[Int], 'continuation.as[String] ?) { case (take, continuation) =>
          onComplete(store.getAssets(take + 1, continuation)) {
            case Success(assetsAndOneMore) =>
              complete {
                // todo move out common take logic
                val continuationAndAssets = if (assetsAndOneMore.lengthCompare(take + 1) == 0) {
                  // we ask the one more subsequent element only to determine if it exists
                  val toReturn = assetsAndOneMore.init
                  (toReturn.last.assetId, toReturn)
                } else {
                  // there are no subsequent elements
                  ("", assetsAndOneMore)
                }
                Json.toJson(TakeResponseObject(continuationAndAssets._1, Json.toJson(continuationAndAssets._2).as[JsArray]))
              }
            case Failure(NonFatal(f)) =>
              logger.error("Read assets from database error", f)
              complete(StatusCodes.InternalServerError -> Json.toJson(ErrorMessage("Read assets from database error")))
          }
        }
      }
    } ~ path(Segment) { assetId =>
      get {
        onComplete(store.getAsset(assetId)) {
          case Success(assetOpt) =>
            complete {
              // todo move out common logic
              assetOpt match {
                case Some(asset) => Json.toJson(asset)
                case None => StatusCodes.NoContent
              }
            }
          case Failure(NonFatal(f)) =>
            logger.error("Read asset from database error", f)
            complete(StatusCodes.InternalServerError -> Json.toJson(ErrorMessage("Read asset from database error")))
        }
      }
    }
  }
}
