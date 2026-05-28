package com.example.food_store.controller.client;

import org.springframework.web.bind.annotation.*;

import com.example.food_store.controller.BaseController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/gemini-proxy")
public class GeminiController extends BaseController {

    // CHỖ SỬA 1: Xóa 'static' và sửa lại đường dẫn key khớp 100% với file
    // application.yml
    // Thêm dấu : và truyền URL dự phòng vào ngay phía sau
    @Value("${api.gemini.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=AIzaSyBtAty7hF9w18pHMG5k-nF83xTTD4Tm9wg}")
    private String apiUrl;

    @PostMapping
public ResponseEntity<String> proxyToGemini(@RequestBody String requestBody) {
    log.info("Request to /gemini-proxy");
    try {
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        // KIỂM TRA HTTP CODE TRƯỚC KHI ĐỌC INPUT STREAM
        int responseCode = conn.getResponseCode();
        if (responseCode == 429) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Hệ thống AI đang quá tải (Lỗi 429: Too Many Requests). Vui lòng thử lại sau 1 phút.");
        } else if (responseCode != 200) {
            return ResponseEntity.status(responseCode)
                    .body("Google Gemini trả về lỗi hệ thống. Mã lỗi: " + responseCode);
        }
        
        StringBuilder responseStr = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                responseStr.append(line);
            }
        }
        
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                .body(extractTextFromResponse(responseStr.toString()));

    } catch (Exception e) {
        log.error("Lỗi kết nối Gemini API: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Không thể kết nối tới Google AI: " + e.getMessage());
    }
}

    private String extractTextFromResponse(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            // Trích xuất phần "text" từ cấu trúc JSON mặc định của Gemini API
            return rootNode.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            return "Lỗi xử lý JSON: " + e.getMessage();
        }
    }
}