package com.datn.Service;

import com.datn.model.Order;
import vn.payos.type.PaymentData;

public interface PayOSService {
    
    /**
     * Tạo link thanh toán PayOS cho đơn hàng
     * @param order Đơn hàng cần thanh toán
     * @return URL thanh toán
     */
    String createPaymentLink(Order order) throws Exception;
    
    /**
     * Xác minh webhook từ PayOS
     * @param webhookBody Nội dung webhook
     * @param signature Chữ ký webhook
     * @return true nếu hợp lệ
     */
    boolean verifyWebhook(String webhookBody, String signature);
    
    /**
     * Xử lý kết quả thanh toán từ webhook
     * @param paymentData Dữ liệu thanh toán
     */
    void handlePaymentResult(PaymentData paymentData);
    
    /**
     * Kiểm tra trạng thái thanh toán
     * @param orderCode Mã đơn hàng
     * @return Thông tin thanh toán
     */
    PaymentData getPaymentInfo(String orderCode) throws Exception;
    
    /**
     * Hủy thanh toán
     * @param orderCode Mã đơn hàng
     * @param cancelReason Lý do hủy
     * @return Thông tin hủy thanh toán
     */
    PaymentData cancelPayment(String orderCode, String cancelReason) throws Exception;
}