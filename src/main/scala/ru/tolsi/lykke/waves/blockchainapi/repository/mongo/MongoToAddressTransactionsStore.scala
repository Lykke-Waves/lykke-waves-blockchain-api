package ru.tolsi.lykke.waves.blockchainapi.repository.mongo

import com.mongodb.casbah.MongoCollection
import ru.tolsi.lykke.waves.blockchainapi.repository.ToAddressTransactionsStore

class MongoToAddressTransactionsStore(collection: MongoCollection, observationsCollection: MongoCollection) extends MongoAddressTransactionsStore(collection, observationsCollection) with ToAddressTransactionsStore
