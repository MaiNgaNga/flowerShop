package com.datn.Controller.user;

import com.datn.Service.PostService;
import com.datn.Service.PostCommentService;
import com.datn.model.Post;
import com.datn.model.PostComment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private PostCommentService postCommentService; // ✅ Thêm service bình luận

    @RequestMapping("/PostUser")
    public String postPage(Model model,
            @RequestParam(name = "p", defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, 12);
        Page<Post> posts = postService.findAllPageable(pageable);
        List<Post> newestPosts = postService.findTop12Newest();

        model.addAttribute("page", posts);
        model.addAttribute("newestPosts", newestPosts);
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

        List<Post> relatedPosts = postService.findRelatedPosts(id);
        model.addAttribute("relatedPosts", relatedPosts);

        // ✅ Truyền danh sách bình luận theo bài viết
        List<PostComment> comments = postCommentService.getCommentsByPostId(id);
        model.addAttribute("postComments", comments);

        model.addAttribute("view", "post-detail");
        return "layouts/layout";
    }
}
