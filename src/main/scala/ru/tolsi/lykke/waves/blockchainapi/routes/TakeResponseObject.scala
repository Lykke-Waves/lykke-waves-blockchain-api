package ru.tolsi.lykke.waves.blockchainapi.routes

import play.api.libs.json.{JsArray, Json, Writes}

object TakeResponseObject {
  implicit val TakeResponseObjectWrites: Writes[TakeResponseObject] = Json.writes[TakeResponseObject]
}

case class TakeResponseObject(continuation: String, items: JsArray)
