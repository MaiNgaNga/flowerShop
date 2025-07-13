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

    // Sinh QR chuyển khoản ngân hàng theo chuẩn VietQR sử dụng API VietQR
    public String generateBankTransferQR(String orderCode, double amount, String bankAccount, String bankCode,
            String accountName) {
        try {
            // Use correct acqId for Techcombank (TCB): 970407
            String acqId = "970407";
            String addInfo = "Thanh toan don hang " + orderCode;
            String apiUrl = "https://api.vietqr.io/v2/generate";

            // Build JSON body
            String safeAccountName = accountName.replace("\"", "");
            String safeAddInfo = addInfo.replace("\"", "");
            String jsonBody = String.format(
                    "{\"accountNo\":\"%s\",\"accountName\":\"%s\",\"acqId\":\"%s\",\"amount\":%.0f,\"addInfo\":\"%s\",\"format\":\"png\"}",
                    bankAccount,
                    safeAccountName,
                    acqId,
                    amount,
                    safeAddInfo);

            java.net.URL url = new java.net.URL(apiUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            try (java.io.OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            if (code != 200) {
                System.err.println("VietQR API error: HTTP " + code);
                return null;
            }

            // Read response JSON
            StringBuilder response = new StringBuilder();
            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), "utf-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
            }

            // Parse base64 image from JSON (field: "data" -> "qrDataURL" or "qrData")
            String json = response.toString();
            String base64 = null;
            // Try to extract "qrDataURL" (data:image/png;base64,...)
            int idx = json.indexOf("qrDataURL");
            if (idx != -1) {
                int start = json.indexOf("data:image/png;base64,", idx);
                if (start != -1) {
                    start += "data:image/png;base64,".length();
                    int end = json.indexOf('"', start);
                    base64 = json.substring(start, end);
                }
            }
            // Fallback: try "qrData"
            if (base64 == null) {
                idx = json.indexOf("qrData");
                if (idx != -1) {
                    int start = json.indexOf('"', idx + 7) + 1;
                    int end = json.indexOf('"', start);
                    base64 = json.substring(start, end);
                }
            }
            if (base64 == null) {
                System.err.println("Không tìm thấy ảnh QR trong phản hồi API VietQR!");
                return null;
            }

            // Decode base64 and save to file
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64);
            String fileName = "transfer_" + orderCode + ".png";
            java.nio.file.Path path = java.nio.file.Paths.get(QR_CODE_IMAGE_PATH + fileName);
            java.nio.file.Files.write(path, imageBytes);

            return "/images/qr/" + fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}