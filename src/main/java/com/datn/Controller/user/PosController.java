package com.datn.Controller.user;

import com.datn.Service.PosService;
import com.datn.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.datn.Service.CategoryService;
import com.datn.Service.ProductCategoryService;
import com.datn.Service.ColorService;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.datn.Service.ProductService;
import org.springframework.web.bind.annotation.RequestMapping;
import com.datn.dto.CartItemDTO;
import com.datn.model.Order;
import com.datn.model.OrderDetail;
import com.datn.dao.OrderDAO;

@Controller
@RequestMapping("/pos")
public class PosController {
    @Autowired
    private PosService posService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProductCategoryService productCategoryService;
    @Autowired
    private ColorService colorService;
    @Autowired
    private ProductService productService;

    @Autowired
    private OrderDAO orderDAO;

    @GetMapping("")
    public String showPosPage(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        int pageSize = 9;
        Pageable pageable = PageRequest.of(page, pageSize);
        double min = (minPrice != null) ? minPrice : 0;
        double max = (maxPrice != null) ? maxPrice : Double.MAX_VALUE;
        Page<Product> productPage = posService.filterProducts(color, type, keyword, min, max, pageable);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("color", color);
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("minPrice", min);
        model.addAttribute("maxPrice", max);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("colors", colorService.findAll());
        model.addAttribute("view", "pos");
        return "layouts/layout";
    }

    @PostMapping("/cart/add")
    @ResponseBody
    public List<CartItemDTO> addToCart(@RequestParam Long productId, HttpSession session) {
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("cart");
        if (cart == null)
            cart = new ArrayList<>();
        Optional<CartItemDTO> existing = cart.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst();
        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + 1);
        } else {
            Product product = productService.findByID(productId);
            CartItemDTO item = new CartItemDTO();
            item.setProductId(productId);
            item.setName(product.getName());
            item.setPrice(product.getPrice());
            item.setQuantity(1);
            cart.add(item);
        }
        session.setAttribute("cart", cart);
        return cart;
    }

    @GetMapping("/cart")
    @ResponseBody
    public List<CartItemDTO> getCart(HttpSession session) {
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("cart");
        if (cart == null)
            cart = new ArrayList<>();
        return cart;
    }

    @PostMapping("/checkout")
    public String checkout(HttpSession session) {
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            return "redirect:/pos?error=empty";
        }
        Order order = new Order();
        // Lấy user từ session
        com.datn.model.User user = (com.datn.model.User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        order.setUser(user);
        order.setCreateDate(new java.util.Date());
        order.setStatus("Chờ xác nhận");
        order.setOrderType("offline");
        // order.setSdt(...); // nếu có
        // order.setAddress(...); // nếu có
        List<OrderDetail> details = new ArrayList<>();
        double total = 0;
        for (CartItemDTO dto : cart) {
            Product product = productService.findByID(dto.getProductId());
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setQuantity(dto.getQuantity());
            detail.setPrice(dto.getPrice());
            details.add(detail);
            total += dto.getPrice() * dto.getQuantity();
        }
        order.setOrderDetails(details);
        order.setTotalAmount(total);
        orderDAO.save(order);
        session.removeAttribute("cart");
        return "redirect:/order/success";
    }

    @PostMapping("/cart/remove")
    @ResponseBody
    public List<CartItemDTO> removeFromCart(@RequestParam Long productId, HttpSession session) {
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("cart");
        if (cart == null)
            cart = new ArrayList<>();
        cart.removeIf(item -> item.getProductId().equals(productId));
        session.setAttribute("cart", cart);
        return cart;
    }
}
