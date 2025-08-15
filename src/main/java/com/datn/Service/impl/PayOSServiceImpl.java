package com.datn.Service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datn.Service.OrderService;
import com.datn.Service.PayOSService;
import com.datn.model.Order;
import com.datn.model.OrderDetail;

import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.PaymentLinkData;

@Service
public class PayOSServiceImpl implements PayOSService {

    @Autowired
    private PayOS payOS;

    @Autowired
    private OrderService orderService;

    // Tạo description ngắn gọn cho PayOS (tối đa 25 ký tự)
    private String createShortDescription(String orderCode, String suffix) {
        String desc = "DH#" + orderCode;
        if (suffix != null && !suffix.isEmpty()) {
            desc += " " + suffix;
        }
        // PayOS giới hạn description tối đa 25 ký tự
        if (desc.length() > 25) {
            desc = desc.substring(0, 25);
        }
        return desc;
    }

    @Override
    public String createPaymentLink(Order order) throws Exception {
        try {
            // Tạo danh sách items từ order details
            List<ItemData> items = new ArrayList<>();

            if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
                for (OrderDetail detail : order.getOrderDetails()) {
                    ItemData item = ItemData.builder()
                            .name(detail.getProduct().getName())
                            .quantity(detail.getQuantity())
                            .price(detail.getPrice().intValue())
                            .build();
                    items.add(item);
                }
            } else {
                // Fallback nếu không có order details
                ItemData item = ItemData.builder()
                        .name("Đơn hàng #" + order.getOrderCode())
                        .quantity(1)
                        .price(order.getTotalAmount().intValue())
                        .build();
                items.add(item);
            }

            // Tạo payment data với description ngắn (tối đa 25 ký tự)
            PaymentData paymentData = PaymentData.builder()
                    .orderCode(Long.parseLong(order.getOrderCode()))
                    .amount(order.getTotalAmount().intValue())
                    .description(createShortDescription(order.getOrderCode(), null))
                    .items(items)
                    .returnUrl("http://localhost:8080/order/payment-success?orderCode=" + order.getOrderCode())
                    .cancelUrl("http://localhost:8080/order/payment-cancel?orderCode=" + order.getOrderCode())
                    .build();

            // Tạo PayOS payment link
            CheckoutResponseData result = payOS.createPaymentLink(paymentData);

            System.out.println("PayOS Payment Link created for order: " + order.getOrderCode());
            System.out.println("Amount: " + order.getTotalAmount());
            System.out.println("Payment URL: " + result.getCheckoutUrl());

            // Return URL thực từ PayOS
            return result.getCheckoutUrl();

        } catch (Exception e) {
            throw new Exception("Lỗi tạo link thanh toán: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyWebhook(String webhookBody, String signature) {
        try {
            // Tạo Webhook object từ webhookBody và signature
            return true; // Tạm thời return true, cần implement đúng sau
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void handlePaymentResult(PaymentData paymentData) {
        try {
            // Webhook handler - có thể implement sau nếu cần
            // Hiện tại sử dụng callback URL để xử lý kết quả thanh toán
            System.out.println("Nhận webhook từ PayOS");
        } catch (Exception e) {
            // Log error
            System.err.println("Lỗi xử lý webhook PayOS: " + e.getMessage());
        }
    }

    @Override
    public PaymentData getPaymentInfo(String orderCode) throws Exception {
        try {
            // Lấy thông tin payment link từ PayOS
            PaymentLinkData paymentLinkData = payOS.getPaymentLinkInformation(Long.parseLong(orderCode));

            // Chuyển đổi PaymentLinkData thành PaymentData
            String shortDesc = "DH#" + orderCode;
            if (shortDesc.length() > 25) {
                shortDesc = shortDesc.substring(0, 25);
            }

            return PaymentData.builder()
                    .orderCode(Long.parseLong(orderCode))
                    .amount(paymentLinkData.getAmount())
                    .description(shortDesc)
                    .build();
        } catch (Exception e) {
            throw new Exception("Lỗi lấy thông tin thanh toán: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentData cancelPayment(String orderCode, String cancelReason) throws Exception {
        try {
            // Hủy payment link trên PayOS
            PaymentLinkData cancelledPayment = payOS.cancelPaymentLink(Long.parseLong(orderCode), cancelReason);

            System.out.println("Cancel payment for order: " + orderCode + ", reason: " + cancelReason);

            // Chuyển đổi PaymentLinkData thành PaymentData
            String cancelDesc = "DH#" + orderCode + " huy";
            if (cancelDesc.length() > 25) {
                cancelDesc = cancelDesc.substring(0, 25);
            }

            return PaymentData.builder()
                    .orderCode(Long.parseLong(orderCode))
                    .amount(cancelledPayment.getAmount())
                    .description(cancelDesc)
                    .build();
        } catch (Exception e) {
            throw new Exception("Lỗi hủy thanh toán: " + e.getMessage(), e);
        }
    }
}