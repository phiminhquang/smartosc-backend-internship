package com.example.ex.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Roles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
// các thuôc j tính này để mapping từ spring sang db nó khớp vs nhau nma quan trong vẫn là ở db th
    // colum là để mapping từ java sang db nếu ko trùng tên chắc chắn cần, ko có thì p trùng
    //update là cho phép Hibernate sửa db qua entity còn mik đang dùng validate thì db  quản lý entity chỉ có nhiệm vụ mapping giữa Hibernate và db
    @Column(name = "name", length = 50, unique = true, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
