package com.datn.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

@Service
public class QRCodeService {

    private static final String QR_CODE_IMAGE_PATH = "target/classes/static/images/qr/";

    public String generateQRCodeImage(String text, int width, int height, String fileName)
            throws WriterException, IOException {

        // Tạo thư mục nếu chưa tồn tại
        File directory = new File(QR_CODE_IMAGE_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        Path path = FileSystems.getDefault().getPath(QR_CODE_IMAGE_PATH + fileName);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

        return "/images/qr/" + fileName;
    }

    public String generatePaymentQRCode(String orderCode, double amount, String bankAccount) {
        try {
            // Tạo nội dung QR theo chuẩn VietQR
            String qrContent = String.format(
                    "00020101021138570010A00000072701270006970455011%s0208QRIBFTTA5303704540%.0f5802VN6304",
                    bankAccount, amount);

            String fileName = "payment_" + orderCode + ".png";
            return generateQRCodeImage(qrContent, 300, 300, fileName);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String generateBankTransferQR(String orderCode, double amount, String bankInfo) {
        try {
            // Nội dung chuyển khoản ngân hàng
            String transferContent = String.format(
                    "Bank: %s\nAmount: %.0f VND\nContent: Thanh toan don hang %s",
                    bankInfo, amount, orderCode);

            String fileName = "transfer_" + orderCode + ".png";
            return generateQRCodeImage(transferContent, 300, 300, fileName);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}