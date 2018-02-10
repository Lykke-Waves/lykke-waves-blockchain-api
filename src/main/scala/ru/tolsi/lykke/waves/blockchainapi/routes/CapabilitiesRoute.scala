package ru.tolsi.lykke.waves.blockchainapi.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json.{JsValue, Json, Writes}

object CapabilitiesRoute {

  private case class ResponseObject(isTransactionsRebuildingSupported: Boolean, areManyInputsSupported: Boolean, areManyOutputsSupported: Boolean)

  private implicit val ResponseWrites: Writes[ResponseObject] = Json.writes[ResponseObject]

  private val WavesCapabilities: JsValue = Json.toJson(ResponseObject(isTransactionsRebuildingSupported = false,
    areManyInputsSupported = false, areManyOutputsSupported = false))
}

case class CapabilitiesRoute() extends PlayJsonSupport {

  import CapabilitiesRoute._

  val route: Route = path("capabilities") {
    get {
      complete(WavesCapabilities)
    }
  }
}