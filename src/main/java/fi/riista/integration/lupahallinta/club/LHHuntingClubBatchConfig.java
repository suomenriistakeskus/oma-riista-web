package fi.riista.integration.lupahallinta.club;

import com.querydsl.sql.SQLQueryFactory;
import fi.riista.config.BatchConfig;
import fi.riista.integration.common.LoggingBatchListener;
import fi.riista.integration.lupahallinta.support.LupahallintaHttpClient;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

@Configuration
public class LHHuntingClubBatchConfig {
    public static final String JOB_NAME = "lhClubImportJob";
    public static final String DOWNLOAD_STEP = "lhClubDownloadStep";
    public static final String TRUNCATE_STEP = "lhClubTruncateStep";
    public static final String IMPORT_STEP = "lhClubImportStep";
    public static final String SYNCHRONIZE_STEP = "lhClubSynchronizeStep";
    public static final String KEY_INPUT_FILE = "inputFile";

    @Resource
    private DataSource dataSource;

    @Resource
    private JobBuilderFactory jobBuilderFactory;

    @Resource
    private StepBuilderFactory stepBuilderFactory;

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    @Bean(name = JOB_NAME)
    public Job lhClubImportJob(
            @Qualifier(DOWNLOAD_STEP) Step download,
            @Qualifier(TRUNCATE_STEP) Step truncate,
            @Qualifier(IMPORT_STEP) Step importStep,
            @Qualifier(SYNCHRONIZE_STEP) Step synchronizeStep) {
        return jobBuilderFactory.get(JOB_NAME)
                .incrementer(new RunIdIncrementer())
                .listener(new LHHuntingClubCSVCleaner())
                .start(download)
                .next(truncate)
                .next(importStep)
                .next(synchronizeStep)
                .build();
    }

    @Bean(name = DOWNLOAD_STEP)
    public Step lhClubDownloadStep(
            LupahallintaHttpClient lupahallintaHttpClient) {
        return stepBuilderFactory.get(DOWNLOAD_STEP)
                .tasklet(new LHHuntingClubCSVDownloader(lupahallintaHttpClient))
                .build();
    }

    @Bean(name = TRUNCATE_STEP)
    public Step lhClubTruncateStep() {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        return stepBuilderFactory.get(TRUNCATE_STEP)
                .tasklet((contribution, chunkContext) -> {
                    jdbcTemplate.update("DELETE FROM lh_org");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean(name = IMPORT_STEP)
    public Step lhClubImportStep(@Qualifier(IMPORT_STEP) FlatFileItemReader<LHHuntingClubCSVRow> reader,
                                 @Qualifier(IMPORT_STEP) LHHuntingClubItemWriter writer,
                                 ItemProcessor<LHHuntingClubCSVRow, LHHuntingClubCSVRow> processor) {
        return stepBuilderFactory.get(JOB_NAME)
                .<LHHuntingClubCSVRow, LHHuntingClubCSVRow>chunk(BatchConfig.BATCH_SIZE)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(new LoggingBatchListener())
                .build();
    }

    @Bean(name = SYNCHRONIZE_STEP)
    public Step lhClubSynchronizeStep() {
        return stepBuilderFactory.get(SYNCHRONIZE_STEP)
                .<LHSynchronizeClubItem, LHSynchronizeClubItem>chunk(BatchConfig.BATCH_SIZE)
                .reader(new LHSynchronizeClubReader(sqlQueryFactory))
                .writer(new LHSynchronizeClubWriter(dataSource))
                .faultTolerant()
                .skipLimit(100)
                .skip(DataAccessException.class)
                .listener(new LoggingBatchListener())
                .build();
    }

    @Bean
    @StepScope
    @Qualifier(IMPORT_STEP)
    public FlatFileItemReader<LHHuntingClubCSVRow> lhClubImportReader(
            @Value("#{jobExecutionContext['inputFile']}") String inputFile) {
        final FlatFileItemReader<LHHuntingClubCSVRow> reader = new FlatFileItemReader<>();

        reader.setEncoding(StandardCharsets.ISO_8859_1.name());
        reader.setLineMapper(inputLineMapper());
        reader.setStrict(true);
        reader.setResource(new FileSystemResource(inputFile));
        reader.setLinesToSkip(1);

        return reader;
    }

    private static LineMapper<LHHuntingClubCSVRow> inputLineMapper() {
        DefaultLineMapper<LHHuntingClubCSVRow> lineMapper = new DefaultLineMapper<>();

        lineMapper.setFieldSetMapper(new LHHuntingClubLineFieldMapper());
        lineMapper.setLineTokenizer(new DelimitedLineTokenizer(";"));

        return lineMapper;
    }

    @Bean
    @StepScope
    @Qualifier(IMPORT_STEP)
    public LHHuntingClubItemValidator lhClubImportProcessor() {
        return new LHHuntingClubItemValidator();
    }

    @Bean
    @StepScope
    @Qualifier(IMPORT_STEP)
    public LHHuntingClubItemWriter lhClubImportWriter() {
        return new LHHuntingClubItemWriter(dataSource);
    }
}
