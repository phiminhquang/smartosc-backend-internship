package com.example.device.service;

import org.springframework.web.multipart.MultipartFile;

public interface DeviceFileService {

    byte[] exportCsv();

    byte[] exportExcel();

    int importCsv(MultipartFile file);
}