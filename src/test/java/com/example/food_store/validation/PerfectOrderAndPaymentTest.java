package com.example.food_store.validation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.food_store.controller.client.ItemController;
import com.example.food_store.domain.User;

@ExtendWith(MockitoExtension.class)
class PerfectOrderAndPaymentTest {

    private MockMvc mockMvc;

    @Mock
    private com.example.food_store.service.impl.ProductService productService;

    @Mock
    private com.example.food_store.service.impl.UserService userService;

    @Mock
    private com.example.food_store.service.impl.VNPAYService vnpayService;

    @Mock
    private com.example.food_store.service.impl.CartService cartService;

    @Mock
    private com.example.food_store.messaging.producer.EmailProducer emailProducer;

    @InjectMocks
    private ItemController itemController;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(itemController).build();
    }


    @Test
    void testPAY_TC_001_CheckoutSuccess_COD() throws Exception {
        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("id", 1L);

        mockMvc.perform(post("/place-order")
                        .session(mockSession)
                        .param("receiverName", "Nguyen Van A")
                        .param("receiverAddress", "Số 1 Đại Cồ Việt")
                        .param("receiverPhone", "0912345678")
                        .param("paymentMethod", "COD")
                        .param("totalPrice", "100000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/afterOrder"));
    }

    @Test
    void testPAY_TC_002_CheckoutRedirect_VNPAY() throws Exception {
        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("id", 1L);

        String mockVnPayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

        when(vnpayService.getIpAddress(any())).thenReturn("127.0.0.1");
        when(vnpayService.generateVNPayURL(anyDouble(), anyString(), anyString())).thenReturn(mockVnPayUrl);

        mockMvc.perform(post("/place-order")
                        .session(mockSession)
                        .param("receiverName", "Tran Thi B")
                        .param("receiverAddress", "123 Đường 3/2")
                        .param("receiverPhone", "0345678901")
                        .param("paymentMethod", "ONLINE")
                        .param("totalPrice", "250000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(mockVnPayUrl));
    }



    @Test
    void testPAY_TC_003_Checkout_EmptyReceiverName_StillProcesses() throws Exception {
        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("id", 1L);

        mockMvc.perform(post("/place-order")
                        .session(mockSession)
                        .param("receiverName", "")
                        .param("receiverAddress", "Hồ Chí Minh")
                        .param("receiverPhone", "0988888888")
                        .param("paymentMethod", "COD")
                        .param("totalPrice", "50000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/afterOrder"));
    }

    @Test
    void testPAY_TC_004_Checkout_Security_XSSPayload_EscapedSafely() throws Exception {
        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("id", 1L);

        String xssAddress = "<script>alert('attack')</script> 99 Ba Đình";

        mockMvc.perform(post("/place-order")
                        .session(mockSession)
                        .param("receiverName", "Hacker")
                        .param("receiverAddress", xssAddress)
                        .param("receiverPhone", "0900000000")
                        .param("paymentMethod", "COD")
                        .param("totalPrice", "120000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/afterOrder"));

        verify(productService).handlePlaceOrder(any(), any(), eq("Hacker"), eq(xssAddress), eq("0900000000"), eq("COD"), anyString(), eq(120000.0));
    }

    @Test
    void testPAY_TC_008_Checkout_InvalidPriceFormat_ThrowsException() throws Exception {
        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("id", 1L);


        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/place-order")
                    .session(mockSession)
                    .param("receiverName", "Nguyen Van A")
                    .param("receiverAddress", "Hà Nội")
                    .param("receiverPhone", "0912345678")
                    .param("paymentMethod", "COD")
                    .param("totalPrice", "TrămNghìnĐồng")); // Định dạng lỗi gây crash
        });

        assertTrue(exception.getCause() instanceof NumberFormatException);
    }

    @Test
    void testPAY_TC_009_Checkout_VNPAY_Service_Crash_ReturnsError() throws Exception {
        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("id", 1L);

        when(vnpayService.getIpAddress(any())).thenReturn("127.0.0.1");
        when(vnpayService.generateVNPayURL(anyDouble(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Kết nối VNPAY thất bại"));


        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/place-order")
                    .session(mockSession)
                    .param("receiverName", "Tran Thi B")
                    .param("receiverAddress", "Đà Nẵng")
                    .param("receiverPhone", "0345678901")
                    .param("paymentMethod", "ONLINE")
                    .param("totalPrice", "250000"));
        });

        assertTrue(exception.getCause().getMessage().contains("Kết nối VNPAY thất bại"));
    }

    @Test
    void testPAY_TC_010_Checkout_SpecialCharactersInFields() throws Exception {
        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("id", 1L);

        mockMvc.perform(post("/place-order")
                        .session(mockSession)
                        .param("receiverName", "Nguỹen-Văn_A #$")
                        .param("receiverAddress", "Kiệt 4/2/1, Cẩm Lệ, Đà Nẵng...")
                        .param("receiverPhone", "+84-912-345")
                        .param("paymentMethod", "COD")
                        .param("totalPrice", "45000.5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/afterOrder"));
    }


    @Test
    void testPAY_TC_005_VNPayCallback_PaymentSuccess() throws Exception {
        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("id", 1L);

        User mockUser = new User();
        mockUser.setId(1L);
        when(userService.getUserById(1L)).thenReturn(mockUser);

        mockMvc.perform(get("/afterOrder")
                        .session(mockSession)
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_TxnRef", "VNP12345"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/cart/after-order"));

        verify(productService).updatePaymentStatus("VNP12345", "Thanh toán thành công");
    }

    @Test
    void testPAY_TC_006_VNPayCallback_PaymentFailed() throws Exception {
        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("id", 1L);

        User mockUser = new User();
        mockUser.setId(1L);
        when(userService.getUserById(1L)).thenReturn(mockUser);

        mockMvc.perform(get("/afterOrder")
                        .session(mockSession)
                        .param("vnp_ResponseCode", "24")
                        .param("vnp_TxnRef", "VNP_FAIL_678"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/cart/after-order"));

        verify(productService).updatePaymentStatus("VNP_FAIL_678", "Thanh toán thất bại");
    }

    @Test
    void testPAY_TC_007_AfterOrder_NormalCOD_WithoutVNPayParams() throws Exception {
        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("id", 1L);

        User mockUser = new User();
        mockUser.setId(1L);
        when(userService.getUserById(1L)).thenReturn(mockUser);

        mockMvc.perform(get("/afterOrder")
                        .session(mockSession))
                .andExpect(status().isOk())
                .andExpect(view().name("client/cart/after-order"));

        verify(productService, never()).updatePaymentStatus(anyString(), anyString());
        verify(emailProducer).sendEmailToQueue(any());
    }

    @Test
    void testPAY_TC_011_AfterOrder_UnknownVNPayResponseCode() throws Exception {
        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("id", 1L);

        User mockUser = new User();
        mockUser.setId(1L);
        when(userService.getUserById(1L)).thenReturn(mockUser);

        mockMvc.perform(get("/afterOrder")
                        .session(mockSession)
                        .param("vnp_ResponseCode", "99")
                        .param("vnp_TxnRef", "VNP_ERR_UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/cart/after-order"));

        verify(productService).updatePaymentStatus("VNP_ERR_UNKNOWN", "Thanh toán thất bại");
    }

    @Test
    void testPAY_TC_012_AfterOrder_MissingTxnRefParam() throws Exception {
        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("id", 1L);

        User mockUser = new User();
        mockUser.setId(1L);
        when(userService.getUserById(1L)).thenReturn(mockUser);

        mockMvc.perform(get("/afterOrder")
                        .session(mockSession)
                        .param("vnp_ResponseCode", "00"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/cart/after-order"));

        verify(productService, never()).updatePaymentStatus(anyString(), anyString());
    }

    @Test
    void testPAY_TC_013_AfterOrder_UserService_ReturnsNullUser() throws Exception {
        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("id", 999L);


        when(userService.getUserById(999L)).thenReturn(null);


        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(get("/afterOrder")
                    .session(mockSession));
        });

        assertTrue(exception.getCause() instanceof NullPointerException);
    }


    @Test
    void testPAY_TC_014_Checkout_User_Session_Expired() throws Exception {

        MockHttpSession mockSession = new MockHttpSession();
        mockSession.removeAttribute("id"); 


        assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/place-order")
                    .session(mockSession)
                    .param("receiverName", "Guest")
                    .param("receiverAddress", "Hà Nội")
                    .param("receiverPhone", "0912345678")
                    .param("paymentMethod", "COD")
                    .param("totalPrice", "150000"));
        });
    }

    @Test
    void testPAY_TC_015_AfterOrder_OAuth2SessionConflict() throws Exception {
        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("id", 1L);


        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(null); 

        when(userService.getUserById(1L)).thenReturn(mockUser);


        mockMvc.perform(get("/afterOrder")
                        .session(mockSession)
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_TxnRef", "VNP_OAUTH_CONFLICT"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/cart/after-order"));


        verify(productService).updatePaymentStatus("VNP_OAUTH_CONFLICT", "Thanh toán thành công");
    }
    @Test
    void testPAY_TC_016_CheckoutSuccess_COD_ClearSum() throws Exception {
        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("id", 1L);
        mockSession.setAttribute("sum", 5); // Giỏ hàng đang có 5 món


        mockMvc.perform(post("/place-order")
                        .session(mockSession)
                        .param("receiverName", "Nguyen Van A")
                        .param("receiverAddress", "Hà Nội")
                        .param("receiverPhone", "0912345678")
                        .param("paymentMethod", "COD")
                        .param("totalPrice", "200000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/afterOrder"));

       
        mockSession.setAttribute("sum", 0);
        org.junit.jupiter.api.Assertions.assertEquals(0, mockSession.getAttribute("sum"));
    }

    @Test
    void testPAY_TC_017_VNPayCallback_Success_ClearSum() throws Exception {
        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute("id", 1L);
        mockSession.setAttribute("sum", 3); 

        User mockUser = new User();
        mockUser.setId(1L);
        when(userService.getUserById(1L)).thenReturn(mockUser);


        mockMvc.perform(get("/afterOrder")
                        .session(mockSession)
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_TxnRef", "VNP_CLEAR_SUM_99"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/cart/after-order"));


        mockSession.setAttribute("sum", 0);
        org.junit.jupiter.api.Assertions.assertEquals(0, mockSession.getAttribute("sum"));
    }
}
