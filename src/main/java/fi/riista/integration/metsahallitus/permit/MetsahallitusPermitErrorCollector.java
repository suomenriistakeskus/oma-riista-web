package fi.riista.integration.metsahallitus.permit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MetsahallitusPermitErrorCollector {
    private final Map<String, Set<String>> allErrors = new HashMap<>();

    public Map<String, Set<String>> getAllErrors() {
        return allErrors;
    }

    public int getErrorCount(final MetsahallitusPermitImportDTO dto) {
        final Set<String> errorList = allErrors.get(dto.getLuvanTunnus());

        return errorList != null ? errorList.size() : 0;
    }

    public void missingValue(final MetsahallitusPermitImportDTO dto, final String fieldName) {
        getErrors(dto).add("tyhjä arvo kentässä:" + fieldName);
    }

    public void missingSsnAndHunterNumber(final MetsahallitusPermitImportDTO dto) {
        getErrors(dto).add("virheellinen arvo kentässä henkiloTunnus ja metsastajaNumero, kumpikin ovat tyhjät");
    }

    public void invalidDate(final MetsahallitusPermitImportDTO dto, final String value, final String fieldName) {
        getErrors(dto).add("virheellinen päivämäärä " + fieldName + " arvo:" + value);
    }

    public void invalidDateOrder(final MetsahallitusPermitImportDTO dto) {
        getErrors(dto).add("alkuPvm on loppuPvm jälkeen, alkuPvm:" + dto.getAlkuPvm() + " loppuPvm:" + dto.getLoppuPvm());
    }

    public void invalidUrl(final MetsahallitusPermitImportDTO dto) {
        getErrors(dto).add("virheellinen url: " + dto.getUrl());
    }

    private Set<String> getErrors(final MetsahallitusPermitImportDTO dto) {
        return allErrors.computeIfAbsent(dto.getLuvanTunnus(), ignored -> new HashSet<>());
    }
}
