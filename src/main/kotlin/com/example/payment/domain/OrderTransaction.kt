package com.example.payment.domain

import com.example.payment.TransactionStatus
import com.example.payment.TransactionType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
class OrderTransaction(
    val transactionId: String,
    @ManyToOne
    val order: Order,
    @Enumerated(EnumType.STRING)
    val transactionType: TransactionType,
    @Enumerated(EnumType.STRING)
    var transactionStatus: TransactionStatus,
    val transactionAmount: Long,
    val merchantTransactionId: String,
    var payMethodTransactionId: String? = null,
    var transactionAt: LocalDateTime? = null,
    var failureCode: String? = null,
    var description: String? = null
) : BaseEntity()