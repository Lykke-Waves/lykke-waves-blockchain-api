package ru.tolsi.lykke.waves.blockchainapi.repository.mongo

import com.mongodb.casbah.MongoCollection
import ru.tolsi.lykke.waves.blockchainapi.repository.FromAddressTransactionsStore

class MongoFromAddressTransactionsStore(collection: MongoCollection, observationsCollection: MongoCollection) extends MongoAddressTransactionsStore(collection, observationsCollection, "fromAddress") with FromAddressTransactionsStore