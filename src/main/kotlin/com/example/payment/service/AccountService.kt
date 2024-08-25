package com.example.payment.service

import com.example.payment.adatper.AccountAdapter
import com.example.payment.adatper.UseBalanceRequest
import com.example.payment.domain.Order
import com.example.payment.exception.ErrorCode
import com.example.payment.exception.PaymentException
import com.example.payment.repository.OrderRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class AccountService(
    private val accountAdapter: AccountAdapter,
    private val orderRepository: OrderRepository
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
}