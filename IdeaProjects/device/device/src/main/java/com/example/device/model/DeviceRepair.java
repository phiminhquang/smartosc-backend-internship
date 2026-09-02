package com.example.device.model;

import com.example.device.enums.RepairStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "device_repairs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceRepair {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "issue_description", nullable = false, length = 1000)
    private String issueDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RepairStatus status;

    @Column(name = "created_by", nullable = false, length = 255)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_by", length = 255)
    private String startedBy;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_by", length = 255)
    private String finishedBy;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "repair_note", length = 1000)
    private String repairNote;

    @Column(name = "cost", precision = 15, scale = 2)
    private BigDecimal cost;
}