package com.rsdevelopers.auctionhub.Models

import java.util.*

class Transactions {
    var transDate: Date? = null
    var transAmount = 0.0
    var transType: String? = null
    var transReason: String? = null
    var transId: String? = null

    constructor() {}
    constructor(
        transDate: Date?,
        transAmount: Double,
        transType: String?,
        transReason: String?,
        transId: String?
    ) {
        this.transDate = transDate
        this.transAmount = transAmount
        this.transType = transType
        this.transReason = transReason
        this.transId = transId
    }
}