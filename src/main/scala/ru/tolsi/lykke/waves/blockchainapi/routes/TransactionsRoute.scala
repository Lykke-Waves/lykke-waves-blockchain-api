package ru.tolsi.lykke.waves.blockchainapi.routes

import ru.tolsi.lykke.waves.blockchainapi.repository.BroadcastOperationsStore


//  [POST] /api/transactions/broadcast
//  [DELETE] /api/transactions/broadcast/{operationId}
// X [POST] /api/transactions/single
// X [POST] /api/transactions/many-inputs
// X [POST] /api/transactions/many-outputs
// X [PUT] /api/transactions
// X [GET] /api/transactions/broadcast/single/{operationId}
// X [GET] /api/transactions/broadcast/many-inputs/{operationId}
// X [GET] /api/transactions/broadcast/many-outputs/{operationId}
case class TransactionsRoute(store: BroadcastOperationsStore) {

}
