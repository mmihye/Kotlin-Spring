package com.example.payment.service

import com.example.payment.OrderStatus
import com.example.payment.OrderStatus.PARTIAL_REFUNDED
import com.example.payment.OrderStatus.REFUNDED
import com.example.payment.TransactionStatus
import com.example.payment.TransactionStatus.SUCCESS
import com.example.payment.TransactionType
import com.example.payment.domain.Order
import com.example.payment.domain.OrderTransaction
import com.example.payment.exception.ErrorCode
import com.example.payment.exception.ErrorCode.*
import com.example.payment.exception.PaymentException
import com.example.payment.repository.OrderRepository
import com.example.payment.repository.OrderTransactionRepository
import com.example.payment.util.generateRefundTransactionId
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * 환불의 요청 저장, 성공, 실패 저장
 */
@Service
class RefundStatusService(
    private val orderRepository: OrderRepository,
    private val orderTransactionRepository: OrderTransactionRepository
) {

    @Transactional
    fun saveRefundRequest(
        originalTransactionId: String,
        merchantRefundId: String,
        refundAmount: Long,
        refundReason: String

    ): Long {
        // 결제(orderTransaction) 확인
        val originalTransaction =
            orderTransactionRepository.findByTransactionId(originalTransactionId)
                ?: throw PaymentException(ORDER_NOT_FOUND)

        val order = originalTransaction.order

        // 환불이 가능한지 확인
        validationRefund(order, refundAmount)

        // 환불 트랜잭션(orderTransaction) 저장
        return orderTransactionRepository.save(
            OrderTransaction(
                transactionId = generateRefundTransactionId(),
                order = order,
                transactionType = TransactionType.REFUND,
                transactionStatus = TransactionStatus.RESERVE,
                transactionAmount = refundAmount,
                merchantTransactionId = merchantRefundId,
                description = refundReason
            )
        ).id ?: throw PaymentException(INTERNAL_SERVER_ERROR)

        return order.id ?: throw PaymentException(INTERNAL_SERVER_ERROR)
    }

    private fun validationRefund(order: Order, refundAmount: Long) {
        if (order.orderStatus !in listOf(OrderStatus.PAID, PARTIAL_REFUNDED)) {
            throw PaymentException(CANNOT_REFUND)
        }
        if (order.refundedAmount + refundAmount > order.paidAmount) {
            throw PaymentException(EXCEED_REFUNDABLE_AMOUNT)
        }
    }

    fun saveAsSuccess(refundTxId: Long, refundMethodTransactionId: String): Pair<String, LocalDateTime> {

        val orderTransaction =
            orderTransactionRepository.findById(refundTxId).orElseThrow {
                throw PaymentException(INTERNAL_SERVER_ERROR)
            }
                .apply {
                    transactionStatus = SUCCESS
                    this.payMethodTransactionId = refundMethodTransactionId
                    transactionAt = LocalDateTime.now()
                }

        val order = orderTransaction.order
        val totalRefundAmount = getTotalRefundedAmount(order)

        order.apply {
            orderStatus = getNewOrderStatus(this, totalRefundAmount)
            refundedAmount = totalRefundAmount
        }

        return Pair(
            orderTransaction.transactionId,
            orderTransaction.transactionAt ?: throw PaymentException(
                INTERNAL_SERVER_ERROR
            )
        )
    }

    private fun getNewOrderStatus(
        order: Order, totalRefundAmount: Long
    ): OrderStatus =
        if (order.orderAmount == totalRefundAmount) REFUNDED
        else PARTIAL_REFUNDED


    private fun getTotalRefundedAmount(order: Order): Long =
        orderTransactionRepository.findByOrderAndTransactionType(order, TransactionType.REFUND)
            .filter { it.transactionStatus == SUCCESS }
            .sumOf { it.transactionAmount }

    fun saveAsFailure(refundTxId: Long, errorCode: ErrorCode) {
        orderTransactionRepository.findById(refundTxId)
            .orElseThrow { throw PaymentException(INTERNAL_SERVER_ERROR) }
            .apply {
                transactionStatus = TransactionStatus.FAILURE
                failureCode = errorCode.name
                description = errorCode.errorMessage
            }

    }

}