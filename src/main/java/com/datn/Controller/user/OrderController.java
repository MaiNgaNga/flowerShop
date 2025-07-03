package com.datn.Controller.user;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.datn.Service.OrderService;

import jakarta.validation.Valid;
import com.datn.Service.CartItemService;
import com.datn.Service.ProductCategoryService;
import com.datn.Service.ProductService;
import com.datn.model.CartItem;
import com.datn.model.Order;
import com.datn.model.OrderDetail;
import com.datn.model.OrderRequest;
import com.datn.model.User;
import com.datn.utils.AuthService;
import org.springframework.web.bind.annotation.PostMapping;




@Controller
@RequestMapping("/order")

public class OrderController {

    @Autowired
    ProductCategoryService pro_ca_service;
    @Autowired
    ProductService productService;
    @Autowired
    AuthService authService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private CartItemService cartItemService;

    @GetMapping("/index")
        public String index(Model model ) {
        OrderRequest orderRequest=new OrderRequest();
        model.addAttribute("orderRequest", orderRequest);
        return showForm(model);
    }

   @PostMapping("/checkout")
    public String checkout(@Valid @ModelAttribute("orderRequest") OrderRequest orderRequest, BindingResult result, Model model) {
    if (result.hasErrors()) {
        return showForm(model);
    }
    User user= authService.getUser();
    List<CartItem> cartItems = cartItemService.getCartItemsByUserId(user.getId());
    if (cartItems.isEmpty()) {
        model.addAttribute("message", "Giỏ hàng trống, không thể đặt hàng.");
        return showForm(model);
    }

    Order order = new Order();
    order.setUser(user);
    order.setCreateDate(new Date());
    order.setSdt(orderRequest.getSdt());
    order.setAddress(orderRequest.getAddress());
    order.setStatus("Chưa xác nhận");
    order.setTotalAmount(cartItemService.getTotalAmount(user.getId()));
    List<OrderDetail> orderDetails = new ArrayList<>();

    Order savedOrder = orderService.saveOrder(order, orderDetails);

    for (CartItem cartItem : cartItems) {
        OrderDetail detail = new OrderDetail();
        detail.setOrder(savedOrder);
        detail.setProduct(cartItem.getProduct());
        detail.setQuantity(cartItem.getQuantity());
        detail.setPrice(cartItem.getProduct().getPrice());
        orderDetails.add(detail);
    }

    orderService.saveOrder(savedOrder, orderDetails);

    cartItemService.clearCartByUserId(user.getId());
    model.addAttribute("success", "Đặt hàng thành công");
    return showForm(model);
}

    public String showForm(Model model ) {
        User user= authService.getUser();
        model.addAttribute("user",user);

        List<CartItem>  cartItems= cartItemService.getCartItemsByUserId(user.getId());
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount",cartItemService.getTotalAmount(user.getId()));
        model.addAttribute("productCategories", pro_ca_service.findAll());
        model.addAttribute("view", "order");
        return "layouts/layout";
    }
    
}