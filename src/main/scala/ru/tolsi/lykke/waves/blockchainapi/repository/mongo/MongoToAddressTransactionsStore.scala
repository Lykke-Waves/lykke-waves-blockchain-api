package ru.tolsi.lykke.waves.blockchainapi.repository.mongo

import ru.tolsi.lykke.waves.blockchainapi.repository.ToAddressTransactionsStore

class MongoToAddressTransactionsStore(dbName: String) extends MongoAddressTransactionsStore(dbName, "to_transactions") with ToAddressTransactionsStore
