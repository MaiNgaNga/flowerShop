package com.datn.Service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.datn.model.Post;

public interface PostService {
    List<Post> findAll();

    Post findById(Long id);

    Post create(Post post, MultipartFile imageFile);

    Post update(Post post, MultipartFile imageFile, String oldImage);

    void deleteById(Long id);

    boolean existsById(Long id);

    Page<Post> findAllPageable(Pageable pageable);

    Page<Post> findByTitleContaining(String keyword, Pageable pageable);

    List<Post> findRelatedPosts(Long excludePostId);

    List<Post> findTop12Newest();
    

}
