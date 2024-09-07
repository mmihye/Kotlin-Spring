package com.example.payment.controller

import com.example.payment.service.RefundService
import com.example.payment.service.RefundServiceRequest
import com.example.payment.service.RefundServiceResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RequestMapping("/api/v1")
@RestController
class RefundController(
    private val refundService: RefundService
) {
    @PostMapping("/refund")
    fun pay(
        @Valid @RequestBody
        refundRequest: RefundRequest
    ): RefundServiceResponse = RefundResponse.from(
        refundService.refund(
            refundRequest.toRefundServiceRequest()
        )
    )
}

data class RefundResponse(
    val refundTransactionId: String,
    val refundAmount: Long,
    val refundAt: LocalDateTime,
) {
    companion object {
        fun from(response: RefundServiceResponse) =
            RefundServiceResponse(
                refundTransactionId = response.refundTransactionId,
                refundAmount = response.refundAmount,
                refundAt = response.refundAt
            )
    }
}

data class RefundRequest(
    val transactionId: String,
    val refundId: String,
    val refundAmount: Long,
    val refundReason: String
) {
    fun toRefundServiceRequest() = RefundServiceRequest(
        transactionId = transactionId,
        refundId = refundId,
        refundAmount = refundAmount,
        refundReason = refundReason
    )
}
