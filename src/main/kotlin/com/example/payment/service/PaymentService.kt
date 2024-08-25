package com.example.payment.service

import com.example.payment.exception.ErrorCode
import com.example.payment.exception.PaymentException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PaymentService(
    private val paymentStatusService: PaymentStatusService,
    private val accountService: AccountService
) {

    fun pay(
        payServiceRequest: PayServiceRequest
    ): PayServiceResponse {
        // 요청을 (요청됨으로) 저장
        val orderId = paymentStatusService.savePayRequest(
            payUserId = payServiceRequest.payUserId,
            amount = payServiceRequest.amount,
            orderTitle = payServiceRequest.orderTitle,
            merchantTransactionId = payServiceRequest.merchantTransactionId

        )

        return try {
            // 계좌에 금액 사용 요청
            val payMethodTransactionId = accountService.useAccount(orderId)

            // 성공 : 거래를 성공으로 저장
            val (transactionId, transactedAt) = paymentStatusService.saveAsSuccess(orderId, payMethodTransactionId)


            PayServiceResponse(
                payUserId = payServiceRequest.payUserId,
                amount = payServiceRequest.amount,
                transactionId = transactionId,
                transactedAt = transactedAt

            )
        } catch (e: Exception) {
            // 실패 : 거래를 실패로 저장
            paymentStatusService.saveAsFailure(orderId, getErrorCode(e))

            throw e
        }
    }

    fun getErrorCode(e: Exception) = if (e is PaymentException) e.errorCode
    else ErrorCode.INTERNAL_SERVER_ERROR

}

data class PayServiceResponse(
    val payUserId: String,
    val amount: Long,
    val transactionId: String,
    val transactedAt: LocalDateTime,
)

data class PayServiceRequest(
    val payUserId: String,
    val amount: Long,
    val merchantTransactionId: String,
    val orderTitle: String,
)
