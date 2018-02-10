package ru.tolsi.lykke.waves.blockchainapi.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json.{Json, Writes}
import ru.tolsi.lykke.common.NetworkType
import ru.tolsi.lykke.common.http.NetworkScheme
import scorex.crypto.encode.Base58
import scorex.crypto.hash.{Blake2b256, Keccak256}
import scorex.utils.HashHelpers

import scala.util.Try

case class Address(address: String)

case class InvalidAddress(reason: String) extends Exception

object Address {
  private val BytesMaxValue = 256
  private val Base58MaxValue = 58

  private val BytesLog = math.log(BytesMaxValue)
  private val BaseLog = math.log(Base58MaxValue)

  private def base58Length(byteArrayLength: Int): Int = math.ceil(BytesLog / BaseLog * byteArrayLength).toInt

  val Prefix: String = "address:"

  val AddressVersion: Byte = 1
  val ChecksumLength = 4
  val HashLength = 20
  val AddressLength = 1 + 1 + ChecksumLength + HashLength
  val AddressStringLength = base58Length(AddressLength)

  private def hash(bytes: Array[Byte]): Array[Byte] = {
    HashHelpers.applyHashes(bytes, Blake2b256, Keccak256)
  }

  def fromPublicKey(publicKey: Array[Byte], scheme: Byte): Address = {
    val publicKeyHash = hash(publicKey).take(HashLength)
    val withoutChecksum = AddressVersion +: scheme +: publicKeyHash
    val bytes = withoutChecksum ++ calcCheckSum(withoutChecksum)
    Address(Base58.encode(bytes))
  }

  def fromBytes(addressBytes: Array[Byte], scheme: Byte): Try[Address] = {
    val version = addressBytes.head
    val network = addressBytes.tail.head
    (for {
      _ <- Either.cond(version == AddressVersion, (), s"Unknown address version: $version")
      _ <- Either.cond(network == scheme, (), s"Data from other network: expected: $scheme(${scheme.toChar}, actual: $network(${network.toChar}")
      _ <- Either.cond(addressBytes.length == Address.AddressLength, (), s"Wrong addressBytes length: expected: ${Address.AddressLength}, actual: ${addressBytes.length}")
      checkSum = addressBytes.takeRight(ChecksumLength)
      checkSumGenerated = calcCheckSum(addressBytes.dropRight(ChecksumLength))
      _ <- Either.cond(checkSum.sameElements(checkSumGenerated), (), s"Bad address checksum")
    } yield Address(Base58.encode(addressBytes))).left.map(InvalidAddress).toTry
  }

  def fromString(addressStr: String, scheme: Byte): Try[Address] = {
    val base58String = if (addressStr.startsWith(Prefix)) addressStr.drop(Prefix.length) else addressStr
    for {
      _ <- Either.cond(base58String.length <= AddressStringLength, (), InvalidAddress(s"Wrong address string length: max=$AddressStringLength, actual: address.length")).toTry
      byteArray <- Base58.decode(base58String).toEither.left.map(ex => InvalidAddress(s"Unable to decode base58: ${ex.getMessage}")).toTry
      address <- fromBytes(byteArray, scheme)
    } yield address
  }

  private def calcCheckSum(withoutChecksum: Array[Byte]): Array[Byte] = hash(withoutChecksum).take(ChecksumLength)
}

object AddressesRoute {
  implicit val addressWrites: Writes[Address] = Json.writes[Address]
}

//  [GET] /api/addresses/{address}/validity
case class AddressesRoute(networkType: NetworkType) extends PlayJsonSupport with NetworkScheme {

  import AddressesRoute._

  val route: Route = path("addresses") {
    pathPrefix(Segment) { address =>
      pathSuffix("validity") {
        get {
          complete(Address.fromString(address, scheme.toByte))
        }
      }
    }
  }
}
