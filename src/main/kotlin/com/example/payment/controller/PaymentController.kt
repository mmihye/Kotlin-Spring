package com.example.payment.controller

import com.example.payment.service.PaymentService
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RequestMapping("/api/v1")
@RestController
class PaymentController(
    private val paymentService: PaymentService // 선언하면서 프로퍼티를 선언하면 스프링이 알아서 의존성을 주입해줌
) {
    @PostMapping("/pay")
    fun pay(
        @Valid @RequestBody
        payRequest: PayRequest
    ): PayResponse {
        return PayResponse("p1", 100, "txId", LocalDateTime.now())
    }
}

data class PayResponse(
    val payUserId: String,
    val amount: Long,
    val transactionId: String,
    val transactedAt: LocalDateTime,
)

data class PayRequest(
    @field:NotBlank //@NotBlank하게되면 생성자에서만 체크하므로 field를 붙여서 필드일때도 체크하게 해주어야함
    val payUserId: String,
    @field:Min(100)
    val amount: Long,
    @field:NotBlank
    val merchantTransactionId: String,
    @field:NotBlank
    val orderTitle: String,
)
