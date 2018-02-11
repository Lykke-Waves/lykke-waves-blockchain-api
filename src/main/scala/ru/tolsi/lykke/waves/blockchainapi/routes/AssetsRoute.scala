package ru.tolsi.lykke.waves.blockchainapi.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json._
import ru.tolsi.lykke.common.repository.{Asset, AssetsStore}

import scala.concurrent.ExecutionContext.Implicits.global

//  [GET] /api/assets?take=integer&[continuation=string]
//  [GET] /api/assets/{assetId}
object AssetsRoute {
  private implicit val AssetWrites: Writes[Asset] = Json.writes[Asset]
}
case class AssetsRoute(store: AssetsStore) extends PlayJsonSupport {

  import AssetsRoute._
  val route: Route = path("assets") {
    pathEnd {
      get {
        parameters('take.as[Int], 'continuation.as[Option[String]]) { case (take, continuation) =>
          complete(store.getAssets(take, continuation).map(Json.toJson(_)))
        }
      }
    } ~ path(Segment) { assetId =>
        get {
          complete(store.getAsset(assetId).map(Json.toJson(_)))
        }
      }
  }
}
