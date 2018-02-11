package ru.tolsi.lykke.waves.blockchainapi

import akka.http.scaladsl.server.{HttpApp, Route}
import com.mongodb.casbah.{MongoClient, MongoCollection, MongoDB}
import ru.tolsi.lykke.common.http.LykkeApiServer
import ru.tolsi.lykke.common.http.routes.IsAliveRoute
import ru.tolsi.lykke.common.repository.mongo._
import ru.tolsi.lykke.common.{NetworkType, Util}
import ru.tolsi.lykke.waves.blockchainapi.routes._

object Server extends App {
  val mongoClient = MongoClient()
  val db = mongoClient.getDB("lykke-waves")
  new Server(db).startServer("localhost", 8080)
}

class Server(db: MongoDB) extends HttpApp with LykkeApiServer {
  private val networkType: NetworkType = NetworkType.Main

  private val isAliveRoute = IsAliveRoute(ProjectInfo.NameString, ProjectInfo.VersionString,
    sys.env.getOrElse("ENV_INFO", ""), Util.isDebug).route

  private val capabilitiesRoute = CapabilitiesRoute().route

  // todo params
  // todo not realized methods
  private val addressesRoute = new AddressesRoute(networkType).route
  private val assetsRoute = new AssetsRoute(new MongoAssetsStore(new MongoCollection(db.getCollection("assets")))).route
  private val balancesRoute = new BalancesRoute(new MongoBalancesStore(new MongoCollection(db.getCollection("balances")),
    new MongoCollection(db.getCollection("balances_observations")))).route
  private val transactionsRoute = new TransactionsRoute(new MongoBroadcastOperationsStore(new MongoCollection(db.getCollection("transactions")))).route
  private val transactionsHistoryRoute = new TransactionsHistoryRoute(
    new MongoFromAddressTransactionsStore(new MongoCollection(db.getCollection("from_address_transactions")),
      new MongoCollection(db.getCollection("from_address_transactions_observations"))),
    new MongoToAddressTransactionsStore(new MongoCollection(db.getCollection("to_address_transactions")),
      new MongoCollection(db.getCollection("to_address_transactions_observations")))
  ).route

  override val routes: Route = handleRejections {
    pathPrefix("api") {
      Seq(isAliveRoute, capabilitiesRoute, addressesRoute, assetsRoute, balancesRoute, transactionsRoute, transactionsHistoryRoute).reduce(_ ~ _)
    }
  }
}