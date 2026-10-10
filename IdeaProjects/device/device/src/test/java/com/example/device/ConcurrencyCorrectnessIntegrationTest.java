package com.example.device;

import com.example.device.enums.DeviceAssignmentStatus;
import com.example.device.enums.DeviceCategory;
import com.example.device.enums.DeviceReturnCondition;
import com.example.device.enums.DeviceState;
import com.example.device.enums.ExtensionRequestStatus;
import com.example.device.enums.RepairStatus;
import com.example.device.model.AssignmentExtension;
import com.example.device.model.Device;
import com.example.device.model.DeviceAssignment;
import com.example.device.model.DeviceRepair;
import com.example.device.model.User;
import com.example.device.repository.AssignmentExtensionRepository;
import com.example.device.repository.DeviceAssignmentRepository;
import com.example.device.repository.DeviceRepairRepository;
import com.example.device.repository.DeviceRepository;
import com.example.device.repository.UserRepository;
import com.example.device.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ConcurrencyCorrectnessIntegrationTest {

    private static final String ISOLATED_JDBC_URL = "jdbc:tc:mysql:8.4:///device_concurrency_test";
    private static final String TEST_ADMIN_PASSWORD = UUID.randomUUID().toString();
    private static final String TEST_JWT_KEY = Base64.getEncoder()
            .encodeToString(SecureRandom.getSeed(64));
    private static final String ACTOR_EMAIL = "concurrency.admin@device.test";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private DeviceAssignmentRepository assignmentRepository;
    @Autowired
    private AssignmentExtensionRepository extensionRepository;
    @Autowired
    private DeviceRepairRepository repairRepository;
    @MockitoBean
    private EmailService emailService;

    @DynamicPropertySource
    static void testProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> ISOLATED_JDBC_URL);
        registry.add("spring.datasource.driver-class-name", () -> "org.testcontainers.jdbc.ContainerDatabaseDriver");
        registry.add("spring.datasource.username", () -> "");
        registry.add("spring.datasource.password", () -> "");
        registry.add("app.admin.password", () -> TEST_ADMIN_PASSWORD);
        registry.add("jwt.signer-key", () -> TEST_JWT_KEY);
    }

    @BeforeEach
    void ensureActorExists() {
        saveActorIfMissing();
    }

    @Test
    void concurrentAssignCommitsExactlyOneActiveAssignment() throws Exception {
        User employee = saveUser("assign.employee");
        Device device = saveDevice(DeviceState.AVAILABLE, "ASSIGN");
        String body = """
                {"userId":"%s","deviceId":"%s","expectedReturnAt":"%s"}
                """.formatted(employee.getId(), device.getId(), LocalDateTime.now().plusDays(7));

        List<MvcResult> results = runConcurrently(
                () -> mockMvc.perform(post("/api/assignments")
                                .with(actor())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andReturn(),
                () -> mockMvc.perform(post("/api/assignments")
                                .with(actor())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andReturn()
        );

        assertOneSuccessOneConflict(results, 1022);
        long activeAssignments = assignmentRepository.findAll().stream()
                .filter(assignment -> assignment.getDevice().getId().equals(device.getId()))
                .filter(assignment -> assignment.getStatus() == DeviceAssignmentStatus.ACTIVE)
                .count();
        assertEquals(1, activeAssignments);
        assertEquals(DeviceState.ASSIGNED, deviceRepository.findById(device.getId()).orElseThrow().getState());
    }

    @Test
    void concurrentReturnCommitsTheTransitionOnce() throws Exception {
        User employee = saveUser("return.employee");
        Device device = saveDevice(DeviceState.ASSIGNED, "RETURN");
        DeviceAssignment assignment = saveAssignment(employee, device);
        String body = "{\"condition\":\"%s\"}".formatted(DeviceReturnCondition.GOOD);

        List<MvcResult> results = runConcurrently(
                () -> mockMvc.perform(patch("/api/assignments/{assignmentId}/return", assignment.getId())
                                .with(actor())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andReturn(),
                () -> mockMvc.perform(patch("/api/assignments/{assignmentId}/return", assignment.getId())
                                .with(actor())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andReturn()
        );

        assertOneSuccessOneConflict(results, 1029);
        DeviceAssignment stored = assignmentRepository.findByIdWithDetails(assignment.getId()).orElseThrow();
        assertEquals(DeviceAssignmentStatus.RETURNED, stored.getStatus());
        assertNotNull(stored.getReturnedAt());
        assertEquals(DeviceState.AVAILABLE, stored.getDevice().getState());
    }

    @Test
    void concurrentApproveAndRejectCommitOnlyOneExtensionDecision() throws Exception {
        User employee = saveUser("extension.employee");
        Device device = saveDevice(DeviceState.ASSIGNED, "EXTENSION");
        DeviceAssignment assignment = saveAssignment(employee, device);
        LocalDateTime previousReturnAt = assignment.getExpectedReturnAt();
        LocalDateTime requestedReturnAt = previousReturnAt.plusDays(5);
        AssignmentExtension extension = extensionRepository.saveAndFlush(AssignmentExtension.builder()
                .assignment(assignment)
                .previousReturnAt(previousReturnAt)
                .requestedReturnAt(requestedReturnAt)
                .reason("Concurrency test")
                .status(ExtensionRequestStatus.PENDING)
                .requestedBy(employee.getEmail())
                .requestedAt(LocalDateTime.now())
                .build());

        List<MvcResult> results = runConcurrently(
                () -> mockMvc.perform(patch("/api/extension-requests/{requestId}/approve", extension.getId())
                                .with(actor()))
                        .andReturn(),
                () -> mockMvc.perform(patch("/api/extension-requests/{requestId}/reject", extension.getId())
                                .with(actor()))
                        .andReturn()
        );

        assertOneSuccessOneConflict(results, 1035);
        AssignmentExtension stored = extensionRepository.findByIdWithDetails(extension.getId()).orElseThrow();
        assertTrue(stored.getStatus() == ExtensionRequestStatus.APPROVED
                || stored.getStatus() == ExtensionRequestStatus.REJECTED);
        LocalDateTime expectedDeadline = stored.getStatus() == ExtensionRequestStatus.APPROVED
                ? requestedReturnAt
                : previousReturnAt;
        assertEquals(expectedDeadline, stored.getAssignment().getExpectedReturnAt());
    }

    @Test
    void concurrentRepairCompletionCommitsOnlyOneFinalState() throws Exception {
        Device device = saveDevice(DeviceState.UNDER_REPAIR, "REPAIR");
        DeviceRepair repair = repairRepository.saveAndFlush(DeviceRepair.builder()
                .device(device)
                .issueDescription("Concurrency test")
                .status(RepairStatus.IN_PROGRESS)
                .createdBy(ACTOR_EMAIL)
                .createdAt(LocalDateTime.now().minusHours(1))
                .startedBy(ACTOR_EMAIL)
                .startedAt(LocalDateTime.now().minusMinutes(30))
                .build());
        String completeBody = "{\"repairNote\":\"Completed\",\"cost\":25.00}";
        String unrepairableBody = "{\"repairNote\":\"Cannot repair\"}";

        List<MvcResult> results = runConcurrently(
                () -> mockMvc.perform(patch("/api/repairs/{repairId}/complete", repair.getId())
                                .with(actor())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(completeBody))
                        .andReturn(),
                () -> mockMvc.perform(patch("/api/repairs/{repairId}/unrepairable", repair.getId())
                                .with(actor())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(unrepairableBody))
                        .andReturn()
        );

        assertOneSuccessOneConflict(results, 1046);
        DeviceRepair stored = repairRepository.findByIdWithDevice(repair.getId()).orElseThrow();
        assertTrue(stored.getStatus() == RepairStatus.COMPLETED
                || stored.getStatus() == RepairStatus.UNREPAIRABLE);
        DeviceState expectedDeviceState = stored.getStatus() == RepairStatus.COMPLETED
                ? DeviceState.AVAILABLE
                : DeviceState.UNDER_REPAIR;
        assertEquals(expectedDeviceState, stored.getDevice().getState());
        assertNotNull(stored.getFinishedAt());
    }

    private SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor actor() {
        return user(ACTOR_EMAIL).roles("ADMIN", "IT_STAFF");
    }

    private void saveActorIfMissing() {
        if (userRepository.findByEmail(ACTOR_EMAIL).isEmpty()) {
            userRepository.saveAndFlush(User.builder()
                    .email(ACTOR_EMAIL)
                    .name("Concurrency Admin")
                    .password("not-used")
                    .build());
        }
    }

    private User saveUser(String prefix) {
        return userRepository.saveAndFlush(User.builder()
                .email(prefix + "." + UUID.randomUUID() + "@device.test")
                .name("Concurrency Employee")
                .password("not-used")
                .build());
    }

    private Device saveDevice(DeviceState state, String prefix) {
        return deviceRepository.saveAndFlush(Device.builder()
                .category(DeviceCategory.LAPTOP)
                .serialNumber(prefix + "-" + UUID.randomUUID().toString()
                        .substring(0, 19 - prefix.length()))
                .name(prefix + " Laptop")
                .model("Concurrency")
                .state(state)
                .build());
    }

    private DeviceAssignment saveAssignment(User user, Device device) {
        return assignmentRepository.saveAndFlush(DeviceAssignment.builder()
                .user(user)
                .device(device)
                .assignedBy(ACTOR_EMAIL)
                .assignedAt(LocalDateTime.now().minusDays(1))
                .expectedReturnAt(LocalDateTime.now().plusDays(7).truncatedTo(ChronoUnit.MICROS))
                .status(DeviceAssignmentStatus.ACTIVE)
                .build());
    }

    private List<MvcResult> runConcurrently(
            Callable<MvcResult> first,
            Callable<MvcResult> second
    ) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<MvcResult>> futures = List.of(
                    executor.submit(awaitStart(first, ready, start)),
                    executor.submit(awaitStart(second, ready, start))
            );
            assertTrue(ready.await(10, TimeUnit.SECONDS), "Concurrent requests did not become ready");
            start.countDown();

            List<MvcResult> results = new ArrayList<>();
            for (Future<MvcResult> future : futures) {
                results.add(future.get(30, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private Callable<MvcResult> awaitStart(
            Callable<MvcResult> request,
            CountDownLatch ready,
            CountDownLatch start
    ) {
        return () -> {
            ready.countDown();
            assertTrue(start.await(10, TimeUnit.SECONDS), "Concurrent start signal timed out");
            return request.call();
        };
    }

    private void assertOneSuccessOneConflict(List<MvcResult> results, int expectedConflictCode)
            throws Exception {
        List<Integer> statuses = results.stream()
                .map(result -> result.getResponse().getStatus())
                .sorted(Comparator.naturalOrder())
                .toList();
        assertEquals(List.of(200, 409), statuses);

        MvcResult conflict = results.stream()
                .filter(result -> result.getResponse().getStatus() == 409)
                .findFirst()
                .orElseThrow();
        assertTrue(conflict.getResponse().getContentAsString()
                .contains("\"code\":" + expectedConflictCode));
    }
}
