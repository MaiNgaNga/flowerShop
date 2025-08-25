package com.datn.Service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.datn.Service.PostService;
import com.datn.dao.PostDAO;
import com.datn.model.Post;
import com.datn.utils.ParamService;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostDAO dao;

    @Autowired
    private ParamService param;

    @Override
    public List<Post> findAll() {
        return dao.findAll();
    }

    @Override
    public Post findById(Long id) {
        Optional<Post> post = dao.findById(id);
        return post.orElse(null);
    }

    @Override
    public Post create(Post entity, MultipartFile imageFile) {
        if (entity.getId() != null && dao.existsById(entity.getId())) {
            throw new IllegalArgumentException("ID bài viết đã tồn tại!");
        }

        if (!imageFile.isEmpty()) {
            entity.setImage(
                    param.save(imageFile, "D:\\Graduation Project\\Git2\\src\\main\\resources\\static\\images")
                            .getName());
        }

        return dao.save(entity);
    }   

    @Override
    public Post update(Post entity, MultipartFile imageFile, String oldImage) {
        if (dao.existsById(entity.getId())) {
            if (!imageFile.isEmpty()) {
                entity.setImage(
                        param.save(imageFile, "D:\\Graduation Project\\Git2\\src\\main\\resources\\static\\images")
                                .getName());
            } else {
                entity.setImage(oldImage);
            }
            return dao.save(entity);
        } else {
            throw new IllegalArgumentException("Không tìm thấy bài viết để cập nhật!");
        }
    }

    @Override
    public void deleteById(Long id) {
        if (dao.existsById(id)) {
            dao.deleteById(id);
        } else {
            throw new IllegalArgumentException("Không tìm thấy bài viết để xóa!");
        }
    }

    @Override
    public boolean existsById(Long id) {
        return dao.existsById(id);
    }

    @Override
    public Page<Post> findAllPageable(Pageable pageable) {
        return dao.findAll(pageable);
    }

    @Override
    public Page<Post> findByTitleContaining(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return dao.findAll(pageable);
        }
        return dao.searchByTitle(keyword.trim(), pageable);
    }

    @Override
    public List<Post> findRelatedPosts(Long excludePostId) {
        Pageable pageable = Pageable.ofSize(3); // Giới hạn 5 bài viết liên quan
        return dao.findTopRelatedPosts(excludePostId, pageable);
    }

    @Override
    public List<Post> findTop12Newest() {
        return dao.findTop9ByStatusTrueOrderByPostDateDesc();
    }
}
