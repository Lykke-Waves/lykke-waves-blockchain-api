package ru.tolsi.lykke.waves.blockchainapi

import java.net.URL

import com.mongodb.ServerAddress
import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json._
import ru.tolsi.lykke.common.NetworkType

import scala.util.Try

object BlockchainAPISettings extends StrictLogging {
  val Default = BlockchainAPISettings(NetworkType.Main, ServerAddress.defaultHost(), ServerAddress.defaultPort())

  implicit val BlockchainAPISettingsReader: Reads[BlockchainAPISettings] = Json.reads[BlockchainAPISettings]

  def loadSettings(pathOpt: Option[String]): BlockchainAPISettings = {
    val contentStreamOpt = pathOpt.map(u => new URL(u).openStream)
    contentStreamOpt.flatMap(c => Try {
      Json.parse(c).as[BlockchainAPISettings]
    }.toOption).getOrElse {
      logger.warn(s"Can't read config from 'SettingsUrl', load defaults: ${BlockchainAPISettings.Default}")
      BlockchainAPISettings.Default
    }
  }
}

case class BlockchainAPISettings(NetworkType: NetworkType, MongoDBHost: String, MongoDBPort: Int)