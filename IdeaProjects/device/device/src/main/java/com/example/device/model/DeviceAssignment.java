package com.example.device.model;

import com.example.device.enums.DeviceAssignmentStatus;
import com.example.device.enums.DeviceReturnCondition;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "device_assignments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceAssignment {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "assigned_by", nullable = false)
    private String assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "expected_return_at", nullable = false)
    private LocalDateTime expectedReturnAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeviceAssignmentStatus status;

    @Column(name = "overdue_notified_at")
    private LocalDateTime overdueNotifiedAt;

    @Column(name = "reminder_notified_at")
    private LocalDateTime reminderNotifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_condition")
    private DeviceReturnCondition returnCondition;

    @Column(name = "return_note", length = 1000)
    private String returnNote;

}