package com.mytask.abn.configuration;

import com.mytask.abn.model.ReportInputData;
import com.mytask.abn.model.ReportOutputData;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;


@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    private static final String[] REPORT_INPUT_FIELDS = {"recordCode", "clientType", "clientNumber", "accountNumber", "subAccountNumber",
            "oppositePartyCode", "productGroupCode", "exchangeCode", "symbol", "expirationDate", "currencyCode", "movementCode",
            "buySellCode", "quantityLongSign", "quantityLong", "quantityShortSign", "quantityShort", "exchBrokerFeeDec",
            "exchBrokerFeeDc", "exchBrokerFeeCurCode", "clearingFeeDec", "clearingFeeDc", "clearingFeeCurCode", "commission",
            "commissionDc", "commissionCurCode", "transactionDate", "futureReference", "ticketNumber", "externalNumber",
            "transactionPriceDec", "traderInitials", "oppositeTrader", "openCloseCode", "filler"};

    private  static final String[] REPORT_OUTPUT_FIELDS = {"Client_Information", "Product_Information", "Total_Transaction_Amount"};
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    @Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public FlatFileItemWriter<ReportOutputData> writer(@Value("#{jobParameters[outputFileName]}") String outputFileName) {
        //Create writer instance
        FlatFileItemWriter<ReportOutputData> writer = new FlatFileItemWriter<>();

        //Set output file location
        writer.setResource(new FileSystemResource(outputFileName));

        //All job repetitions should not "append" to same output file
        writer.setAppendAllowed(false);
        writer.setHeaderCallback(
                new FlatFileHeaderCallback() {
                    @Override
                    public void writeHeader(Writer writer) throws IOException {
                        writer.write(convertObjectArrayToString(REPORT_OUTPUT_FIELDS, ","));
                    }
                });
        writer.setShouldDeleteIfExists(true);

        //Name field values sequence based on object properties
        writer.setLineAggregator(new DelimitedLineAggregator<ReportOutputData>() {
            {
                setDelimiter(",");
                setFieldExtractor(new BeanWrapperFieldExtractor<ReportOutputData>() {
                    {
                        setNames(REPORT_OUTPUT_FIELDS);
                    }
                });
            }
        });
        return writer;
    }

    @Bean
    public ItemProcessor<ReportInputData, ReportOutputData> processor() {
        return new ItemProcessor<ReportInputData, ReportOutputData>() {
            @Override
            public ReportOutputData process(ReportInputData reportInputData)
                    throws Exception {
                StringBuilder clientInfoBuilder = new StringBuilder();
                clientInfoBuilder.append(reportInputData.getClientType());
                clientInfoBuilder.append(reportInputData.getClientNumber());
                clientInfoBuilder.append(reportInputData.getAccountNumber());
                clientInfoBuilder.append(reportInputData.getSubAccountNumber());

                StringBuilder productInfoBuilder = new StringBuilder();
                productInfoBuilder.append(reportInputData.getExchangeCode());
                productInfoBuilder.append(reportInputData.getProductGroupCode());
                productInfoBuilder.append(reportInputData.getSymbol());
                productInfoBuilder.append(reportInputData.getExpirationDate());

                BigDecimal totalTransactionAmount = BigDecimal.valueOf(reportInputData.getQuantityLong()).subtract(BigDecimal.valueOf(reportInputData.getQuantityShort()));
                ReportOutputData reportOutputData = new ReportOutputData(clientInfoBuilder.toString(), productInfoBuilder.toString(), totalTransactionAmount.toString());
                return reportOutputData;
            }
        };
    }

    @Bean
    public Job createEmployeeJob(JobCompletionListener listener, Step step1) {
        return jobBuilderFactory
                .get("createEmployeeJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .end()
                .build();
    }

    @Bean
    public Step step1(ItemReader<ReportInputData> reader, ItemWriter<ReportOutputData> writer,
                      ItemProcessor<ReportInputData, ReportOutputData> processor) {
        return stepBuilderFactory
                .get("step1")
                .<ReportInputData, ReportOutputData>chunk(5)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(5)
                .skip(FlatFileParseException.class)
                .taskExecutor(threadPoolTaskExecutor())
                .build();
    }


    @Bean
    @Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public FlatFileItemReader reader(@Value("#{jobParameters[fullPathFileName]}") String fullPathFileName) {
        FlatFileItemReader reader = new FlatFileItemReader();
        reader.setResource(new FileSystemResource(fullPathFileName));
        reader.setLineMapper(new DefaultLineMapper() {
            {
                setLineTokenizer(fixedLengthTokenizer());
                setFieldSetMapper(new BeanWrapperFieldSetMapper<ReportInputData>() {
                    {
                        setTargetType(ReportInputData.class);
                    }
                });
            }
        });
        return reader;
    }


    @Bean
    public FixedLengthTokenizer fixedLengthTokenizer() {
        FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
        tokenizer.setColumns(new Range(1, 3), new Range(4, 7), new Range(8, 11), new Range(12, 15), new Range(16, 19),
                new Range(20, 25), new Range(26, 27), new Range(28, 31), new Range(32, 37), new Range(38, 45), new Range(46, 48), new Range(49, 50),
                new Range(51, 51), new Range(52, 52), new Range(53, 62), new Range(63, 63), new Range(64, 73), new Range(74, 85), new Range(86, 86),
                new Range(87, 89), new Range(90, 101), new Range(102, 102), new Range(103, 105), new Range(106, 117), new Range(118, 118), new Range(119, 121),
                new Range(122, 129), new Range(130, 135), new Range(136, 141), new Range(142, 147), new Range(148, 162), new Range(163, 168), new Range(169, 175),
                new Range(176, 176), new Range(177, 303));
        tokenizer.setNames(REPORT_INPUT_FIELDS);

        tokenizer.setStrict(false);
        return tokenizer;
    }

    @Bean
    public JobCompletionListener jobCompletionListener() {
        return new JobCompletionListener();
    }

    @Bean
    public TaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.initialize();
        return executor;
    }

    private  String convertObjectArrayToString(Object[] arr, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : arr)
            sb.append(obj.toString()).append(delimiter);
        return sb.substring(0, sb.length() - 1);

    }
}