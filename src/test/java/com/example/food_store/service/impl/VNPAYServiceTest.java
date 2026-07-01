package com.example.food_store.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.servlet.http.HttpServletRequest;

class VNPAYServiceTest {

    private VNPAYService vnPayService;

    @BeforeEach
    void setUp() throws Exception {

        vnPayService = new VNPAYService();

        setField("vnp_TmnCode", "TESTCODE");
        setField("secretKey", "SECRETKEY");
        setField("vnp_ReturnUrl", "http://localhost/return");
        setField("vnp_PayUrl", "http://localhost/pay");
    }

    private void setField(String fieldName, String value) throws Exception {
        Field field = VNPAYService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(vnPayService, value);
    }

    // ===============================
    // generateVNPayURL
    // ===============================

    @Test
    void generateVNPayURL_ShouldReturnValidUrl()
            throws UnsupportedEncodingException {

        String url = vnPayService.generateVNPayURL(
                100000,
                "ORDER001",
                "127.0.0.1");

        assertNotNull(url);

        assertTrue(url.contains("http://localhost/pay?"));

        assertTrue(url.contains("vnp_TmnCode=TESTCODE"));

        assertTrue(url.contains("vnp_TxnRef=ORDER001"));

        assertTrue(url.contains("vnp_SecureHash="));
    }

    // ===============================
    // hmacSHA512
    // ===============================

    @Test
    void hmacSHA512_ShouldReturnHash() {

        String hash = vnPayService.hmacSHA512(
                "secret",
                "hello");

        assertNotNull(hash);

        assertFalse(hash.isEmpty());
    }

    @Test
    void hmacSHA512_KeyNull_ShouldReturnEmptyString() {

        String hash = vnPayService.hmacSHA512(
                null,
                "hello");

        assertEquals("", hash);
    }

    @Test
    void hmacSHA512_DataNull_ShouldReturnEmptyString() {

        String hash = vnPayService.hmacSHA512(
                "secret",
                null);

        assertEquals("", hash);
    }

    // ===============================
    // getIpAddress
    // ===============================

    @Test
    void getIpAddress_ShouldReturnHeaderIp() {

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getHeader("X-FORWARDED-FOR"))
                .thenReturn("192.168.1.1");

        String ip = vnPayService.getIpAddress(request);

        assertEquals("192.168.1.1", ip);
    }

    @Test
    void getIpAddress_ShouldReturnRemoteAddr() {

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getHeader("X-FORWARDED-FOR"))
                .thenReturn(null);

        when(request.getRemoteAddr())
                .thenReturn("10.10.10.10");

        String ip = vnPayService.getIpAddress(request);

        assertEquals("10.10.10.10", ip);
    }

    @Test
    void getIpAddress_WhenException_ShouldReturnInvalidIp() {

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getHeader(anyString()))
                .thenThrow(new RuntimeException("Network Error"));

        String ip = vnPayService.getIpAddress(request);

        assertTrue(ip.startsWith("Invalid IP:"));
    }
}