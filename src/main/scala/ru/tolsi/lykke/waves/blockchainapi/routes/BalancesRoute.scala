package ru.tolsi.lykke.waves.blockchainapi.routes

import ru.tolsi.lykke.waves.blockchainapi.repository.BalancesStore

//  [POST] /api/balances/{address}/observation
//  [DELETE] /api/balances/{address}/observation
//  [GET] /api/balances?take=integer&[continuation=string]
case class BalancesRoute(store: BalancesStore) {

}
