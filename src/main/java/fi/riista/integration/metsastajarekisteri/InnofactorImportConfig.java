package fi.riista.integration.metsastajarekisteri;

import fi.riista.config.BatchConfig;
import fi.riista.feature.organization.person.ProcessDeceasedPersonFeature;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.integration.common.LoggingBatchListener;
import fi.riista.integration.metsastajarekisteri.common.MetsastajaRekisteriArchiveTasklet;
import fi.riista.integration.metsastajarekisteri.common.MetsastajaRekisteriJobParameters;
import fi.riista.integration.metsastajarekisteri.exception.IllegalAgeException;
import fi.riista.integration.metsastajarekisteri.exception.InvalidHunterDateFieldException;
import fi.riista.integration.metsastajarekisteri.exception.InvalidHunterInvoiceReferenceException;
import fi.riista.integration.metsastajarekisteri.exception.InvalidHunterNumberException;
import fi.riista.integration.metsastajarekisteri.exception.InvalidPersonName;
import fi.riista.integration.metsastajarekisteri.exception.InvalidRhyException;
import fi.riista.integration.metsastajarekisteri.exception.InvalidSsnException;
import fi.riista.integration.metsastajarekisteri.input.MetsastajaRekisteriBufferedReaderFactory;
import fi.riista.integration.metsastajarekisteri.person.MetsastajaRekisteriPerson;
import fi.riista.integration.metsastajarekisteri.person.MetsastajaRekisteriPersonFormatter;
import fi.riista.integration.metsastajarekisteri.person.finnish.MetsastajaRekisteriFinnishPersonImportService;
import fi.riista.integration.metsastajarekisteri.person.finnish.MetsastajaRekisteriFinnishPersonValidator;
import fi.riista.integration.metsastajarekisteri.person.finnish.MetsastajaRekisteriFinnishPersonWriter;
import fi.riista.integration.metsastajarekisteri.person.foreign.MetsastajaRekisteriForeignPersonImportService;
import fi.riista.integration.metsastajarekisteri.person.foreign.MetsastajaRekisteriForeignPersonValidator;
import fi.riista.integration.metsastajarekisteri.person.foreign.MetsastajaRekisteriForeignPersonWriter;
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

import static java.util.Arrays.asList;

@Configuration
public class InnofactorImportConfig {

    public static final String JOB_NAME = "innofactorImportJob";

    public static final String MR_PERSON_READER = "innofactorPersonReader";
    public static final String MR_FINNISH_PERSON_IMPORT = "innofactorFinnishPersonImport";
    public static final String MR_FOREIGN_PERSON_IMPORT = "innofactorForeignPersonImport";

    public static final String ARCHIVE_STEP = "innofactorArchiveStep";
    public static final String POST_PROCESS_STEP = "innofactorPostProcessStep";

    @Resource
    private JobBuilderFactory jobBuilder;

    @Resource
    private StepBuilderFactory stepBuilder;

    @Resource
    private MetsastajaRekisteriFinnishPersonImportService finnishPersonImportService;

    @Resource
    private MetsastajaRekisteriForeignPersonImportService foreignPersonImportService;

    @Resource
    private MetsastajaRekisteriBufferedReaderFactory bufferedReaderFactory;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private ProcessDeceasedPersonFeature processDeceasedPersonFeature;

    @Bean(name = JOB_NAME)
    public Job innofactorImportJob(@Qualifier(MR_FINNISH_PERSON_IMPORT) final Step finnishPersonImportStep,
                                   @Qualifier(MR_FOREIGN_PERSON_IMPORT) final Step foreignPersonImportStep) {
        return jobBuilder.get(JOB_NAME)
                .incrementer(new RunIdIncrementer())
                .validator(MetsastajaRekisteriJobParameters.createValidator())
                .start(finnishPersonImportStep)
                .next(foreignPersonImportStep)
                .next(innofactorArchiveStep())
                .next(innofactorPostProcessStep())
                .build();
    }

    @Bean(name = MR_FINNISH_PERSON_IMPORT)
    public Step finnishPersonImportStep(@Qualifier(MR_PERSON_READER) final ItemReader<InnofactorImportFileLine> reader,
                                        @Qualifier(MR_FINNISH_PERSON_IMPORT) final ItemWriter<MetsastajaRekisteriPerson> writer) {

        final CompositeItemProcessor<InnofactorImportFileLine, MetsastajaRekisteriPerson> processorChain =
                new CompositeItemProcessor<>();

        processorChain.setDelegates(asList(
                new MetsastajaRekisteriPersonFormatter(),
                new MetsastajaRekisteriFinnishPersonValidator()
        ));

        return stepBuilder.get(MR_FINNISH_PERSON_IMPORT)
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
                .skip(InvalidHunterInvoiceReferenceException.class)
                .skip(InvalidHunterNumberException.class)
                .skip(InvalidSsnException.class)
                .skip(IllegalAgeException.class)
                .skip(InvalidPersonName.class)
                .skip(InvalidRhyException.class)
                .listener(new LoggingBatchListener())
                .build();
    }

    @Bean(name = MR_FOREIGN_PERSON_IMPORT)
    public Step foreignPersonImportStep(@Qualifier(MR_PERSON_READER) final ItemReader<InnofactorImportFileLine> reader,
                                        @Qualifier(MR_FOREIGN_PERSON_IMPORT) final ItemWriter<MetsastajaRekisteriPerson> writer) {

        final CompositeItemProcessor<InnofactorImportFileLine, MetsastajaRekisteriPerson> processorChain =
                new CompositeItemProcessor<>();

        processorChain.setDelegates(asList(
                new MetsastajaRekisteriPersonFormatter(),
                new MetsastajaRekisteriForeignPersonValidator()
        ));

        return stepBuilder.get(MR_FOREIGN_PERSON_IMPORT)
                .<InnofactorImportFileLine, MetsastajaRekisteriPerson>chunk(BatchConfig.BATCH_SIZE)
                .reader(reader)
                .processor(processorChain)
                .writer(writer)
                .faultTolerant()
                .skipLimit(200)
                .skip(PersistenceException.class)
                .skip(org.hibernate.exception.ConstraintViolationException.class)
                .skip(DataIntegrityViolationException.class)
                .skip(InvalidHunterNumberException.class)
                .skip(InvalidPersonName.class)
                .skip(IllegalAgeException.class)
                .listener(new LoggingBatchListener())
                .build();
    }

    @Bean
    @StepScope
    @Qualifier(MR_PERSON_READER)
    public FlatFileItemReader<InnofactorImportFileLine> innofactorReader(
            @Value("#{jobParameters['inputFile']}") final String inputFile) {

        final FlatFileItemReader<InnofactorImportFileLine> reader = new FlatFileItemReader<>();

        reader.setEncoding(StandardCharsets.UTF_8.name());
        reader.setLineMapper(innofactorImportLineMapper());
        reader.setStrict(true);
        reader.setResource(new FileSystemResource(inputFile));
        reader.setLinesToSkip(0);
        reader.setBufferedReaderFactory(bufferedReaderFactory);

        return reader;
    }

    private static LineMapper<InnofactorImportFileLine> innofactorImportLineMapper() {
        final DefaultLineMapper<InnofactorImportFileLine> lineMapper = new DefaultLineMapper<>();

        lineMapper.setFieldSetMapper(new InnofactorImportFileFieldSetMapper());
        lineMapper.setLineTokenizer(new DelimitedLineTokenizer(";"));

        return lineMapper;
    }

    @Bean
    @StepScope
    @Qualifier(MR_FINNISH_PERSON_IMPORT)
    public MetsastajaRekisteriFinnishPersonWriter finnishPersonWriter(
            @Value("#{jobParameters['importTimestamp']}") final long importTimestamp) {

        return new MetsastajaRekisteriFinnishPersonWriter(finnishPersonImportService, importTimestamp);
    }

    @Bean
    @StepScope
    @Qualifier(MR_FOREIGN_PERSON_IMPORT)
    public MetsastajaRekisteriForeignPersonWriter foreignPersonWriter(
            @Value("#{jobParameters['importTimestamp']}") final long importTimestamp) {

        return new MetsastajaRekisteriForeignPersonWriter(foreignPersonImportService, importTimestamp);
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
}
