package com.mytask.abn.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class BatchInvokerService {

    private static final Logger logger = LoggerFactory.getLogger(BatchInvokerService.class);
    private static final String FULL_PATH_FILE_NAME = "fullPathFileName";
    private static final String OUTPUT_FILE_NAME = "outputFileName";

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job createEmployeeJob;


    public BatchStatus runJobAsAsync(File inputFile, File ouputFile) {

        try {
            JobExecution jobExecution = jobLauncher.run(createEmployeeJob, new JobParametersBuilder()
                    .addString(FULL_PATH_FILE_NAME, inputFile.getAbsolutePath())
                    .addString(OUTPUT_FILE_NAME, ouputFile.getAbsolutePath())
                    .toJobParameters());
            return jobExecution.getStatus();
        } catch (Exception e) {
            logger.error("Error Reading File Name {} contents ", inputFile.getName());
            throw new IllegalStateException();
        }


    }
}
