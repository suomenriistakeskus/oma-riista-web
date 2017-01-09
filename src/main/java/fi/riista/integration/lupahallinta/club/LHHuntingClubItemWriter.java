package fi.riista.integration.lupahallinta.club;

import com.google.common.base.Joiner;
import org.springframework.batch.item.database.JdbcBatchItemWriter;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.stream.IntStream;

import static org.springframework.jdbc.core.StatementCreatorUtils.setParameterValue;

public class LHHuntingClubItemWriter extends JdbcBatchItemWriter<LHHuntingClubCSVRow> {
    private static final String[] columnNames = new String[]{
            "official_code",
            "name_finnish",
            "name_swedish",
            "rhy_official_code",
            "moose_area_code",
            "latitude",
            "longitude",
            "area_size",
            "contact_person_ssn",
            "contact_person_rhy",
            "contact_person_name",
            "contact_person_address_1",
            "contact_person_address_2",
            "contact_person_phone_1",
            "contact_person_phone_2",
            "contact_person_email",
            "contact_person_lang"
    };

    public LHHuntingClubItemWriter(final DataSource dataSource) {
        setDataSource(dataSource);

        setSql("INSERT INTO lh_org (" +
                Joiner.on(',').join(columnNames) + ") VALUES (" +
                Joiner.on(',').join(IntStream.range(0, columnNames.length).mapToObj(i -> '?').toArray()) + ");");

        setItemPreparedStatementSetter((item, ps) -> {
            int col = 1;
            setParameterValue(ps, col++, Types.VARCHAR, item.getAsiakasNumero());
            setParameterValue(ps, col++, Types.VARCHAR, item.getNimiSuomi());
            setParameterValue(ps, col++, Types.VARCHAR, item.getNimiRuotsi());
            setParameterValue(ps, col++, Types.VARCHAR, item.getRhy());
            setParameterValue(ps, col++, Types.VARCHAR, item.getHirvitalousAlue());
            setParameterValue(ps, col++, Types.INTEGER, item.getpKoordinaatti());
            setParameterValue(ps, col++, Types.INTEGER, item.getiKoordinaatti());
            setParameterValue(ps, col++, Types.INTEGER, item.getPintaAla());
            setParameterValue(ps, col++, Types.VARCHAR, item.getHetu());
            setParameterValue(ps, col++, Types.VARCHAR, item.getRhy2());
            setParameterValue(ps, col++, Types.VARCHAR, item.getYhteysHenkilo());
            setParameterValue(ps, col++, Types.VARCHAR, item.getOsoite());
            setParameterValue(ps, col++, Types.VARCHAR, item.getOsoite2());
            setParameterValue(ps, col++, Types.VARCHAR, item.getPuhelin1());
            setParameterValue(ps, col++, Types.VARCHAR, item.getPuhelin2());
            setParameterValue(ps, col++, Types.VARCHAR, item.getSahkoposti());
            setParameterValue(ps, col++, Types.VARCHAR, item.getKieli());
        });
    }
}
