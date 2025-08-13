package com.datn.validation;

import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.datn.model.Product;

@Component
public class DiscountValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Product.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Product product = (Product) target;

        // Kiểm tra logic giảm giá tùy chỉnh
        validateDiscountLogic(product, errors);
    }

    private void validateDiscountLogic(Product product, Errors errors) {
        try {
            // Kiểm tra nếu có bất kỳ trường giảm giá nào được điền
            Integer discountPercent = product.getDiscountPercent();
            LocalDate discountStart = product.getDiscountStart();
            LocalDate discountEnd = product.getDiscountEnd();

            boolean hasDiscountPercent = discountPercent != null && discountPercent > 0;
            boolean hasDiscountStart = discountStart != null;
            boolean hasDiscountEnd = discountEnd != null;

            // Nếu có một trong các trường giảm giá thì phải có đầy đủ
            if (hasDiscountPercent || hasDiscountStart || hasDiscountEnd) {
                if (!hasDiscountPercent) {
                    errors.rejectValue("discountPercent", "discount.percent.required",
                            "Phần trăm giảm giá là bắt buộc khi áp dụng giảm giá");
                }
                if (!hasDiscountStart) {
                    errors.rejectValue("discountStart", "discount.start.required",
                            "Ngày bắt đầu là bắt buộc khi áp dụng giảm giá");
                }
                if (!hasDiscountEnd) {
                    errors.rejectValue("discountEnd", "discount.end.required",
                            "Ngày kết thúc là bắt buộc khi áp dụng giảm giá");
                }

                // Kiểm tra logic ngày tháng nếu có đầy đủ thông tin
                if (hasDiscountStart && hasDiscountEnd) {
                    if (discountEnd.isBefore(discountStart) || discountEnd.isEqual(discountStart)) {
                        errors.rejectValue("discountEnd", "discount.end.invalid",
                                "Ngày kết thúc phải sau ngày bắt đầu");
                    }
                }

                if (hasDiscountStart) {
                    if (discountStart.isBefore(LocalDate.now())) {
                        errors.rejectValue("discountStart", "discount.start.invalid",
                                "Ngày bắt đầu giảm giá không được trong quá khứ");
                    }
                }

                if (hasDiscountPercent) {
                    if (discountPercent < 1 || discountPercent > 100) {
                        errors.rejectValue("discountPercent", "discount.percent.invalid",
                                "Phần trăm giảm giá phải từ 1% đến 100%");
                    }
                }
            }
        } catch (Exception e) {
            // Log lỗi nếu cần thiết
            System.err.println("Lỗi trong quá trình validation giảm giá: " + e.getMessage());
            errors.rejectValue("discountPercent", "discount.validation.error",
                    "Có lỗi xảy ra trong quá trình kiểm tra thông tin giảm giá");
        }
    }
}