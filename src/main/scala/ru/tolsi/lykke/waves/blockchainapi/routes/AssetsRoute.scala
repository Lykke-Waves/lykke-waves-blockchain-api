package ru.tolsi.lykke.waves.blockchainapi.routes

import ru.tolsi.lykke.waves.blockchainapi.repository.AssetsStore

//  [GET] /api/assets?take=integer&[continuation=string]
//  [GET] /api/assets/{assetId}
case class AssetsRoute(store: AssetsStore) {

}
