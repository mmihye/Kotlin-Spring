package com.example.payment.domain

import com.example.payment.OrderStatus
import jakarta.persistence.*

@Entity
@Table(name = "orders")
class Order(
    //변화하지 않는 값:val, 변화할 가능성이 있는 값:var
    val orderId: String,
    @ManyToOne
    val paymentUser: PaymentUser,
    @Enumerated(EnumType.STRING)
    var orderStatus: OrderStatus,
    val orderTitle: String,
    val orderAmount: Long,
    var paidAmount: Long,
    var refundedAmount: Long
) : BaseEntity()