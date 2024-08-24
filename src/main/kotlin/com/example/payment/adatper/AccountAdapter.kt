package com.example.payment.adatper

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.time.LocalDateTime

@FeignClient(
    name = "account-adapter",
    url = "http://localhost:8080"
)
interface AccountAdapter {

    @PostMapping("/transaction/use")
    fun useAccount(
        @RequestBody useBalanceRequest: UseBalanceRequest
    ): UseBalanceResponse
}

class UseBalanceResponse(
    var accountNumber: String, //private 삭제해야 외부에서 접근가능
    val transactionResult: TransactionResultType,
    val transactionId: String,
    val amount: Long,
    val transactedAt: LocalDateTime
)

class UseBalanceRequest(
    val userId: Long,
    val accountNumber: String,
    val amount: Long
)


enum class TransactionResultType {
    S,
    F
}

