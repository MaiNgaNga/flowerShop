package com.datn.Controller.user;

import com.datn.Service.PostService;

import com.datn.Service.PostCommentService;
import com.datn.Service.ProductCategoryService;

import com.datn.model.Post;
import com.datn.model.PostComment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import com.datn.utils.AuthService;
import com.datn.model.User;

@Controller
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private ProductCategoryService pro_ca_service;

    @Autowired
    private PostCommentService postCommentService; // ✅ Thêm service bình luận

    @Autowired
    private AuthService authService;
    @Autowired
    private com.datn.Service.CartItemService cartItemService;

    @RequestMapping("/PostUser")
    public String postPage(Model model,
            @RequestParam(name = "p", defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 6, Sort.by(Sort.Direction.DESC, "postDate"));

        List<Post> newestPosts = postService.findTop12Newest();
        Page<Post> posts = postService.findAllPageable(pageable);

        int cartCount = 0;
        User user = authService.getUser();
        if (user != null) {
            Integer userId = user.getId();
            cartCount = cartItemService.getCartItemsByUserId(userId).size();
        }
        model.addAttribute("cartCount", cartCount);

        model.addAttribute("productCategories", pro_ca_service.findAll());
        model.addAttribute("page", posts);
        model.addAttribute("newestPosts", newestPosts);
        model.addAttribute("view", "post");

        return "layouts/layout";
    }

    @RequestMapping("/searchPost")
    public String searchPost(Model model,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "p", defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, 6);
        Page<Post> posts;

        if (keyword != null && !keyword.trim().isEmpty()) {
            posts = postService.findByTitleContaining(keyword, pageable);
            model.addAttribute("searchKeyword", keyword);
        } else {
            posts = Page.empty();
        }

        model.addAttribute("productCategories", pro_ca_service.findAll());
        model.addAttribute("page", posts);
        model.addAttribute("view", "post");

        return "layouts/layout";
    }

    @RequestMapping("/postDetail")
    public String detail(Model model, @RequestParam("id") Long id) {
        Post post = postService.findById(id);
        if (post == null) {
            return "redirect:/PostUser";
        }

        model.addAttribute("post", post);

        List<Post> relatedPosts = postService.findRelatedPosts(id);
        model.addAttribute("relatedPosts", relatedPosts);

        // ✅ Truyền danh sách bình luận theo bài viết
        List<PostComment> comments = postCommentService.getCommentsByPostId(id);
        model.addAttribute("postComments", comments);

        model.addAttribute("productCategories", pro_ca_service.findAll());

        model.addAttribute("view", "post-detail");
        return "layouts/layout";
    }
}
