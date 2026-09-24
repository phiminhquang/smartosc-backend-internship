CREATE TABLE roles (
    id BINARY(16) NOT NULL,
    description VARCHAR(255) NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UKofx66keruapi6vyqpv6f2or37 (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE users (
    id BINARY(16) NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    system_owner BIT(1) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UK6dotkott2kjsp8vw4d0m25fb7 (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE devices (
    id VARCHAR(36) NOT NULL,
    category ENUM('LAPTOP','MONITOR','PHONE') NOT NULL,
    description VARCHAR(255) NULL,
    model VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    serial_number VARCHAR(20) NOT NULL,
    state ENUM('ASSIGNED','AVAILABLE','UNDER_REPAIR') NOT NULL,
    updated_by VARCHAR(100) NULL,
    updated_time DATETIME(6) NULL,
    version BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UK6ju48hv6y1f2kn982hyxd0wep (serial_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_roles (
    user_id BINARY(16) NOT NULL,
    role_id BINARY(16) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    KEY FKh8ciramu9cc9q3qcqiv4ue8a6 (role_id),
    CONSTRAINT FKh8ciramu9cc9q3qcqiv4ue8a6 FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT FKhfh9dx7w3ubf1co1vdev94g3f FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE device_assignments (
    id BINARY(16) NOT NULL,
    assigned_at DATETIME(6) NOT NULL,
    assigned_by VARCHAR(255) NOT NULL,
    expected_return_at DATETIME(6) NOT NULL,
    overdue_notified_at DATETIME(6) NULL,
    reminder_notified_at DATETIME(6) NULL,
    return_condition ENUM('DAMAGED','GOOD') NULL,
    return_note VARCHAR(1000) NULL,
    returned_at DATETIME(6) NULL,
    status ENUM('ACTIVE','OVERDUE','RETURNED') NOT NULL,
    device_id VARCHAR(36) NOT NULL,
    user_id BINARY(16) NOT NULL,
    PRIMARY KEY (id),
    KEY FKt90oda68w2myhlkgby5kgle89 (device_id),
    KEY FKjyips7rul62wuraa06oas0q2w (user_id),
    CONSTRAINT FKjyips7rul62wuraa06oas0q2w FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT FKt90oda68w2myhlkgby5kgle89 FOREIGN KEY (device_id) REFERENCES devices (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE assignment_extensions (
    id BINARY(16) NOT NULL,
    previous_return_at DATETIME(6) NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    requested_at DATETIME(6) NOT NULL,
    requested_by VARCHAR(255) NOT NULL,
    requested_return_at DATETIME(6) NOT NULL,
    review_note VARCHAR(1000) NULL,
    reviewed_at DATETIME(6) NULL,
    reviewed_by VARCHAR(255) NULL,
    status ENUM('APPROVED','PENDING','REJECTED') NOT NULL,
    assignment_id BINARY(16) NOT NULL,
    PRIMARY KEY (id),
    KEY FKjoiuaksb85o7y3n4jd3p1h5fe (assignment_id),
    CONSTRAINT FKjoiuaksb85o7y3n4jd3p1h5fe FOREIGN KEY (assignment_id) REFERENCES device_assignments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE device_repairs (
    id VARCHAR(36) NOT NULL,
    cost DECIMAL(15,2) NULL,
    created_at DATETIME(6) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    finished_at DATETIME(6) NULL,
    finished_by VARCHAR(255) NULL,
    issue_description VARCHAR(1000) NOT NULL,
    repair_note VARCHAR(1000) NULL,
    started_at DATETIME(6) NULL,
    started_by VARCHAR(255) NULL,
    status ENUM('COMPLETED','IN_PROGRESS','PENDING','UNREPAIRABLE') NOT NULL,
    device_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (id),
    KEY FK73ylsjm05bobtpott0k375dk3 (device_id),
    CONSTRAINT FK73ylsjm05bobtpott0k375dk3 FOREIGN KEY (device_id) REFERENCES devices (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
