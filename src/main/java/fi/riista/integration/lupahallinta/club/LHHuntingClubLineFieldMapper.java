package fi.riista.integration.lupahallinta.club;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.trimToNull;

public class LHHuntingClubLineFieldMapper implements FieldSetMapper<LHHuntingClubCSVRow> {
    private static final Logger LOG = LoggerFactory.getLogger(LHHuntingClubLineFieldMapper.class);

    private static final int EXPECTED_FIELD_COUNT = 21;

    private static String formatRow(final FieldSet fieldSet) {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < fieldSet.getFieldCount(); ++i) {
            strings.add(fieldSet.readString(i));
        }
        return StringUtils.join(strings, "\t");
    }

    @Override
    public LHHuntingClubCSVRow mapFieldSet(FieldSet fieldSet) {
        if (fieldSet.getFieldCount() <= 1) {
            LOG.debug("Ignoring invalid row: {}", formatRow(fieldSet));
            return null;
        }

        if (fieldSet.getFieldCount() != EXPECTED_FIELD_COUNT) {
            LOG.error("Invalid field set with field count {}, expexcted {}",
                    fieldSet.getFieldCount(), EXPECTED_FIELD_COUNT);
            LOG.debug("Invalid row was: {}", formatRow(fieldSet));
            throw new IllegalStateException(
                    MessageFormat.format("Invalid field set with field count {0}, expected {1}",
                            fieldSet.getFieldCount(), EXPECTED_FIELD_COUNT));
        }
        return csvToRow(fieldSet);
    }

    private static LHHuntingClubCSVRow csvToRow(FieldSet fieldSet) {
        final LHHuntingClubCSVRow row = new LHHuntingClubCSVRow();

        int fieldCounter = 0;

        row.setNimiSuomi(trimToNull(fieldSet.readString(fieldCounter++)));
        row.setNimiRuotsi(trimToNull(fieldSet.readString(fieldCounter++)));
        row.setValittuAlue(trimToNull(fieldSet.readString(fieldCounter++)));
        row.setAsiakasNumero(trimToNull(fieldSet.readString(fieldCounter++)));
        row.setRhy(trimToNull(fieldSet.readString(fieldCounter++)));
        row.setHirvitalousAlue(trimToNull(fieldSet.readString(fieldCounter++)));
        row.setpKoordinaatti(trimZeroValue(readInteger(fieldSet, fieldCounter++)));
        row.setiKoordinaatti(trimZeroValue(readInteger(fieldSet, fieldCounter++)));
        row.setPintaAla(trimZeroValue(readInteger(fieldSet, fieldCounter++, true)));
        row.setYhteysHenkilo(trimToNull(fieldSet.readString(fieldCounter++)));
        row.setValittuAlue2(trimToNull(fieldSet.readString(fieldCounter++)));
        row.setHetu(trimToNull(fieldSet.readString(fieldCounter++)));
        row.setOsoite(trimToNull(fieldSet.readString(fieldCounter++)));
        row.setOsoite2(trimToNull(fieldSet.readString(fieldCounter++)));
        row.setPostinumeroJaPostitoimipaikka(trimToNull(fieldSet.readString(fieldCounter++)));
        row.setRhy2(trimToNull(fieldSet.readString(fieldCounter++)));
        row.setTitteli(trimToNull(fieldSet.readString(fieldCounter++)));
        row.setPuhelin1(trimToNull(fieldSet.readString(fieldCounter++)));
        row.setPuhelin2(trimToNull(fieldSet.readString(fieldCounter++)));
        row.setSahkoposti(trimToNull(fieldSet.readString(fieldCounter++)));
        row.setKieli(trimToNull(fieldSet.readString(fieldCounter++)));

        return row;
    }

    public static String[] rowToCsv(final LHHuntingClubCSVRow row) {
        return new String[]{
                row.getNimiSuomi(),
                row.getNimiRuotsi(),
                row.getValittuAlue(),
                row.getAsiakasNumero(),
                row.getRhy(),
                row.getHirvitalousAlue(),
                numberToString(row.getpKoordinaatti()),
                numberToString(row.getiKoordinaatti()),
                numberToString(row.getPintaAla()),
                row.getYhteysHenkilo(),
                row.getValittuAlue2(),
                row.getHetu(),
                row.getOsoite(),
                row.getOsoite2(),
                row.getPostinumeroJaPostitoimipaikka(),
                row.getRhy2(),
                row.getTitteli(),
                row.getPuhelin1(),
                row.getPuhelin2(),
                row.getSahkoposti(),
                row.getKieli()
        };
    }

    private static Integer readInteger(final FieldSet fieldSet, final int fieldCounter) {
        return readInteger(fieldSet, fieldCounter, false);
    }

    private static Integer readInteger(final FieldSet fieldSet, final int fieldCounter, final boolean castFromDouble) {
        final String rawValue = fieldSet.readString(fieldCounter);

        if (StringUtils.isNotBlank(rawValue)) {
            if (castFromDouble) {
                return Double.valueOf(rawValue).intValue();
            }
            return Integer.valueOf(rawValue);
        }

        return null;
    }

    private static Integer trimZeroValue(final Integer value) {
        return value == null || value == 0 ? null : value;
    }

    private static String numberToString(Number n) {
        return n != null ? n.toString() : "";
    }
}
