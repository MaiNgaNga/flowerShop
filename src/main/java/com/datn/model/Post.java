package com.datn.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@Table(name = "posts")
@AllArgsConstructor
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "NVARCHAR(255)", nullable = false)
    @NotBlank(message = "Tiêu đề bài viết không được để trống")
    @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự")
    private String title;

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    @NotBlank(message = "Nội dung bài viết không được để trống")
    private String content;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String image;

    @Column(nullable = false)
    private boolean status;

    @DateTimeFormat(pattern = "yyyy-MM-dd")

    private LocalDate postDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    // ⚠ Chú ý: user_id phải ánh xạ đúng với kiểu int trong User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    public void prePersist() {
        if (this.postDate == null) {
            this.postDate = LocalDate.now();
        }
        this.lastUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }
}
