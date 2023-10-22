package com.mytask.abn.controller;

import com.mytask.abn.service.BatchInvokerService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Profile("test")
public class ReportGeneratorControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private BatchInvokerService batchInvokerService;

    @Test()
    public void testGenerateReportWithInValidRequestFile() {
     //   Exception exception = assertThrows( FileNotFoundException.class, () -> new ClassPathResource("inputErr.txt"));
        Resource fileResource = new ClassPathResource("input.txt");
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", fileResource);
        var result = webTestClient.post()
                .uri("/single-file-upload")
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectBody().returnResult();
      //  assertNotNull(exception);
    }

    @Test
    public void testGenerateReportWithValidRequest() {
        Resource fileResource = new ClassPathResource("input.txt");
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", fileResource);
        var result = webTestClient.post()
                .uri("/single-file-upload")
                .contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectBody().returnResult();

        String s = new String(result.getResponseBodyContent());
        System.out.println(s); //Normally this is not included in test. Included for display purposes only
        Assert.assertTrue(s.contains("Client_Information"));
        Assert.assertTrue(s.contains("Product_Information"));
        Assert.assertTrue(s.contains("Total_Transaction_Amount"));
        Assert.assertTrue(s.contains("CL432100020001"));
        Assert.assertTrue(s.contains("SGXFUNK20100910"));
        Assert.assertTrue(s.contains("CL123400030001"));

    }
}
