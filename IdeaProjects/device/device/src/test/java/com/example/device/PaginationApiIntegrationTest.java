package com.example.device;

import com.example.device.enums.DeviceAssignmentStatus;
import com.example.device.enums.DeviceCategory;
import com.example.device.enums.DeviceState;
import com.example.device.enums.ExtensionRequestStatus;
import com.example.device.enums.RepairStatus;
import com.example.device.exception.AppException;
import com.example.device.exception.ErrorCode;
import com.example.device.model.AssignmentExtension;
import com.example.device.model.Device;
import com.example.device.model.DeviceAssignment;
import com.example.device.model.DeviceRepair;
import com.example.device.model.Role;
import com.example.device.model.User;
import com.example.device.repository.AssignmentExtensionRepository;
import com.example.device.repository.DeviceAssignmentRepository;
import com.example.device.repository.DeviceRepairRepository;
import com.example.device.repository.DeviceRepository;
import com.example.device.repository.RoleRepository;
import com.example.device.repository.UserRepository;
import com.example.device.service.EmailService;
import com.example.device.service.DeviceFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PaginationApiIntegrationTest {

    private static final String ISOLATED_JDBC_URL = "jdbc:tc:mysql:8.4:///device_pagination_test";
    private static final String TEST_ADMIN_PASSWORD = UUID.randomUUID().toString();
    private static final String TEST_JWT_KEY = Base64.getEncoder()
            .encodeToString(SecureRandom.getSeed(64));
    private static final String EMPLOYEE_EMAIL = "pager.employee@device.test";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private DeviceAssignmentRepository assignmentRepository;
    @Autowired
    private DeviceRepairRepository repairRepository;
    @Autowired
    private AssignmentExtensionRepository extensionRepository;
    @Autowired
    private DeviceFileService deviceFileService;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @MockitoBean
    private EmailService emailService;

    private User employee;
    private Device device;

    @DynamicPropertySource
    static void testProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> ISOLATED_JDBC_URL);
        registry.add("spring.datasource.driver-class-name", () -> "org.testcontainers.jdbc.ContainerDatabaseDriver");
        registry.add("spring.datasource.username", () -> "");
        registry.add("spring.datasource.password", () -> "");
        registry.add("app.admin.password", () -> TEST_ADMIN_PASSWORD);
        registry.add("jwt.signer-key", () -> TEST_JWT_KEY);
        registry.add("app.device-file.export-batch-size", () -> 2);
        registry.add("app.device-file.import-batch-size", () -> 2);
    }

    @BeforeEach
    void seedPageData() {
        Role employeeRole = roleRepository.findByName("EMPLOYEE").orElseThrow();

        User alpha = user("Pager Alpha", "pager.alpha@device.test", employeeRole);
        User middle = user("Pager Middle", EMPLOYEE_EMAIL, employeeRole);
        User zulu = user("Pager Zulu", "pager.zulu@device.test", employeeRole);
        List<User> users = userRepository.saveAllAndFlush(List.of(zulu, middle, alpha));
        employee = users.stream()
                .filter(user -> EMPLOYEE_EMAIL.equals(user.getEmail()))
                .findFirst()
                .orElseThrow();

        device = deviceRepository.saveAndFlush(Device.builder()
                .category(DeviceCategory.LAPTOP)
                .serialNumber("PAGER-0001")
                .name("Pager Laptop")
                .model("P1")
                .state(DeviceState.ASSIGNED)
                .build());

        LocalDateTime now = LocalDateTime.of(2026, 10, 9, 8, 0);
        DeviceAssignment earlier = assignmentRepository.saveAndFlush(assignment(
                DeviceAssignmentStatus.ACTIVE, now.minusDays(2), now.plusDays(2)));
        DeviceAssignment later = assignmentRepository.saveAndFlush(assignment(
                DeviceAssignmentStatus.ACTIVE, now.minusDays(1), now.plusDays(4)));
        assignmentRepository.saveAndFlush(assignment(
                DeviceAssignmentStatus.RETURNED, now.minusDays(5), now.minusDays(1)));

        repairRepository.saveAllAndFlush(List.of(
                repair(RepairStatus.PENDING, now.minusHours(2), null),
                repair(RepairStatus.COMPLETED, now.minusDays(3), new BigDecimal("125.50"))
        ));

        extensionRepository.saveAllAndFlush(List.of(
                extension(earlier, ExtensionRequestStatus.PENDING, now.minusHours(2), now.plusDays(5)),
                extension(later, ExtensionRequestStatus.PENDING, now.minusHours(1), now.plusDays(7)),
                extension(later, ExtensionRequestStatus.APPROVED, now.minusDays(2), now.plusDays(6))
        ));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, roles = {"ADMIN", "IT_STAFF", "EMPLOYEE"})
    void usersSupportFilterSortAndPageBoundariesWithoutDuplicatingRoleRows() throws Exception {
        mockMvc.perform(get("/api/users")
                        .param("keyword", "  PAGER  ")
                        .param("role", "EMPLOYEE")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content.length()").value(2))
                .andExpect(jsonPath("$.result.content[0].name").value("Pager Alpha"))
                .andExpect(jsonPath("$.result.content[1].name").value("Pager Middle"))
                .andExpect(jsonPath("$.result.content[0].roles[0]").value("EMPLOYEE"))
                .andExpect(jsonPath("$.result.number").value(0))
                .andExpect(jsonPath("$.result.size").value(2))
                .andExpect(jsonPath("$.result.totalElements").value(3))
                .andExpect(jsonPath("$.result.totalPages").value(2))
                .andExpect(jsonPath("$.result.first").value(true))
                .andExpect(jsonPath("$.result.last").value(false))
                .andExpect(jsonPath("$.result.empty").value(false));

        mockMvc.perform(get("/api/users")
                        .param("keyword", "pager")
                        .param("role", "EMPLOYEE")
                        .param("page", "1")
                        .param("size", "2")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content.length()").value(1))
                .andExpect(jsonPath("$.result.content[0].name").value("Pager Zulu"))
                .andExpect(jsonPath("$.result.first").value(false))
                .andExpect(jsonPath("$.result.last").value(true));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, roles = {"ADMIN", "IT_STAFF", "EMPLOYEE"})
    void everyNewCollectionEndpointReturnsTheStablePageShape() throws Exception {
        mockMvc.perform(get("/api/assignments")
                        .param("status", "ACTIVE")
                        .param("userId", employee.getId().toString())
                        .param("deviceId", device.getId().toString())
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "expectedReturnAt,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content.length()").value(1))
                .andExpect(jsonPath("$.result.totalElements").value(2))
                .andExpect(jsonPath("$.result.totalPages").value(2));

        assertSinglePage("/api/assignments/me", 3);
        assertSinglePage("/api/assignments/user/" + employee.getId(), 3);
        assertSinglePage("/api/repairs?status=PENDING&deviceId=" + device.getId(), 1);
        assertSinglePage("/api/repairs/device/" + device.getId(), 2);
        assertSinglePage("/api/extension-requests/me?status=PENDING", 2);
        assertSinglePage("/api/extension-requests/pending", 2);
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, roles = {"ADMIN", "IT_STAFF", "EMPLOYEE"})
    void invalidPaginationFilterSortAndUuidReturnTheDocumentedBadRequest() throws Exception {
        assertInvalid("/api/users?page=-1");
        assertInvalid("/api/users?size=101");
        assertInvalid("/api/users?sort=password,asc");
        assertInvalid("/api/users?role=ROOT");
        assertInvalid("/api/users?keyword=" + "x".repeat(101));
        assertInvalid("/api/assignments?status=UNKNOWN");
        assertInvalid("/api/assignments?userId=not-a-uuid");
        assertInvalid("/api/devices?size=101");
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, roles = {"ADMIN", "IT_STAFF", "EMPLOYEE"})
    void deviceFileBatchQueriesAndImportRunAgainstMysql() {
        deviceRepository.saveAllAndFlush(List.of(
                Device.builder()
                        .category(DeviceCategory.MONITOR)
                        .serialNumber("PAGER-0002")
                        .name("Pager Monitor")
                        .model("P2")
                        .state(DeviceState.AVAILABLE)
                        .build(),
                Device.builder()
                        .category(DeviceCategory.PHONE)
                        .serialNumber("PAGER-0003")
                        .name("Pager Phone")
                        .model("P3")
                        .state(DeviceState.AVAILABLE)
                        .build()
        ));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        deviceFileService.exportCsv(output);
        String csv = output.toString(StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("\uFEFFid,category,serialNumber,name"));
        assertTrue(csv.contains("Pager Laptop"));
        assertTrue(csv.contains("Pager Monitor"));
        assertTrue(csv.contains("Pager Phone"));

        long countBeforeImport = deviceRepository.count();
        MockMultipartFile importFile = new MockMultipartFile(
                "file",
                "devices.csv",
                "text/csv",
                ("\uFEFFcategory,name,model,description\n"
                        + "LAPTOP,Imported Laptop,I1,MySQL batch row 1\n"
                        + "PHONE,Imported Phone,I2,MySQL batch row 2\n"
                        + "MONITOR,Imported Monitor,I3,MySQL batch row 3\n")
                        .getBytes(StandardCharsets.UTF_8)
        );

        assertEquals(3, deviceFileService.importCsv(importFile));
        assertEquals(countBeforeImport + 3, deviceRepository.count());
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, roles = {"ADMIN", "IT_STAFF", "EMPLOYEE"})
    void invalidRowAfterAFlushedImportBatchRollsBackEveryRow() {
        long countBeforeImport = deviceRepository.count();
        MockMultipartFile importFile = new MockMultipartFile(
                "file",
                "devices.csv",
                "text/csv",
                ("category,name,model,description\n"
                        + "LAPTOP,Rollback Laptop,R1,Valid row 1\n"
                        + "PHONE,Rollback Phone,R2,Valid row 2\n"
                        + "INVALID,Rollback Invalid,R3,Invalid row 3\n")
                        .getBytes(StandardCharsets.UTF_8)
        );
        TransactionTemplate isolatedImport = new TransactionTemplate(transactionManager);
        isolatedImport.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        AppException exception = assertThrows(
                AppException.class,
                () -> isolatedImport.executeWithoutResult(status -> deviceFileService.importCsv(importFile))
        );

        assertEquals(ErrorCode.INVALID_DEVICE_CATEGORY, exception.getErrorCode());
        assertEquals(countBeforeImport, deviceRepository.count());
    }

    private void assertSinglePage(String path, int totalElements) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content").isArray())
                .andExpect(jsonPath("$.result.number").value(0))
                .andExpect(jsonPath("$.result.size").value(20))
                .andExpect(jsonPath("$.result.totalElements").value(totalElements))
                .andExpect(jsonPath("$.result.totalPages").value(1))
                .andExpect(jsonPath("$.result.first").value(true))
                .andExpect(jsonPath("$.result.last").value(true))
                .andExpect(jsonPath("$.result.empty").value(false));
    }

    private void assertInvalid(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1055))
                .andExpect(jsonPath("$.message")
                        .value("Tham số phân trang, lọc hoặc sắp xếp không hợp lệ"));
    }

    private User user(String name, String email, Role role) {
        return User.builder()
                .name(name)
                .email(email)
                .password("not-used-in-pagination-test")
                .roles(new HashSet<>(List.of(role)))
                .build();
    }

    private DeviceAssignment assignment(
            DeviceAssignmentStatus status,
            LocalDateTime assignedAt,
            LocalDateTime expectedReturnAt
    ) {
        return DeviceAssignment.builder()
                .user(employee)
                .device(device)
                .assignedBy("pagination-test@device.test")
                .assignedAt(assignedAt)
                .expectedReturnAt(expectedReturnAt)
                .returnedAt(status == DeviceAssignmentStatus.RETURNED ? expectedReturnAt : null)
                .status(status)
                .build();
    }

    private DeviceRepair repair(RepairStatus status, LocalDateTime createdAt, BigDecimal cost) {
        return DeviceRepair.builder()
                .device(device)
                .issueDescription("Pagination integration test")
                .status(status)
                .createdBy("pagination-test@device.test")
                .createdAt(createdAt)
                .finishedAt(status == RepairStatus.COMPLETED ? createdAt.plusHours(2) : null)
                .cost(cost)
                .build();
    }

    private AssignmentExtension extension(
            DeviceAssignment assignment,
            ExtensionRequestStatus status,
            LocalDateTime requestedAt,
            LocalDateTime requestedReturnAt
    ) {
        return AssignmentExtension.builder()
                .assignment(assignment)
                .previousReturnAt(assignment.getExpectedReturnAt())
                .requestedReturnAt(requestedReturnAt)
                .reason("Pagination integration test")
                .status(status)
                .requestedBy(EMPLOYEE_EMAIL)
                .requestedAt(requestedAt)
                .build();
    }
}
