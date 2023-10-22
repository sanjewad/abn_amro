package com.mytask.abn.controller;

import com.mytask.abn.service.BatchInvokerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ReportGenerateController.class})
public class ReportGenerateControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ReportGenerateController reportGenerateController;

    @MockBean
    private BatchInvokerService batchInvokerService;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(reportGenerateController).build();
    }

    @Test
    public void testReportGeneratorInvalidUrl() throws Exception {

        Resource fileResource = new ClassPathResource(
                "input.txt");

        MockMultipartFile firstFile = new MockMultipartFile(
                "file", fileResource.getFilename(),
                MediaType.TEXT_PLAIN_VALUE,
                fileResource.getInputStream());

        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/abn/v2/single-file-upload")
                .file(firstFile))
                .andExpect(status().is(404));

    }

    @Test
    public void testReportGeneratorNoValidResponse() throws Exception {

        Resource fileResource = new ClassPathResource(
                "input.txt");

        MockMultipartFile firstFile = new MockMultipartFile(
                "file", fileResource.getFilename(),
                MediaType.TEXT_PLAIN_VALUE,
                fileResource.getInputStream());

        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/abn/v1/single-file-upload")
                .file(firstFile))
                .andExpect(status().is(204));
    }

    @Test
    public void testReportGeneratorWithValidResponse() throws Exception {

        Resource fileResource = new ClassPathResource(
                "input.txt");

        when(batchInvokerService.runJobAsAsync(any(), any())).thenReturn(BatchStatus.COMPLETED);
        MockMultipartFile firstFile = new MockMultipartFile(
                "file", fileResource.getFilename(),
                MediaType.TEXT_PLAIN_VALUE,
                fileResource.getInputStream());

        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/abn/v1/single-file-upload")
                .file(firstFile))
                .andExpect(status().is(200));
        verify(batchInvokerService).runJobAsAsync(any(), any());

    }
}