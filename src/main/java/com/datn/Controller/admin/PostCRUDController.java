package com.datn.Controller.admin;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.datn.Service.PostService;
import com.datn.Service.UserService;
import com.datn.model.Post;
import com.datn.model.User;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/Post")
public class PostCRUDController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @ModelAttribute("posts")
    public List<Post> getAllPosts() {
        return postService.findAll();
    }

    @RequestMapping("/index")
    public String index(Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            postPage = postService.findByTitleContaining(keyword.trim(), pageable);
        } else {
            postPage = postService.findAllPageable(pageable);
        }

        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("totalElements", postPage.getTotalElements());
        model.addAttribute("hasPrevious", postPage.hasPrevious());
        model.addAttribute("hasNext", postPage.hasNext());
        model.addAttribute("keyword", keyword);
        model.addAttribute("post", new Post());
        model.addAttribute("view", "admin/PostCRUD");

        return "admin/layout";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword) {
        Post post = new Post();
        post.setPostDate(LocalDate.now());
        post.setStatus(true);

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = keyword != null && !keyword.trim().isEmpty()
                ? postService.findByTitleContaining(keyword.trim(), pageable)
                : postService.findAllPageable(pageable);

        model.addAttribute("post", post);
        model.addAttribute("users", userService.findAll());
        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("totalElements", postPage.getTotalElements());
        model.addAttribute("hasPrevious", postPage.hasPrevious());
        model.addAttribute("hasNext", postPage.hasNext());
        model.addAttribute("keyword", keyword);
        model.addAttribute("view", "admin/PostCRUD");
        return "admin/layout";
    }

    @PostMapping("/create")
    public String create(Model model,
            @Valid @ModelAttribute("post") Post post, Errors errors,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = keyword != null && !keyword.trim().isEmpty()
                ? postService.findByTitleContaining(keyword.trim(), pageable)
                : postService.findAllPageable(pageable);

        if (errors.hasErrors()) {
            model.addAttribute("users", userService.findAll());
            model.addAttribute("posts", postPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", postPage.getTotalPages());
            model.addAttribute("totalElements", postPage.getTotalElements());
            model.addAttribute("hasPrevious", postPage.hasPrevious());
            model.addAttribute("hasNext", postPage.hasNext());
            model.addAttribute("keyword", keyword);
            model.addAttribute("view", "admin/PostCRUD");
            return "admin/layout";
        }

        try {
            Integer userId = (Integer) session.getAttribute("userId");

            if (userId == null) {
                redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để tạo bài viết.");
                return "redirect:/login";
            }

            User currentUser = userService.findByID(userId);
            post.setUser(currentUser);
            post.setPostDate(LocalDate.now());
            postService.create(post, imageFile);

            redirectAttributes.addFlashAttribute("success", "Thêm bài viết thành công!");
            return "redirect:/Post/index";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("users", userService.findAll());
            model.addAttribute("posts", postPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", postPage.getTotalPages());
            model.addAttribute("totalElements", postPage.getTotalElements());
            model.addAttribute("hasPrevious", postPage.hasPrevious());
            model.addAttribute("hasNext", postPage.hasNext());
            model.addAttribute("keyword", keyword);
            model.addAttribute("view", "admin/PostCRUD");
            return "admin/layout";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable("id") long id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword) {

        Post post = postService.findById(id);

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = keyword != null && !keyword.trim().isEmpty()
                ? postService.findByTitleContaining(keyword.trim(), pageable)
                : postService.findAllPageable(pageable);

        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("totalElements", postPage.getTotalElements());
        model.addAttribute("hasPrevious", postPage.hasPrevious());
        model.addAttribute("hasNext", postPage.hasNext());
        model.addAttribute("keyword", keyword);
        model.addAttribute("post", post);
        model.addAttribute("users", userService.findAll());
        model.addAttribute("view", "admin/PostCRUD");
        return "admin/layout";
    }

    @PostMapping("/update")
    public String update(Model model,
            @Valid @ModelAttribute("post") Post post, Errors errors,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "oldImage", required = false) String oldImage,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            RedirectAttributes redirectAttributes) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = keyword != null && !keyword.trim().isEmpty()
                ? postService.findByTitleContaining(keyword.trim(), pageable)
                : postService.findAllPageable(pageable);

        if (errors.hasErrors()) {
            model.addAttribute("users", userService.findAll());
            model.addAttribute("posts", postPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", postPage.getTotalPages());
            model.addAttribute("totalElements", postPage.getTotalElements());
            model.addAttribute("hasPrevious", postPage.hasPrevious());
            model.addAttribute("hasNext", postPage.hasNext());
            model.addAttribute("keyword", keyword);
            model.addAttribute("view", "admin/PostCRUD");
            return "admin/layout";
        }

        try {
            Post oldPost = postService.findById(post.getId());

            if (oldPost == null) {
                redirectAttributes.addFlashAttribute("error", "Bài viết không tồn tại.");
                return "redirect:/Post/index";
            }

            post.setUser(oldPost.getUser());
            postService.update(post, imageFile, oldImage);

            redirectAttributes.addFlashAttribute("success", "Cập nhật bài viết thành công!");
            return "redirect:/Post/edit/" + post.getId() + "?page=" + page + "&size=" + size
                    + (keyword != null ? "&keyword=" + keyword : "");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("users", userService.findAll());
            model.addAttribute("posts", postPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", postPage.getTotalPages());
            model.addAttribute("totalElements", postPage.getTotalElements());
            model.addAttribute("hasPrevious", postPage.hasPrevious());
            model.addAttribute("hasNext", postPage.hasNext());
            model.addAttribute("keyword", keyword);
            model.addAttribute("view", "admin/PostCRUD");
            return "admin/layout";
        }
    }

    @RequestMapping("/delete/{id}")
    public String delete(Model model,
            @ModelAttribute("post") Post post, Errors errors,
            @PathVariable("id") long id,
            RedirectAttributes redirectAttributes) {

        try {
            postService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xoá bài viết!");
            return "redirect:/Post/index";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/Post/edit/" + post.getId();
        }
    }
}