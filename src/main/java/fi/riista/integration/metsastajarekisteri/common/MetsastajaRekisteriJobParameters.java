package fi.riista.integration.metsastajarekisteri.common;

import fi.riista.integration.metsastajarekisteri.input.PendingImportFile;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.job.DefaultJobParametersValidator;

import java.util.Map;

public class MetsastajaRekisteriJobParameters {

    public static final String JOB_PARAMETER_INPUT_FILE = "inputFile";
    public static final String JOB_PARAMETER_IMPORT_TIMESTAMP = "importTimestamp";

    public static JobParameters createJobParameters(final PendingImportFile entry) {
        return new JobParametersBuilder()
                .addString(JOB_PARAMETER_INPUT_FILE, entry.getInputFile().toString())
                .addLong(JOB_PARAMETER_IMPORT_TIMESTAMP, entry.getLastModifiedAt().toMillis())
                .toJobParameters();
    }

    public static JobParametersValidator createValidator() {
        final DefaultJobParametersValidator parametersValidator = new DefaultJobParametersValidator();

        final String[] required = {
                JOB_PARAMETER_IMPORT_TIMESTAMP, JOB_PARAMETER_INPUT_FILE
        };
        final String[] optional = {};

        parametersValidator.setRequiredKeys(required);
        parametersValidator.setOptionalKeys(optional);

        return parametersValidator;
    }

    private final Map<String, Object> rawParameters;

    public MetsastajaRekisteriJobParameters(final Map<String, Object> rawParameters) {
        this.rawParameters = rawParameters;
    }

    public String getInputFile() {
        return (String) this.rawParameters.get(JOB_PARAMETER_INPUT_FILE);
    }
}
