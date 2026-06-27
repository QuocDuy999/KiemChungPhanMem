package com.example.food_store.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.food_store.controller.BaseController;
import com.example.food_store.domain.Order;
import com.example.food_store.domain.OrderDetail;
import com.example.food_store.service.impl.OrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class OrderController extends BaseController {
    // Đường dẫn chuyển hướng đến trang quản lý đơn hàng
    private static final String REDIRECT_ORDER = "redirect:/admin/order";
    private final OrderService orderService;

    @GetMapping("/admin/order")
    public String getDashboard(Model model, @RequestParam("page") Optional<String> pageOptional) {
        log.info("Request to /admin/order");
        int page = 1;
        try {
            if (pageOptional.isPresent()) {
                // convert from String to int
                page = Integer.parseInt(pageOptional.get());
            } else {
                // page = 1
            }
            Pageable pageable = PageRequest.of(page - 1, 4);
            Page<Order> ordersPage = this.orderService.fetchAllOrders(pageable);
            List<Order> orders = ordersPage.getContent();
            model.addAttribute("orders", orders);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", ordersPage.getTotalPages());
            return "admin/order/show";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Không tìm thấy trang .");
            return "not-match";
        }

    }

    @GetMapping("/admin/order/{id}")
    public String getMethodName(@PathVariable long id, Model model) {
        log.info("Request to /admin/order/{}", id);

        // Lấy thông tin đơn hàng theo ID
        Optional<Order> optionalOrder = this.orderService.fetchOrderById(id);

        // Kiểm tra đơn hàng có tồn tại hay không trước khi lấy dữ liệu
        if (optionalOrder.isEmpty()) {
            // Nếu không tìm thấy đơn hàng thì quay về trang danh sách
            return REDIRECT_ORDER;
        }

        // Lấy đối tượng Order sau khi đã xác nhận tồn tại
        Order order = optionalOrder.get();

        model.addAttribute("id", id);
        model.addAttribute("order", order);
        model.addAttribute("orderDetails", order.getOrderDetails());

        return "admin/order/detail";
    }

    @GetMapping("/admin/order/delete/{id}")
    public String getDeleteOrderPage(Model model, @PathVariable long id) {
        log.info("Request to /admin/order/delete/{id}");
        model.addAttribute("id", id);
        model.addAttribute("newOrder", new Order());
        return "admin/order/delete";
    }

    @GetMapping("/admin/order/update/{id}")
    public String getUpdateOrderPage(Model model, @PathVariable long id) {
        log.info("Request to /admin/order/update/{id}");
        Optional<Order> currentOrder = this.orderService.fetchOrderById(id);
        model.addAttribute("newOrder", currentOrder.get());
        return "admin/order/update";
    }

    @PostMapping("/admin/order/delete")
    public String postDeleteOrder(@ModelAttribute("newOrder") Order order) {
        log.info("Request to /admin/order/delete");
        this.orderService.deleteById(order.getId());
        return REDIRECT_ORDER;
    }

    @PostMapping("/admin/order/update")
    public String handleUpdateOrder(@ModelAttribute("newOrder") Order order) {
        log.info("Request to /admin/order/update");
        this.orderService.updateOrder(order);
        return REDIRECT_ORDER;
    }

}
