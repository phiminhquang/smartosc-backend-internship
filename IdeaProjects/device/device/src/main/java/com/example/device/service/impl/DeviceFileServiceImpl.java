package com.example.device.service.impl;

import com.example.device.dto.request.DeviceCreationRequest;
import com.example.device.enums.DeviceCategory;
import com.example.device.exception.AppException;
import com.example.device.exception.ErrorCode;
import com.example.device.model.Device;
import com.example.device.repository.DeviceRepository;
import com.example.device.service.DeviceFileService;
import com.example.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DeviceFileServiceImpl implements DeviceFileService {

    private static final String[] HEADERS = {"id", "category", "serialNumber", "name", "model", "description", "state", "updatedBy", "updatedTime"};

    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;

    @Override
    @Transactional(readOnly = true)
    public byte[] exportCsv() {
        List<Device> devices = deviceRepository.findAll();

        try (StringWriter writer = new StringWriter();
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader(HEADERS).build())) {

            for (Device device : devices) {
                printer.printRecord(device.getId(), device.getCategory(), device.getSerialNumber(), device.getName(),
                        device.getModel(), device.getDescription(), device.getState(), device.getUpdatedBy(), device.getUpdatedTime());
            }

            printer.flush();
            return ("\uFEFF" + writer.toString()).getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_PROCESSING_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportExcel() {
        List<Device> devices = deviceRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Devices");
            Row header = sheet.createRow(0);

            for (int i = 0; i < HEADERS.length; i++) header.createCell(i).setCellValue(HEADERS[i]);

            int rowIndex = 1;
            for (Device device : devices) {
                Row row = sheet.createRow(rowIndex++);
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

            for (int i = 0; i < HEADERS.length; i++) sheet.autoSizeColumn(i);

            workbook.write(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_PROCESSING_ERROR);
        }
    }

    @Override
    @Transactional
    public int importCsv(MultipartFile file) {
        validateCsvFile(file);
        int count = 0;

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                     .setHeader().setSkipHeaderRecord(true).setTrim(true).build())) {

            for (CSVRecord record : parser) {
                DeviceCategory category = parseCategory(record.get("category"));
                String name = record.get("name");
                String model = record.get("model");
                String description = record.isMapped("description") ? record.get("description") : null;

                if (name.isBlank()) throw new AppException(ErrorCode.INVALID_DEVICE_NAME);
                if (model.isBlank()) throw new AppException(ErrorCode.INVALID_DEVICE_MODEL);

                DeviceCreationRequest request = DeviceCreationRequest.builder()
                        .category(category).name(name).model(model)
                        .description(description == null || description.isBlank() ? null : description)
                        .build();

                deviceService.createDevice(request);
                count++;
            }

            return count;
        } catch (IOException | IllegalArgumentException e) {
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
        if (file == null || file.isEmpty()) throw new AppException(ErrorCode.EMPTY_FILE);

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase(Locale.ROOT).endsWith(".csv"))
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
    }

    private String text(Object value) {
        return value == null ? "" : value.toString();
    }
}