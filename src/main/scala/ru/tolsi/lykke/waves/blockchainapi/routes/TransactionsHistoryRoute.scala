package ru.tolsi.lykke.waves.blockchainapi.routes

import ru.tolsi.lykke.waves.blockchainapi.repository.{FromAddressTransactionsStore, ToAddressTransactionsStore}

//  [POST] /api/transactions/history/from/{address}/observation
//  [POST] /api/transactions/history/to/{address}/observation
//  [DELETE] /api/transactions/history/from/{address}/observation
//  [DELETE] /api/transactions/history/to/{address}/observation
//  [GET] /api/transactions/history/from/{address}?take=integer&[afterHash=string]
//  [GET] /api/transactions/history/to/{address}?take=integer&[afterHash=string]
case class TransactionsHistoryRoute(fromStore: FromAddressTransactionsStore, toStore: ToAddressTransactionsStore) {

}
