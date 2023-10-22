package com.mytask.abn.controller;

import com.mytask.abn.service.BatchInvokerService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Objects;

import static org.springframework.batch.core.BatchStatus.COMPLETED;

@RestController
@RequestMapping("abn/v1")
public class ReportGenerateController {

    private static final Logger logger = LoggerFactory.getLogger(ReportGenerateController.class);
    private static final String TEMP = "temp";
    private static final String OUTPUT_CSV = "Output.csv";
    private static final String APPLICATION_CSV = "application/csv";

    @Autowired
    private BatchInvokerService batchInvokerService;

    @PostMapping("/single-file-upload")
    public ResponseEntity<Resource> handleFileUploadUsingCurl(
            @RequestParam("file") MultipartFile file) throws IOException {
        File tempFile = null;
        try {
            tempFile = new File(TEMP + Instant.now().toEpochMilli());
            File outputFile = new File(OUTPUT_CSV);
            FileUtils.writeByteArrayToFile(tempFile, file.getBytes());
            //Launch the Batch Job

            BatchStatus status = batchInvokerService.runJobAsAsync(tempFile, outputFile);
            if(status == COMPLETED) {
                InputStream targetStream = new FileInputStream(outputFile);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + outputFile.getName())
                        .contentType(MediaType.parseMediaType(APPLICATION_CSV))
                        .body(new InputStreamResource(targetStream));
            }else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
            }
        } catch (Exception e) {
            logger.error("Error Reading File Name {} contents ", tempFile.getName());
            throw new IllegalStateException();
        } finally {
            try {
                FileUtils.forceDelete(Objects.requireNonNull(tempFile));
            } catch (IOException e) {
                logger.error("Error Deleting File Name");
            }
        }
    }
}
