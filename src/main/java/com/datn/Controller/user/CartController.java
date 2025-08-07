package com.datn.Controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.datn.Service.CartItemService;
import com.datn.Service.ProductCategoryService;
import com.datn.model.CartItem;
import com.datn.model.User;
import com.datn.utils.AuthService;

import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {
    @Autowired
    ProductCategoryService pro_ca_Service;
    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private AuthService authService;

    /**
     * Trang hiển thị giỏ hàng của người dùng.
     * - Endpoint: GET /cart
     * - Nếu chưa đăng nhập, chuyển hướng về trang đăng nhập.
     * - Nếu đã đăng nhập:
     *   1. Lấy danh sách sản phẩm trong giỏ hàng của user hiện tại.
     *   2. Tính tổng số lượng sản phẩm và tổng tiền.
     *   3. Truyền dữ liệu sang view để hiển thị.
     * - Trả về: view layouts/layout với thông tin giỏ hàng.
     */
    @GetMapping
    public String viewCart(Model model) {
        User user = authService.getUser();
        if (user == null)
            return "redirect:/login";
        Integer userId = user.getId();
        int cartCount = cartItemService.getCartItemsByUserId(userId).size();
        model.addAttribute("cartCount", cartCount);
        List<CartItem> cartItems = cartItemService.getCartItemsByUserId(userId);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", cartItemService.getTotalAmount(userId));
        model.addAttribute("productCategories", pro_ca_Service.findAll());

        model.addAttribute("view", "cart");
        return "layouts/layout";
    }


    /**
     * API thêm sản phẩm vào giỏ hàng (dùng cho form truyền thống).
     * - Endpoint: POST /cart/add
     * - Tham số:
     *   + productId (Long): ID sản phẩm
     *   + quantity (int): số lượng
     * - Nếu chưa đăng nhập, chuyển hướng về trang đăng nhập.
     * - Nếu đã đăng nhập, thêm sản phẩm vào giỏ hàng và chuyển hướng về trang giỏ hàng.
     */
    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Long productId,
            @RequestParam("quantity") int quantity) {
        User user = authService.getUser();
        if (user == null)
            return "redirect:/login";
        Integer userId = user.getId();
        cartItemService.addCartItem(userId, productId, quantity);
        return "redirect:/cart";
    }



    /**
     * API thêm sản phẩm vào giỏ hàng bằng AJAX (trả về JSON).
     * - Endpoint: POST /cart/add-ajax
     * - Tham số:
     *   + productId (Long): ID sản phẩm
     *   + quantity (int): số lượng
     * - Nếu chưa đăng nhập, trả về JSON {redirect: "/login"}
     * - Nếu đã đăng nhập, thêm sản phẩm vào giỏ hàng và trả về JSON:
     *   { success: true, message: "Đã thêm sản phẩm vào giỏ hàng!", cartCount: số lượng }
     */
    @PostMapping("/add-ajax")
    @ResponseBody
    public Object addToCartAjax(@RequestParam("productId") Long productId,
                                @RequestParam("quantity") int quantity) {
        User user = authService.getUser();
        if (user == null) {
            return java.util.Collections.singletonMap("redirect", "/login");
        }
        Integer userId = user.getId();
        cartItemService.addCartItem(userId, productId, quantity);
        int cartCount = cartItemService.getCartItemsByUserId(userId).size();
        return java.util.Map.of(
            "success", true,
            "message", "Đã thêm sản phẩm vào giỏ hàng!",
            "cartCount", cartCount
        );
    }
    

    /**
     * API cập nhật số lượng sản phẩm trong giỏ hàng.
     * - Endpoint: POST /cart/update
     * - Tham số:
     *   + id (Integer): ID của CartItem
     *   + quantity (int): số lượng mới
     * - Thực hiện cập nhật số lượng sản phẩm, sau đó chuyển hướng về trang giỏ hàng.
     */
    @PostMapping("/update")
    public String UpdateToCart(@RequestParam("id") Integer cartItemId,
            @RequestParam("quantity") int quantity) {
        cartItemService.updateCartItemQuantity(cartItemId, quantity);
        return "redirect:/cart";
    }


    /**
     * API xóa một sản phẩm khỏi giỏ hàng.
     * - Endpoint: GET /cart/remove
     * - Tham số: cartItemId (Integer) - ID của CartItem cần xóa
     * - Nếu user hợp lệ, xóa sản phẩm khỏi giỏ hàng và chuyển hướng về trang giỏ hàng.
     */
    @GetMapping("/remove")
    public String removeCartItem(@RequestParam("cartItemId") Integer cartItemId) {
        Integer userId = authService.getUser().getId();

        if (userId != null) {
            cartItemService.removeCartItem(cartItemId);
        }
        return "redirect:/cart";
    }

    /**
     * API xóa toàn bộ sản phẩm trong giỏ hàng của user hiện tại.
     * - Endpoint: GET /cart/clear
     * - Nếu user hợp lệ, xóa toàn bộ giỏ hàng và chuyển hướng về trang giỏ hàng.
     */
    @GetMapping("/clear")
    public String clearCart() {
        Integer userId = authService.getUser().getId();

        if (userId != null) {
            cartItemService.clearCartByUserId(userId);
        }
        return "redirect:/cart";
    }
}
