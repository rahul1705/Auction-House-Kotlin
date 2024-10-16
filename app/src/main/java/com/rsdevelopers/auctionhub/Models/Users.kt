package com.rsdevelopers.auctionhub.Models

import java.util.*

class Users {
    var name: String? = null
    var mobile: String? = null
    var email: String? = null
    var pass: String? = null
    var balance = "0"
    var userImage: String? = null
    var createdAt: Date? = null

    constructor() {}
    constructor(balance: String) {
        this.balance = balance
    }

    constructor(name: String?, mobile: String?, email: String?, pass: String?, createdAt: Date?) {
        this.name = name
        this.mobile = mobile
        this.email = email
        this.pass = pass
        this.createdAt = createdAt
    }
}