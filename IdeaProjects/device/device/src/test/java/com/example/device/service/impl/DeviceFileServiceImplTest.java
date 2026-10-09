package com.example.device.service.impl;

import com.example.device.configuration.DeviceFileProperties;
import com.example.device.dto.request.DeviceCreationRequest;
import com.example.device.enums.DeviceCategory;
import com.example.device.enums.DeviceState;
import com.example.device.exception.AppException;
import com.example.device.exception.ErrorCode;
import com.example.device.model.Device;
import com.example.device.repository.DeviceRepository;
import com.example.device.service.DeviceService;
import jakarta.persistence.EntityManager;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceFileServiceImplTest {

    @BeforeAll
    static void enableHeadlessPoi() {
        System.setProperty("java.awt.headless", "true");
    }

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceService deviceService;

    @Mock
    private EntityManager entityManager;

    private DeviceFileProperties properties;
    private DeviceFileServiceImpl service;

    @BeforeEach
    void setUp() {
        properties = new DeviceFileProperties();
        properties.setExportBatchSize(2);
        properties.setImportBatchSize(2);
        properties.setExcelRowWindow(2);
        properties.setMaxImportBytes(1024);
        service = new DeviceFileServiceImpl(deviceRepository, deviceService, entityManager, properties);
    }

    @Test
    void exportCsv_readsOrderedKeysetBatchesAndWritesUtf8Bom() {
        Device first = device("00000000-0000-0000-0000-000000000001", "Laptop 1");
        Device second = device("00000000-0000-0000-0000-000000000002", "Laptop 2");
        Device third = device("00000000-0000-0000-0000-000000000003", "Laptop 3");
        when(deviceRepository.findAllByOrderByIdAsc(any(Pageable.class)))
                .thenReturn(List.of(first, second));
        when(deviceRepository.findByIdGreaterThanOrderByIdAsc(
                eq(second.getId()), any(Pageable.class)))
                .thenReturn(List.of(third));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        service.exportCsv(output);

        String csv = output.toString(StandardCharsets.UTF_8);
        assertTrue(csv.startsWith("\uFEFFid,category,serialNumber,name"));
        assertTrue(csv.contains("Laptop 1"));
        assertTrue(csv.contains("Laptop 3"));
        verify(deviceRepository).findAllByOrderByIdAsc(any(Pageable.class));
        verify(deviceRepository).findByIdGreaterThanOrderByIdAsc(
                eq(second.getId()), any(Pageable.class));
        verify(entityManager, times(2)).clear();
    }

    @Test
    void exportExcel_writesAReadableStreamingWorkbook() throws Exception {
        when(deviceRepository.findAllByOrderByIdAsc(any(Pageable.class)))
                .thenReturn(List.of(device("00000000-0000-0000-0000-000000000001", "Laptop 1")));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        service.exportExcel(output);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(output.toByteArray()))) {
            assertEquals("Devices", workbook.getSheetAt(0).getSheetName());
            assertEquals("id", workbook.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
            assertEquals("Laptop 1", workbook.getSheetAt(0).getRow(1).getCell(3).getStringCellValue());
        }
        verify(entityManager).clear();
    }

    @Test
    void importCsv_acceptsBomAndFlushesPersistenceContextByBatch() {
        String csv = "\uFEFFcategory,name,model,description\n"
                + "LAPTOP,Device 1,Model 1,First\n"
                + "MONITOR,Device 2,Model 2,Second\n"
                + "PHONE,Device 3,Model 3,\n";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "devices.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        int imported = service.importCsv(file);

        assertEquals(3, imported);
        ArgumentCaptor<DeviceCreationRequest> requestCaptor =
                ArgumentCaptor.forClass(DeviceCreationRequest.class);
        verify(deviceService, times(3)).createDevice(requestCaptor.capture());
        assertEquals(
                List.of(DeviceCategory.LAPTOP, DeviceCategory.MONITOR, DeviceCategory.PHONE),
                requestCaptor.getAllValues().stream().map(DeviceCreationRequest::getCategory).toList()
        );
        verify(entityManager, times(2)).flush();
        verify(entityManager, times(2)).clear();
    }

    @Test
    void importCsv_rejectsMissingRequiredHeader() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "devices.csv",
                "text/csv",
                "category,name\nLAPTOP,Device 1\n".getBytes(StandardCharsets.UTF_8)
        );

        AppException exception = assertThrows(AppException.class, () -> service.importCsv(file));

        assertEquals(ErrorCode.INVALID_CSV_FILE, exception.getErrorCode());
    }

    @Test
    void importCsv_rejectsFileAboveConfiguredLimit() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1025L);

        AppException exception = assertThrows(AppException.class, () -> service.importCsv(file));

        assertEquals(ErrorCode.FILE_TOO_LARGE, exception.getErrorCode());
    }

    private Device device(String id, String name) {
        return Device.builder()
                .id(UUID.fromString(id))
                .category(DeviceCategory.LAPTOP)
                .serialNumber("LAP" + id.substring(id.length() - 7))
                .name(name)
                .model("Model")
                .description("Description")
                .state(DeviceState.AVAILABLE)
                .updatedBy("admin@device.test")
                .updatedTime(LocalDateTime.of(2026, 10, 9, 12, 0))
                .version(0L)
                .build();
    }
}
