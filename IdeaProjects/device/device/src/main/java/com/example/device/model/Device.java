package com.example.device.model;

import com.example.device.enums.DeviceCategory;
import com.example.device.enums.DeviceState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private DeviceCategory category;

    @Column(name = "serial_number", nullable = false, unique = true, length = 20)
    private String serialNumber;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private DeviceState state;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_time")
    private LocalDateTime                                                                                                       updatedTime;
}