package com.example.payment.repository

import com.example.payment.TransactionType
import com.example.payment.domain.Order
import com.example.payment.domain.OrderTransaction
import com.example.payment.domain.PaymentUser
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentUserRepository : JpaRepository<PaymentUser, Long> {
    fun findByPayUserId(payUserId: String): PaymentUser? //있을떄는 생기고 없을때는 null
}

interface OrderRepository : JpaRepository<Order, Long> {

}

interface OrderTransactionRepository : JpaRepository<OrderTransaction, Long> {
    fun findByOrderAndTransactionType(order: Order, transactionType: TransactionType): List<OrderTransaction>

}