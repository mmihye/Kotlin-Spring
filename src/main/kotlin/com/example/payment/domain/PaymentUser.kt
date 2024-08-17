package com.example.payment.domain

import jakarta.persistence.Entity
@Entity
class PaymentUser (
    val payUserId: String,
    val accountUserId: Long,
    var accountNumber: String,
    val name: String
):BaseEntity()