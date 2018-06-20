package fi.riista.integration.lupahallinta.club;

import org.springframework.batch.item.database.JdbcBatchItemWriter;

import javax.sql.DataSource;
import java.sql.Types;

import static org.springframework.jdbc.core.StatementCreatorUtils.setParameterValue;

public class LHSynchronizeClubWriter extends JdbcBatchItemWriter<LHSynchronizeClubItem> {
    public LHSynchronizeClubWriter(final DataSource dataSource) {
        super();
        setDataSource(dataSource);

        setSql("UPDATE organisation SET" +
                " parent_organisation_id = ?," +
                " name_finnish = ?," +
                " name_swedish = ?," +
                " longitude = ?," +
                " latitude = ?," +
                " hunting_area_size = ?," +
                " moose_area_id = ?" +
                " WHERE organisation_id = ?");

        setItemPreparedStatementSetter((item, ps) -> {
            int col = 1;
            setParameterValue(ps, col++, Types.BIGINT, item.getRhyId());
            setParameterValue(ps, col++, Types.VARCHAR, item.getNameFinnish());
            setParameterValue(ps, col++, Types.VARCHAR, item.getNameSwedish());
            setParameterValue(ps, col++, Types.INTEGER, item.getLongitude());
            setParameterValue(ps, col++, Types.INTEGER, item.getLatitude());
            setParameterValue(ps, col++, Types.INTEGER, item.getAreaSize());
            setParameterValue(ps, col++, Types.INTEGER, item.getHtaId());
            setParameterValue(ps, col++, Types.BIGINT, item.getClubId());
        });
    }

}
