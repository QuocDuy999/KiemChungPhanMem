package com.example.food_store.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.example.food_store.constant.AppConstant;

import jakarta.servlet.ServletContext;

@ExtendWith(MockitoExtension.class)
class UploadServiceTest {

    @Mock
    private ServletContext servletContext;

    @InjectMocks
    private UploadService uploadService;

    @TempDir
    File tempDir;

    @Test
    void testHandleSaveUploadFile_WhenFileIsEmpty_ReturnEmptyString() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "",
                "text/plain",
                new byte[0]);

        String result = uploadService.handleSaveUploadFile(file, "images");

        assertEquals("", result);
    }

    @Test
    void testHandleSaveUploadFile_Success() {

        when(servletContext.getRealPath(AppConstant.LOCAL_PATH))
                .thenReturn(tempDir.getAbsolutePath());

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello".getBytes());

        String result = uploadService.handleSaveUploadFile(file, "images");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.endsWith("test.txt"));

        File folder = new File(tempDir, "images");
        assertTrue(folder.exists());

        File saved = new File(folder, result);
        assertTrue(saved.exists());
    }

    @Test
    void testHandleSaveUploadFile_WhenIOException_ReturnEmptyString() throws Exception {

        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenThrow(new IOException("Test exception"));

        String result = uploadService.handleSaveUploadFile(file, "images");

        assertEquals("", result);
    }
}