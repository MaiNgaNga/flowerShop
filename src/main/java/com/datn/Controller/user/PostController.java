package com.datn.Controller.user;

import com.datn.Service.PostService;
import com.datn.model.Post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
public class PostController {

    @Autowired
    private PostService postService;

    @RequestMapping("/PostUser")
    public String postPage(Model model,
            @RequestParam(name = "p", defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, 12);
        Page<Post> posts = postService.findAllPageable(pageable); // nếu bạn vẫn muốn phân trang

        // Thêm danh sách 12 bài viết mới nhất (không phân trang)
        List<Post> newestPosts = postService.findTop12Newest();

        model.addAttribute("page", posts);
        model.addAttribute("newestPosts", newestPosts); // truyền vào HTML nếu cần
        model.addAttribute("view", "post");

        return "layouts/layout";
    }

    @RequestMapping("/searchPost")
    public String searchPost(Model model,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "p", defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, 12);
        Page<Post> posts;

        if (keyword != null && !keyword.trim().isEmpty()) {
            posts = postService.findByTitleContaining(keyword, pageable);
            model.addAttribute("searchKeyword", keyword);
        } else {
            posts = Page.empty();
        }

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

        // lấy 4 bài viết khác
        List<Post> relatedPosts = postService.findRelatedPosts(id);
        model.addAttribute("relatedPosts", relatedPosts);

        model.addAttribute("view", "post-detail");
        return "layouts/layout";
    }

}
