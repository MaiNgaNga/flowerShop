// package com.datn.Service;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;
// import org.apache.http.client.methods.HttpPost;
// import org.apache.http.impl.client.HttpClientBuilder;
// import org.apache.http.client.HttpClient;
// import org.apache.http.entity.StringEntity;
// import org.apache.http.HttpResponse;
// import org.apache.http.util.EntityUtils;
// import javax.crypto.Mac;
// import javax.crypto.spec.SecretKeySpec;
// import java.util.HashMap;
// import java.util.Map;
// import org.json.simple.JSONObject;
// import org.json.simple.parser.JSONParser;

// @Service
// public class ZaloPayService {
//     @Value("${zalopay.app_id}")
//     private String appId;
//     @Value("${zalopay.key1}")
//     private String key1;
//     @Value("${zalopay.endpoint}")
//     private String endpoint;
//     @Value("${zalopay.callback_url}")
//     private String callbackUrl;
//     @Value("${zalopay.return_url}")
//     private String returnUrl;

//     public String createPaymentUrl(long amount, String orderId, String description) throws Exception {
//         Map<String, Object> order = new HashMap<>();
//         order.put("app_id", appId);
//         order.put("app_trans_id", orderId);
//         order.put("app_user", "user123");
//         order.put("app_time", System.currentTimeMillis());
//         order.put("amount", amount);
//         order.put("item", "[]");
//         order.put("description", description);
//         order.put("bank_code", "zalopayapp");
//         order.put("callback_url", callbackUrl);
//         order.put("return_url", returnUrl);

//         String data = appId + "|" + orderId + "|user123|" + amount + "|" + order.get("app_time") + "|zalopayapp|"
//                 + description + "|" + callbackUrl + "|" + returnUrl;
//         String mac = hmacSHA256(data, key1);
//         order.put("mac", mac);

//         HttpClient client = HttpClientBuilder.create().build();
//         HttpPost post = new HttpPost(endpoint);
//         StringEntity params = new StringEntity(new JSONObject(order).toJSONString(), "UTF-8");
//         post.setHeader("Content-type", "application/json");
//         post.setEntity(params);

//         HttpResponse response = client.execute(post);
//         String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
//         JSONObject jsonResponse = (JSONObject) new JSONParser().parse(responseString);

//         return (String) jsonResponse.get("order_url");
//     }

//     private String hmacSHA256(String data, String key) throws Exception {
//         Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
//         SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
//         sha256_HMAC.init(secret_key);
//         byte[] hash = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
//         StringBuilder sb = new StringBuilder();
//         for (byte b : hash)
//             sb.append(String.format("%02x", b));
//         return sb.toString();
//     }
// }
