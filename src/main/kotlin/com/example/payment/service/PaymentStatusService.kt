package com.example.payment.service

import com.example.payment.OrderStatus
import com.example.payment.TransactionStatus
import com.example.payment.TransactionStatus.RESERVE
import com.example.payment.TransactionType.PAYMENT
import com.example.payment.domain.Order
import com.example.payment.domain.OrderTransaction
import com.example.payment.exception.ErrorCode
import com.example.payment.exception.ErrorCode.INTERNAL_SERVER_ERROR
import com.example.payment.exception.PaymentException
import com.example.payment.repository.OrderRepository
import com.example.payment.repository.OrderTransactionRepository
import com.example.payment.repository.PaymentUserRepository
import com.example.payment.util.generateOrderId
import com.example.payment.util.generateTransactionId
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * 결제의 요청 저장, 성공, 실패 저장
 */
@Service
class PaymentStatusService(
    private val paymentUserRepository: PaymentUserRepository,
    private val orderRepository: OrderRepository,
    private val orderTransactionRepository: OrderTransactionRepository
) {

    @Transactional
    fun savePayRequest(
        payUserId: String,
        amount: Long,
        orderTitle: String,
        merchantTransactionId: String
    ): Long {
        //order, orderTransaction 저장
        val paymentUser = paymentUserRepository.findByPayUserId(payUserId)
            ?: throw PaymentException(ErrorCode.INVALID_REQUEST, "사용자 없음 : $payUserId")

        val order = orderRepository.save(
            Order(
                orderId = generateOrderId(),
                paymentUser = paymentUser,
                orderStatus = OrderStatus.CREATED,
                orderTitle = orderTitle,
                orderAmount = amount
            )
        )

        orderTransactionRepository.save(
            OrderTransaction(
                transactionId = generateTransactionId(),
                order = order,
                transactionType = PAYMENT,
                transactionStatus = RESERVE,
                transactionAmount = amount,
                merchantTransactionId = merchantTransactionId,
                description = orderTitle
            )
        )

        return order.id ?: throw PaymentException(INTERNAL_SERVER_ERROR)
    }

    @Transactional
    fun saveAsSuccess(orderId: Long, payMethodTransactionId: String): Pair<String, LocalDateTime> {

        val order: Order = orderRepository.findById(orderId)
            .orElseThrow { throw PaymentException(ErrorCode.ORDER_NOT_FOUND) }
            .apply {  //필요한곳에서만 order를 수정
                orderStatus = OrderStatus.PAID
                paidAmount = orderAmount
            }

        val orderTransaction = orderTransactionRepository.findByOrderAndTransactionType(
            order = order,
            transactionType = PAYMENT
        ).first() //첫번째 값 반환
            .apply {
                transactionStatus = TransactionStatus.SUCCESS
                this.payMethodTransactionId = payMethodTransactionId
                transactionAt = LocalDateTime.now()
            }

        return Pair(
            orderTransaction.transactionId,
            orderTransaction.transactionAt ?: throw PaymentException(
                INTERNAL_SERVER_ERROR
            )
        )
    }
}