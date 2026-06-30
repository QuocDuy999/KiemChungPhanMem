package com.example.food_store.service.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.food_store.constant.AppConstant;
import com.example.food_store.service.IUploadService;

import jakarta.servlet.ServletContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadService implements IUploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadService.class);

    private final ServletContext servletContext;

    @Override
    public String handleSaveUploadFile(MultipartFile file, String targetFolder) {

        if (file.isEmpty()) {
            return "";
        }

        String finalName = "";

        try {
            byte[] bytes = file.getBytes();

            String rootPath = servletContext.getRealPath(AppConstant.LOCAL_PATH);
            File dir = new File(rootPath + File.separator + targetFolder);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Create the file on server
            finalName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
            File serverFile = new File(dir.getAbsolutePath() + File.separator + finalName);

            try (BufferedOutputStream stream =
                    new BufferedOutputStream(new FileOutputStream(serverFile))) {
                stream.write(bytes);
            }

        } catch (IOException e) {
            log.error("Error while uploading file", e);
        }

        return finalName;
    }
}