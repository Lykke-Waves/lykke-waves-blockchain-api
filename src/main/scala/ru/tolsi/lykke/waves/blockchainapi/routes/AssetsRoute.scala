package ru.tolsi.lykke.waves.blockchainapi.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json.Json
import ru.tolsi.lykke.waves.blockchainapi.repository.AssetsStore

//  [GET] /api/assets?take=integer&[continuation=string]
//  [GET] /api/assets/{assetId}
case class AssetsRoute(store: AssetsStore) extends PlayJsonSupport {
  val route: Route = path("assets") {
    pathEnd {
      get {
        parameters('take.as[Int], 'continuation.as[Option[String]]) { case (take, continuation) =>
          complete(store.getAssets(take, continuation).map(Json.toJson))
        }
      }
    } ~ path(Segment) { assetId =>
        get {
          complete(store.getAsset(assetId).map(Json.toJson))
        }
      }
  }
}
