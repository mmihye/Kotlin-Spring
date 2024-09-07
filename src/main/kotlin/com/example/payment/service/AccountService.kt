package com.example.payment.service

import com.example.payment.TransactionType
import com.example.payment.adatper.AccountAdapter
import com.example.payment.adatper.UseBalanceRequest
import com.example.payment.adatper.cancelBalanceRequest
import com.example.payment.domain.Order
import com.example.payment.exception.ErrorCode
import com.example.payment.exception.PaymentException
import com.example.payment.repository.OrderRepository
import com.example.payment.repository.OrderTransactionRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class AccountService(
    private val accountAdapter: AccountAdapter,
    private val orderRepository: OrderRepository,
    private val orderTransactionRepository: OrderTransactionRepository
) {
    @Transactional
    fun useAccount(orderId: Long): String {
        // 계좌 사용 요청 및 처리
        val order: Order = orderRepository.findById(orderId)
            .orElseThrow { throw PaymentException(ErrorCode.ORDER_NOT_FOUND) }


        return accountAdapter.useAccount(
            UseBalanceRequest(
                userId = order.paymentUser.accountUserId,
                accountNumber = order.paymentUser.accountNumber,
                amount = order.orderAmount
            )
        ).transactionId // 대사 처리를 위해 transactionId 반환
    }

    @Transactional
    fun cancelUseAccount(refundTxId: Long): String {
        val refundTransaction = orderTransactionRepository.findById(refundTxId).orElseThrow {
            throw PaymentException(ErrorCode.INTERNAL_SERVER_ERROR)
        }

        val order = refundTransaction.order
        val paymentTransaction = orderTransactionRepository.findByOrderAndTransactionType(
            order, TransactionType.PAYMENT
        ).first()

        return accountAdapter.cancelUseAccount(
            cancelBalanceRequest(
                transactionId = paymentTransaction.payMethodTransactionId
                    ?: throw PaymentException(ErrorCode.INTERNAL_SERVER_ERROR),
                accountNumber = order.paymentUser.accountNumber,
                amount = refundTransaction.transactionAmount

            )
        ).transactionId
    }
}