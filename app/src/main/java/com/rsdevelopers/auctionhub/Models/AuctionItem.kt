package com.rsdevelopers.auctionhub.Models

import java.util.*

class AuctionItem {
    var itemName: String? = null
    var itemImage: String? = null
    var itemDesc: String? = null
    var sellerId: String? = null
    var itemId: String? = null
    var itemStatus: String? = null
    var buyerId: String? = null
    var currentBid = 0.0
    var startDate: Date? = null
    var expiryDate: String? = null

    constructor() {}
    constructor(
        itemId: String?,
        itemName: String?,
        itemImage: String?,
        itemDesc: String?,
        expiryDate: String?,
        currentBid: Double,
        sellerId: String?,
        startDate: Date?,
        itemStatus: String?
    ) {
        this.itemId = itemId
        this.itemName = itemName
        this.itemImage = itemImage
        this.itemDesc = itemDesc
        this.expiryDate = expiryDate
        this.currentBid = currentBid
        this.sellerId = sellerId
        this.startDate = startDate
        this.itemStatus = itemStatus
    }
}