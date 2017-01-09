package fi.riista.integration.lupahallinta;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.integration.common.entity.Integration;
import fi.riista.integration.common.repository.IntegrationRepository;
import fi.riista.integration.lupahallinta.parser.PermitCSVImporter;
import fi.riista.integration.lupahallinta.parser.PermitCSVLine;
import javaslang.Tuple3;
import liquibase.util.csv.opencsv.CSVReader;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class HarvestPermitImportFeature {
    private static final Logger LOG = LoggerFactory.getLogger(HarvestPermitImportFeature.class);

    public static final char SEPARATOR = ';';
    private static final char QUOTE_CHAR = '"';

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private IntegrationRepository integrationRepository;

    @Resource
    private PermitCSVImporter parser;

    @PersistenceContext
    private EntityManager entityManager;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Transactional(rollbackFor = {IOException.class, HarvestPermitImportException.class})
    public HarvestPermitImportResultDTO doImport(Reader reader, String requestInfo, DateTime lhSyncTime)
            throws IOException, HarvestPermitImportException {

        final List<HarvestPermitImportResultDTO.PermitParsingError> allErrors = new ArrayList<>();
        final List<String> messages = new ArrayList<>();
        final Set<String> permitNumbers = new HashSet<>();

        try (final CSVReader r = new CSVReader(reader, SEPARATOR, QUOTE_CHAR)) {

            final List<String[]> lines = r.readAll();
            final boolean hasHeader = "yhteyshenkilo".equals(lines.get(0)[0]);

            final List<HarvestPermit> addedOrChanged = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                if (hasHeader && i == 0) {
                    //skip header
                    continue;
                }

                final Tuple3<HarvestPermit, List<String>, PermitCSVLine> parsed = parser.process(lines.get(i));
                final HarvestPermit permit = parsed._1();
                final List<String> errors = parsed._2();
                final String permitNumber = parsed._3() != null ? parsed._3().getPermitNumber() : null;

                checkPermitUnique(errors, permitNumbers, permit);

                if (CollectionUtils.isEmpty(errors)) {
                    if (permit == null) {
                        LOG.info("Skipping line:" + i + " permitNumber:" + permitNumber);
                        continue;
                    }
                    if (permit.getSpeciesAmounts().isEmpty()) {
                        addEmptySpeciesAmountsMessage(messages, permit);
                    }
                    permit.setParsingInfo(requestInfo + ":" + i);
                    addedOrChanged.add(permit);
                } else {
                    allErrors.add(new HarvestPermitImportResultDTO.PermitParsingError(i, permitNumber, errors));
                }
                entityManager.flush();
                entityManager.clear();
            }
            if (allErrors.isEmpty()) {
                updateLhSyncTime(addedOrChanged, lhSyncTime);
                harvestPermitRepository.save(addedOrChanged);

                return new HarvestPermitImportResultDTO(addedOrChanged.size(), messages);
            }
        }

        throw new HarvestPermitImportException(allErrors);
    }

    private static void checkPermitUnique(List<String> errors, Set<String> permitNumbers, HarvestPermit permit) {
        if (permit != null) {
            final String permitNumber = permit.getPermitNumber();
            if (permitNumbers.contains(permitNumber)) {
                errors.add("Lupanumero tulee toiseen kertaan:" + permitNumber);
            }
            permitNumbers.add(permitNumber);
        }
    }

    private static void addEmptySpeciesAmountsMessage(List<String> messages, HarvestPermit permit) {
        String msg = String.format("Luvalla ei ole yhtään eläimen saalismäärää astettuna, voiko luvan poistaa? id:%s lupanumero:%s",
                permit.getId(), permit.getPermitNumber());
        messages.add(msg);
    }

    private static void updateLhSyncTime(List<HarvestPermit> permits, DateTime lhSyncTime) {
        if (lhSyncTime != null) {
            for (HarvestPermit p : permits) {
                p.setLhSyncTime(lhSyncTime);
            }
        }
    }

    @Transactional(readOnly = true)
    public DateTime getLastLhSyncTime() {
        return integrationRepository.getOne(Integration.LH_PERMIT_IMPORT_ID).getLastRun();
    }

    @Transactional(readOnly = false)
    public void updateLastLhSyncTime(DateTime now) {
        integrationRepository.getOne(Integration.LH_PERMIT_IMPORT_ID).setLastRun(now);
    }
}
