package ru.tolsi.lykke.waves.blockchainapi.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json._
import ru.tolsi.lykke.common.repository.{Asset, AssetsStore}

//  [GET] /api/assets?take=integer&[continuation=string]
//  [GET] /api/assets/{assetId}
object AssetsRoute {
  private implicit val AssetWrites: Writes[Asset] = Json.writes[Asset]
}

case class AssetsRoute(store: AssetsStore) extends PlayJsonSupport {

  import AssetsRoute._

  val route: Route = pathPrefix("assets") {
    pathEnd {
      get {
        parameters('take.as[Int], 'continuation.as[String] ?) { case (take, continuation) =>
          onSuccess(store.getAssets(take + 1, continuation)) { assetsAndOneMore =>
            complete {
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
          }
        }
      }
    } ~ path(Segment) { assetId =>
      get {
        onSuccess(store.getAsset(assetId)) { assetOpt =>
          complete {
            assetOpt match {
              case Some(asset) => Json.toJson(asset)
              case None => StatusCodes.NoContent
            }
          }
        }
      }
    }
  }
}
