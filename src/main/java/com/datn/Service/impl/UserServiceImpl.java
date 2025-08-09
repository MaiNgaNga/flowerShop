package com.datn.Service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datn.Service.UserService;
import com.datn.dao.UserDAO;
import com.datn.model.User;
import com.datn.utils.AuthService;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserDAO dao;
    @Autowired
    AuthService authService;

    @Override
    public List<User> findAll() {
        return dao.findAll();
    }

    @Override
    public User findByID(int id) {
        Optional<User> User = dao.findById(id);
        return User.orElse(null);
    }

    @Override
    public User create(User entity) {

        System.out.println("Mật khẩu sau khi mã hóa: " + entity.getPassword());

        return dao.save(entity);
    }

    @Override
    public void update(User entity) {

        if (!dao.existsById(entity.getId())) {
            throw new IllegalArgumentException("Chưa chọn User để cập nhật!");
        }
        User existingUser = dao.findByEmail(entity.getEmail());
        if (existingUser != null && existingUser.getId() != (entity.getId())) {
            throw new IllegalArgumentException("Email này đã tồn tại!");
        }

        dao.save(entity);
        System.out.println("User update: " + entity);
    }

    @Override
    public void deleteById(int id) {

        if (dao.existsById(id)) {
            // if(!orderDAO.findByUser_Id(id).isEmpty()){
            // throw new IllegalArgumentException("Người dùng này đã mua sản phẩm!");

            // }
            dao.deleteById(id);
        } else {
            throw new IllegalArgumentException("Không xác định được User cần xóa");
        }
    }

    @Override
    public boolean existsById(int id) {
        return dao.existsById(id);
    }

    @Override
    public User findByEmail(String email) {
        User User = dao.findByEmail(email);
        return User != null ? User : null;
    }

    @Override
    public boolean updatePassword(String newPassword) {
        User user = authService.getUser();
        if (user != null) {
            user.setPassword(newPassword);
            dao.save(user);
            return true;
        }
        return false;
    }

    @Override
    public List<User> findAllNonShipper() {
        return dao.findAllNonShippers();
    }

    @Override
    public User findBySdt(String sdt) {
         return dao.findBySdt(sdt);

    }

    @Override
    public Optional<User> findByRememberToken(String token) {
        return dao.findByRememberToken(token);
    }

}