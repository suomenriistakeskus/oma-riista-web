package fi.riista.integration.metsastajarekisteri.innofactor;

import fi.riista.config.BatchConfig;
import fi.riista.feature.organization.person.ProcessDeceasedPersonFeature;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.integration.common.LoggingBatchListener;
import fi.riista.integration.metsastajarekisteri.MetsastajaRekisteriImportService;
import fi.riista.integration.metsastajarekisteri.MetsastajaRekisteriPerson;
import fi.riista.integration.metsastajarekisteri.common.MetsastajaRekisteriArchiveTasklet;
import fi.riista.integration.metsastajarekisteri.common.MetsastajaRekisteriItemValidator;
import fi.riista.integration.metsastajarekisteri.common.MetsastajaRekisteriJobParameters;
import fi.riista.integration.metsastajarekisteri.common.MetsastajaRekisteriPersonItemWriter;
import fi.riista.integration.metsastajarekisteri.exception.InvalidHunterDateFieldException;
import fi.riista.integration.metsastajarekisteri.exception.InvalidHunterNumberException;
import fi.riista.integration.metsastajarekisteri.exception.InvalidPersonName;
import fi.riista.integration.metsastajarekisteri.exception.InvalidRhyException;
import fi.riista.integration.metsastajarekisteri.exception.InvalidSsnException;
import fi.riista.integration.metsastajarekisteri.input.MetsastajaRekisteriBufferedReaderFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.DataIntegrityViolationException;

import javax.annotation.Resource;
import javax.persistence.PersistenceException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Configuration
public class InnofactorImportConfig {
    public static final String JOB_NAME = "innofactorImportJob";
    public static final String STEP_NAME = "innofactorImportStep";
    public static final String ARCHIVE_STEP = "innofactorArchiveStep";
    public static final String POST_PROCESS_STEP = "innofactorPostProcessStep";

    @Resource
    private JobBuilderFactory jobBuilder;

    @Resource
    private StepBuilderFactory stepBuilder;

    @Resource
    private MetsastajaRekisteriImportService importService;

    @Resource
    private MetsastajaRekisteriBufferedReaderFactory bufferedReaderFactory;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private ProcessDeceasedPersonFeature processDeceasedPersonFeature;

    @Bean(name = JOB_NAME)
    public Job innofactorImportJob(@Qualifier(STEP_NAME) Step innofactorImportStep) {
        return jobBuilder.get(JOB_NAME)
                .incrementer(new RunIdIncrementer())
                .validator(MetsastajaRekisteriJobParameters.createValidator())
                .start(innofactorImportStep)
                .next(innofactorArchiveStep())
                .next(innofactorPostProcessStep())
                .build();
    }

    @Bean(name = STEP_NAME)
    public Step innofactorImportStep(@Qualifier(STEP_NAME) ItemReader<InnofactorImportFileLine> reader,
                                     @Qualifier(STEP_NAME) ItemWriter<MetsastajaRekisteriPerson> writer) {
        CompositeItemProcessor<InnofactorImportFileLine, MetsastajaRekisteriPerson>
                processorChain = new CompositeItemProcessor<>();

        processorChain.setDelegates(Arrays.asList(
                new InnofactorImportFormatter(),
                new MetsastajaRekisteriItemValidator()
        ));

        return stepBuilder.get(JOB_NAME)
                .<InnofactorImportFileLine, MetsastajaRekisteriPerson>chunk(BatchConfig.BATCH_SIZE)
                .reader(reader)
                .processor(processorChain)
                .writer(writer)
                .faultTolerant()
                .skipLimit(200)
                .skip(PersistenceException.class)
                .skip(org.hibernate.exception.ConstraintViolationException.class)
                .skip(DataIntegrityViolationException.class)
                .skip(InvalidHunterDateFieldException.class)
                .skip(InvalidHunterNumberException.class)
                .skip(InvalidSsnException.class)
                .skip(InvalidPersonName.class)
                .skip(InvalidRhyException.class)
                .listener(new LoggingBatchListener())
                .build();
    }

    @Bean(name = ARCHIVE_STEP)
    public Step innofactorArchiveStep() {
        return stepBuilder.get(ARCHIVE_STEP)
                .tasklet(new MetsastajaRekisteriArchiveTasklet(fileStorageService))
                .build();
    }

    @Bean(name = POST_PROCESS_STEP)
    public Step innofactorPostProcessStep() {
        return stepBuilder.get(POST_PROCESS_STEP)
                .tasklet((stepContribution, chunkContext) -> {
                    processDeceasedPersonFeature.execute();
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    @StepScope
    @Qualifier(STEP_NAME)
    public MetsastajaRekisteriPersonItemWriter innofactorWriter(
            @Value("#{jobParameters['importTimestamp']}") long importTimestamp) {
        return new MetsastajaRekisteriPersonItemWriter(importService, importTimestamp);
    }

    @Bean
    @StepScope
    @Qualifier(STEP_NAME)
    public FlatFileItemReader<InnofactorImportFileLine> innofactorReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {

        FlatFileItemReader<InnofactorImportFileLine> reader = new FlatFileItemReader<>();

        reader.setEncoding(StandardCharsets.UTF_8.name());
        reader.setLineMapper(innofactorImportLineMapper());
        reader.setStrict(true);
        reader.setResource(new FileSystemResource(inputFile));
        reader.setLinesToSkip(0);
        reader.setBufferedReaderFactory(bufferedReaderFactory);

        return reader;
    }

    private static LineMapper<InnofactorImportFileLine> innofactorImportLineMapper() {
        DefaultLineMapper<InnofactorImportFileLine> lineMapper = new DefaultLineMapper<>();

        lineMapper.setFieldSetMapper(new InnofactorImportFileFieldSetMapper());
        lineMapper.setLineTokenizer(new DelimitedLineTokenizer(";"));

        return lineMapper;
    }
}
