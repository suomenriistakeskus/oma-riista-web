package fi.riista.integration.lupahallinta.club;

import fi.riista.feature.common.repository.NativeQueries;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class LHHuntingClubSynchronizer implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    public LHHuntingClubSynchronizer(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        jdbcTemplate.update(NativeQueries.UPDATE_UNCLAIMED_CLUBS_FROM_LH_ORGS);
        return RepeatStatus.FINISHED;
    }
}
