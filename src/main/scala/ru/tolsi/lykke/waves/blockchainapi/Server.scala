package ru.tolsi.lykke.waves.blockchainapi

import akka.http.scaladsl.server.{HttpApp, Route}
import ru.tolsi.lykke.common.http.LykkeApiServer
import ru.tolsi.lykke.common.http.routes.IsAliveRoute
import ru.tolsi.lykke.common.{NetworkType, Util}
import ru.tolsi.lykke.waves.blockchainapi.routes.CapabilitiesRoute

object Server extends HttpApp with LykkeApiServer with App {
  private val networkType: NetworkType = NetworkType.Main

  private val isAliveRoute = IsAliveRoute(ProjectInfo.NameString, ProjectInfo.VersionString,
    sys.env.getOrElse("ENV_INFO", ""), Util.isDebug).route

  private val capabilitiesRoute = CapabilitiesRoute().route

  override def routes: Route = handleRejections {
    pathPrefix("api") {
      isAliveRoute ~ capabilitiesRoute
    }
  }

  Server.startServer("localhost", 8080)
}
