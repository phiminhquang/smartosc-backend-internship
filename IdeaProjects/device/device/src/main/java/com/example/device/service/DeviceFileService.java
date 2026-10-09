package com.example.device.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;

public interface DeviceFileService {

    void exportCsv(OutputStream outputStream);

    void exportExcel(OutputStream outputStream);

    int importCsv(MultipartFile file);
}
