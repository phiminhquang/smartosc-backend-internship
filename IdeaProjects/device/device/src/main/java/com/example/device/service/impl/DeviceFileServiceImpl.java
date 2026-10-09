package com.example.device.service.impl;

import com.example.device.configuration.DeviceFileProperties;
import com.example.device.dto.request.DeviceCreationRequest;
import com.example.device.enums.DeviceCategory;
import com.example.device.exception.AppException;
import com.example.device.exception.ErrorCode;
import com.example.device.model.Device;
import com.example.device.repository.DeviceRepository;
import com.example.device.service.DeviceFileService;
import com.example.device.service.DeviceService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceFileServiceImpl implements DeviceFileService {

    private static final String[] HEADERS = {
            "id", "category", "serialNumber", "name", "model",
            "description", "state", "updatedBy", "updatedTime"
    };
    private static final Set<String> REQUIRED_IMPORT_HEADERS = Set.of("category", "name", "model");

    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;
    private final EntityManager entityManager;
    private final DeviceFileProperties properties;

    @Override
    @Transactional(readOnly = true)
    public void exportCsv(OutputStream outputStream) {
        try {
            outputStream.write("\uFEFF".getBytes(StandardCharsets.UTF_8));
            Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            CSVPrinter printer = new CSVPrinter(
                    writer,
                    CSVFormat.DEFAULT.builder().setHeader(HEADERS).build()
            );

            forEachDeviceBatch(devices -> {
                for (Device device : devices) {
                    printer.printRecord(
                            device.getId(),
                            device.getCategory(),
                            device.getSerialNumber(),
                            device.getName(),
                            device.getModel(),
                            device.getDescription(),
                            device.getState(),
                            device.getUpdatedBy(),
                            device.getUpdatedTime()
                    );
                }
                printer.flush();
            });

            writer.flush();
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_PROCESSING_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void exportExcel(OutputStream outputStream) {
        SXSSFWorkbook workbook = new SXSSFWorkbook(properties.getExcelRowWindow());
        workbook.setCompressTempFiles(true);

        try {
            Sheet sheet = workbook.createSheet("Devices");
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                header.createCell(i).setCellValue(HEADERS[i]);
            }

            int[] rowIndex = {1};
            forEachDeviceBatch(devices -> {
                for (Device device : devices) {
                    Row row = sheet.createRow(rowIndex[0]++);
                    row.createCell(0).setCellValue(text(device.getId()));
                    row.createCell(1).setCellValue(text(device.getCategory()));
                    row.createCell(2).setCellValue(text(device.getSerialNumber()));
                    row.createCell(3).setCellValue(text(device.getName()));
                    row.createCell(4).setCellValue(text(device.getModel()));
                    row.createCell(5).setCellValue(text(device.getDescription()));
                    row.createCell(6).setCellValue(text(device.getState()));
                    row.createCell(7).setCellValue(text(device.getUpdatedBy()));
                    row.createCell(8).setCellValue(text(device.getUpdatedTime()));
                }
            });

            workbook.write(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_PROCESSING_ERROR);
        } finally {
            try {
                workbook.close();
            } catch (IOException ignored) {
                // The response may already be committed; disposal below still removes temp files.
            }
            workbook.dispose();
        }
    }

    @Override
    @Transactional
    public int importCsv(MultipartFile file) {
        validateCsvFile(file);
        int count = 0;

        try (PushbackReader reader = utf8ReaderWithoutBom(file);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build())) {

            validateHeaders(parser);

            for (CSVRecord record : parser) {
                DeviceCategory category = parseCategory(record.get("category"));
                String name = record.get("name");
                String model = record.get("model");
                String description = record.isMapped("description") ? record.get("description") : null;

                if (name.isBlank()) {
                    throw new AppException(ErrorCode.INVALID_DEVICE_NAME);
                }
                if (model.isBlank()) {
                    throw new AppException(ErrorCode.INVALID_DEVICE_MODEL);
                }

                DeviceCreationRequest request = DeviceCreationRequest.builder()
                        .category(category)
                        .name(name)
                        .model(model)
                        .description(description == null || description.isBlank() ? null : description)
                        .build();

                deviceService.createDevice(request);
                count++;

                if (count % properties.getImportBatchSize() == 0) {
                    flushAndClear();
                }
            }

            if (count % properties.getImportBatchSize() != 0) {
                flushAndClear();
            }
            return count;
        } catch (IOException | IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_CSV_FILE);
        }
    }

    private void forEachDeviceBatch(DeviceBatchConsumer consumer) throws IOException {
        Pageable batchRequest = PageRequest.of(0, properties.getExportBatchSize());
        List<Device> batch = deviceRepository.findAllByOrderByIdAsc(batchRequest);

        while (!batch.isEmpty()) {
            consumer.accept(batch);
            UUID lastId = batch.get(batch.size() - 1).getId();
            entityManager.clear();

            if (batch.size() < properties.getExportBatchSize()) {
                return;
            }
            batch = deviceRepository.findByIdGreaterThanOrderByIdAsc(lastId, batchRequest);
        }
    }

    private PushbackReader utf8ReaderWithoutBom(MultipartFile file) throws IOException {
        Reader delegate = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
        PushbackReader reader = new PushbackReader(delegate, 1);
        int firstCharacter = reader.read();
        if (firstCharacter != -1 && firstCharacter != '\uFEFF') {
            reader.unread(firstCharacter);
        }
        return reader;
    }

    private void validateHeaders(CSVParser parser) {
        if (!parser.getHeaderMap().keySet().containsAll(REQUIRED_IMPORT_HEADERS)) {
            throw new AppException(ErrorCode.INVALID_CSV_FILE);
        }
    }

    private DeviceCategory parseCategory(String value) {
        try {
            return DeviceCategory.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_DEVICE_CATEGORY);
        }
    }

    private void validateCsvFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.EMPTY_FILE);
        }
        if (file.getSize() > properties.getMaxImportBytes()) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private String text(Object value) {
        return value == null ? "" : value.toString();
    }

    @FunctionalInterface
    private interface DeviceBatchConsumer {
        void accept(List<Device> devices) throws IOException;
    }
}
