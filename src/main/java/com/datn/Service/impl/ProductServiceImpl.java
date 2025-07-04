package com.datn.Service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.datn.Service.ProductService;
import com.datn.dao.ProductDAO;
import com.datn.model.Product;
import com.datn.utils.ParamService;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDAO dao;
    @Autowired
    ParamService param;

    @Override
    public List<Product> findAll() {
        return dao.findAll();
    }

    @Override
    public Product findByID(long id) {
        Optional<Product> product = dao.findById(id);
        return product.orElse(null);
    }

    @Override
    public Product create(Product entity, MultipartFile image1, MultipartFile image2, MultipartFile image3) {
        if (dao.existsById(entity.getId())) {
            throw new IllegalArgumentException("ID sản phẩm này đã tồn tại!");
        }

        if (image1 != null && !image1.isEmpty()) {
            entity.setImage_url(param.save(image1,
                    "E:\\duantotnghiep\\datn\\src\\main\\resources\\static\\images").getName());
        }
        if (image2 != null && !image2.isEmpty()) {
            entity.setImage_url2(param.save(image2,
                    "E:\\duantotnghiep\\datn\\src\\main\\resources\\static\\images").getName());
        }
        if (image3 != null && !image3.isEmpty()) {
            entity.setImage_url3(param.save(image3,
                    "E:\\duantotnghiep\\datn\\src\\main\\resources\\static\\images").getName());
        }
        return dao.save(entity);
    }

    @Override
    public void update(Product entity, MultipartFile image1, MultipartFile image2, MultipartFile image3,
            String[] oldImages) {
        if (dao.existsById(entity.getId())) {
            if (image1 != null && !image1.isEmpty()) {
                entity.setImage_url(param.save(image1,
                        "E:\\duantotnghiep\\datn\\src\\main\\resources\\static\\images").getName());
            } else {
                entity.setImage_url(oldImages.length > 0 ? oldImages[0] : null);
            }

            // Ảnh 2
            if (image2 != null && !image2.isEmpty()) {
                entity.setImage_url2(param.save(image2,
                        "E:\\duantotnghiep\\datn\\src\\main\\resources\\static\\images").getName());
            } else {
                entity.setImage_url2(oldImages.length > 1 ? oldImages[1] : null);
            }

            // Ảnh 3
            if (image3 != null && !image3.isEmpty()) {
                entity.setImage_url3(param.save(image3,
                        "E:\\duantotnghiep\\datn\\src\\main\\resources\\static\\images").getName());
            } else {
                entity.setImage_url3(oldImages.length > 2 ? oldImages[2] : null);
            }
            dao.save(entity);
        } else {
            throw new IllegalArgumentException("Chưa chọn sản phẩm để cập nhật!");
        }
    }

    @Override
    public void deleteById(long id) {
        if (dao.existsById(id)) {
            dao.deleteById(id);
        } else {
            throw new IllegalArgumentException("Không xác định được sản phẩm cần xóa!");
        }
    }

    @Override
    public boolean existsById(long id) {
        return dao.existsById(id);
    }

    @Override
    public List<Product> findBestSellingProductPerCategory() {
        return dao.findBestSellingProductPerCategory();
    }

    @Override
    public List<Product> findByProductOrderByQuantityDesc() {
        return dao.findByProductOrderByQuantityDesc();
    }

    @Override
    public List<Product> findLatestProductsPerCategory() {
        return dao.findLatestProductsPerCategory();
    }

    @Override
    public List<Product> findProductByNameProductCategory(String name) {
        return dao.findProductByNameProductCategory(name);
    }

    @Override
    public List<Product> findByCategory_Id(int id) {
        return dao.findByCategory_Id(id);
    }

    @Override
    public List<Product> findByProductCategoryId(int id) {
        return dao.findByProductCategoryId(id);
    }

    @Override
    public Page<Product> findByPriceRange(Integer productCategoryId, Double minPrice, Double maxPrice,
            Pageable pageable) {
        return dao.findByPriceRange(productCategoryId, minPrice, maxPrice, pageable);
    }

    @Override
    public Page<Product> findByColor(Integer productCategoryId, String color,
            Pageable pageable) {
        return dao.findByColor(productCategoryId, color, pageable);
    }

    @Override
    public Page<Product> findByCaId(Integer productCategoryId, int categoryId,
            Pageable pageable) {
        return dao.findByCaId(productCategoryId, categoryId, pageable);
    }

    @Override
    public Page<Product> findByProductCategoryIdPage(Integer productCategoryId,
            Pageable pageable) {
        return dao.findByProductCategoryId(productCategoryId, pageable);
    }

    @Override
    public Page<Product> findByAllProduct(Pageable pageable) {
        return dao.findAll(pageable);
    }

    @Override
    public Page<Product> searchByName(String name, Pageable pageable) {
        return dao.searchByName(name, pageable);
    }

    @Override
    public List<Product> findTop6ByOrderByQuantityDesc() {
        return dao.findTop6ByOrderByQuantityDesc();
    }

    // TODO: Implement các phương thức cho Best Seller theo loại sản phẩm
    // (Cần thêm vào ProductService interface và ProductDAO trước)
    /*
     * @Override
     * public List<Product> findBestSellerLangHoa() {
     * return dao.findBestSellerLangHoa();
     * }
     * 
     * @Override
     * public List<Product> findBestSellerGioHoa() {
     * return dao.findBestSellerGioHoa();
     * }
     * 
     * @Override
     * public List<Product> findBestSellerBoHoa() {
     * return dao.findBestSellerBoHoa();
     * }
     * 
     * @Override
     * public List<Product> findBestSellerByCategory(Integer categoryId) {
     * return dao.findBestSellerByCategory(categoryId);
     * }
     */

    @Override
    public List<Product> findBestSellerByCategory(String categoryName) {
        return dao.findBestSellerByCategory(categoryName);
    }

    @Override
    public List<Product> findProductByCategory(Integer categoryId) {
        // TODO Auto-generated method stub
        return dao.findProductByCategory(categoryId);
    }

    @Override
    public Page<Product> findByProductCategoryName(String name, Pageable pageable) {
        return dao.findByProductCategoryName(name, pageable);
    }

}
