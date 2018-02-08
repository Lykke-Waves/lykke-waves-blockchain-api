package ru.tolsi.lykke.waves.blockchainapi.repository.mongo

import ru.tolsi.lykke.waves.blockchainapi.repository.FromAddressTransactionsStore

class MongoFromAddressTransactionsStore(dbName: String) extends MongoAddressTransactionsStore(dbName, "from_transactions") with FromAddressTransactionsStore