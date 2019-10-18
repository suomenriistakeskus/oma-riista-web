package fi.riista.integration.metsahallitus.permit;

import fi.riista.validation.Validators;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.StringUtils.hasText;

public class MetsahallitusPermitParser {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("d.M.yyyy");

    private final MetsahallitusPermitErrorCollector errorCollector;

    public MetsahallitusPermitParser(final MetsahallitusPermitErrorCollector errorCollector) {
        this.errorCollector = Objects.requireNonNull(errorCollector);
    }

    public boolean parse(final MetsahallitusPermit permitEntity, final MetsahallitusPermitImportDTO dto) {
        return checkNoErrors(dto, () -> {
            if (isBlank(dto.getLuvanTunnus())) {
                errorCollector.missingValue(dto, "luvanTunnus");
            } else {
                permitEntity.setPermitIdentifier(dto.getLuvanTunnus());
            }

            if (isBlank(dto.getTilauksenTila())) {
                errorCollector.missingValue(dto, "tilauksenTila");
            } else {
                permitEntity.setStatus(dto.getTilauksenTila());
            }

            if (hasText(dto.getHenkiloTunnus()) && Validators.isValidSsn(dto.getHenkiloTunnus())) {
                permitEntity.setSsn(dto.getHenkiloTunnus());
            }

            if (hasText(dto.getMetsastajaNumero()) && Validators.isValidHunterNumber(dto.getMetsastajaNumero())) {
                permitEntity.setHunterNumber(dto.getMetsastajaNumero());
            }

            // Luvanhaltijan metsästäjänumero tai henkilötunnus on pakollinen tieto
            if (isBlank(permitEntity.getSsn()) && isBlank(permitEntity.getHunterNumber())) {
                errorCollector.missingSsnAndHunterNumber(dto);
            }

            permitEntity.setPermitType(trimToNull(dto.getLupaTyyppi()));
            permitEntity.setPermitTypeSwedish(trimToNull(dto.getLupaTyyppiSE()));
            permitEntity.setPermitTypeEnglish(trimToNull(dto.getLupaTyyppiEN()));

            permitEntity.setPermitName(trimToNull(dto.getLuvanNimi()));
            permitEntity.setPermitNameSwedish(trimToNull(dto.getLuvanNimiSE()));
            permitEntity.setPermitNameEnglish(trimToNull(dto.getLuvanNimiEN()));

            permitEntity.setAreaNumber(trimToNull(dto.getAlueNro()));
            permitEntity.setAreaName(trimToNull(dto.getAlueenNimi()));
            permitEntity.setAreaNameSwedish(trimToNull(dto.getAlueenNimiSE()));
            permitEntity.setAreaNameEnglish(trimToNull(dto.getAlueenNimiEN()));

            permitEntity.setUrl(parseUrl(dto, dto.getUrl()));
            permitEntity.setBeginDate(parseDate(dto, dto.getAlkuPvm(), "alkuPvm"));
            permitEntity.setEndDate(parseDate(dto, dto.getLoppuPvm(), "loppuPvm"));
            permitEntity.setHarvestReportSubmitted(dto.getSaalispalauteAnnettu());

            checkDateOrdering(dto, permitEntity.getBeginDate(), permitEntity.getEndDate());
        });
    }

    private boolean checkNoErrors(final MetsahallitusPermitImportDTO dto,
                                  final Runnable callback) {
        final int errorCountBefore = errorCollector.getErrorCount(dto);

        callback.run();

        final int errorCountAfter = errorCollector.getErrorCount(dto);

        return errorCountAfter == errorCountBefore;
    }

    private void checkDateOrdering(final MetsahallitusPermitImportDTO dto,
                                   final LocalDate beginDate, final LocalDate endDate) {
        if (beginDate != null && endDate != null && beginDate.isAfter(endDate)) {
            errorCollector.invalidDateOrder(dto);
        }
    }

    private LocalDate parseDate(final MetsahallitusPermitImportDTO dto,
                                final String value, final String fieldName) {
        if (isBlank(value)) {
            return null;
        }

        try {
            return DATE_FORMAT.parseLocalDate(value);

        } catch (Exception e) {
            errorCollector.invalidDate(dto, value, fieldName);

            return null;
        }
    }

    private String parseUrl(final MetsahallitusPermitImportDTO dto, final String url) {
        if (isBlank(url)) {
            return null;
        }

        try {
            return new URL(url).toString();

        } catch (MalformedURLException e) {
            errorCollector.invalidUrl(dto);

            return null;
        }
    }
}
