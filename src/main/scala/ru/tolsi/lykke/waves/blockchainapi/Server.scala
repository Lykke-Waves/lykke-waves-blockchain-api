package ru.tolsi.lykke.waves.blockchainapi

import akka.http.scaladsl.server.{HttpApp, Route}
import com.mongodb.casbah.{MongoClient, MongoCollection, MongoDB}
import ru.tolsi.lykke.common.api.WavesApi
import ru.tolsi.lykke.common.http.LykkeApiServer
import ru.tolsi.lykke.common.http.routes.IsAliveRoute
import ru.tolsi.lykke.common.repository.mongo._
import ru.tolsi.lykke.common.{NetworkType, Util}
import ru.tolsi.lykke.waves.blockchainapi.routes._

object Server extends App {
  private val settingsUrl = Option(System.getProperty("SettingsUrl"))
  private val settings = BlockchainAPISettings.loadSettings(settingsUrl)

  private val mongoClient = MongoClient(settings.MongoDBHost, settings.MongoDBPort)

  private val dbName = if (settings.NetworkType == NetworkType.Main) "lykke-waves" else "lykke-waves-testnet"
  private val db = mongoClient.getDB(dbName)

  new Server(db, settings).startServer(settings.ServiceHost, settings.ServicePort)
}

class Server(db: MongoDB, settings: BlockchainAPISettings) extends HttpApp with LykkeApiServer {
  private val networkType: NetworkType = settings.NetworkType

  private val isAliveRoute = IsAliveRoute(ProjectInfo.NameString, ProjectInfo.VersionString,
    sys.env.getOrElse("ENV_INFO", ""), Util.isDebug).route

  private val capabilitiesRoute = CapabilitiesRoute().route

  private val apiUrl = if (settings.NetworkType == NetworkType.Main) "https://nodes.wavesnodes.com" else "https://testnodes.wavesnodes.com/"
  private val api = new WavesApi(apiUrl)

  private val addressesRoute = new AddressesRoute(networkType).route
  private val assetsRoute = new AssetsRoute(new MongoAssetsStore(new MongoCollection(db.getCollection("assets")))).route
  private val balancesRoute = new BalancesRoute(new MongoBalancesStore(new MongoCollection(db.getCollection("balances")),
    new MongoCollection(db.getCollection("balances_observations")))).route
  private val transactionsRoute = new TransactionsRoute(new MongoBroadcastOperationsStore(new MongoCollection(db.getCollection("transactions"))), api).route
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