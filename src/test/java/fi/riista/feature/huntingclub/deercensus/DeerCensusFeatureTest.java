package fi.riista.feature.huntingclub.deercensus;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gis.HtaNotResolvableByGeoLocationException;
import fi.riista.feature.gis.MockGISQueryService;
import fi.riista.feature.gis.RhyNotResolvableByGeoLocationException;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.hta.HirvitalousalueDTO;
import fi.riista.feature.huntingclub.CreateHuntingClubDTO;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubCrudFeature;
import fi.riista.feature.huntingclub.HuntingClubDTO;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DeerCensusFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private DeerCensusFeature deerCensusFeature;

    @Test
    public void testCreate() {
        HuntingClub huntingClub = model().newHuntingClub();

        onSavedAndAuthenticated(createNewModerator(), () -> {

            final DeerCensusDTO createDTO = getDeerCensusDTO(huntingClub.getId());

            DeerCensusDTO savedDTO = deerCensusFeature.create(createDTO);
            assertEquals(savedDTO.getObservationDate(), createDTO.getObservationDate());
            assertEquals(savedDTO.getHuntingClubId(), createDTO.getHuntingClubId());
            assertEquals(savedDTO.getFallowDeers(), createDTO.getFallowDeers());
            assertEquals(savedDTO.getRoeDeers(), createDTO.getRoeDeers());
            assertEquals(savedDTO.getWhiteTailDeers(), createDTO.getWhiteTailDeers());
            assertEquals(savedDTO.getFallowDeersAdditionalInfo(), createDTO.getFallowDeersAdditionalInfo());
            assertEquals(savedDTO.getRoeDeersAdditionalInfo(), createDTO.getRoeDeersAdditionalInfo());
            assertEquals(savedDTO.getWhiteTailDeersAdditionalInfo(), createDTO.getWhiteTailDeersAdditionalInfo());
        });
    }

    @Test
    public void testUpdate() {
        HuntingClub huntingClub = model().newHuntingClub();

        onSavedAndAuthenticated(createNewModerator(), () -> {

            final DeerCensusDTO createDTO = getDeerCensusDTO(huntingClub.getId());
            DeerCensusDTO editDTO = deerCensusFeature.create(createDTO);

            editDTO.setWhiteTailDeers(11);
            editDTO.setRoeDeers(12);
            editDTO.setFallowDeers(13);
            editDTO.setFallowDeersAdditionalInfo("aa");
            editDTO.setRoeDeersAdditionalInfo("bb");
            editDTO.setWhiteTailDeersAdditionalInfo("cc");

            DeerCensusDTO savedDTO = deerCensusFeature.update(editDTO);

            assertEquals(savedDTO.getFallowDeers(), editDTO.getFallowDeers());
            assertEquals(savedDTO.getRoeDeers(), editDTO.getRoeDeers());
            assertEquals(savedDTO.getWhiteTailDeers(), editDTO.getWhiteTailDeers());
            assertEquals(savedDTO.getFallowDeersAdditionalInfo(), editDTO.getFallowDeersAdditionalInfo());
            assertEquals(savedDTO.getRoeDeersAdditionalInfo(), editDTO.getRoeDeersAdditionalInfo());
            assertEquals(savedDTO.getWhiteTailDeersAdditionalInfo(), editDTO.getWhiteTailDeersAdditionalInfo());
        });
    }

    private DeerCensusDTO getDeerCensusDTO(Long huntingClubId) {
        final DeerCensusDTO dto = new DeerCensusDTO();
        dto.setObservationDate(LocalDate.now());
        dto.setHuntingClubId(huntingClubId);
        dto.setFallowDeers(1);
        dto.setRoeDeers(2);
        dto.setWhiteTailDeers(3);
        dto.setFallowDeersAdditionalInfo("a");
        dto.setRoeDeersAdditionalInfo("b");
        dto.setWhiteTailDeersAdditionalInfo("c");
        return dto;
    }
}
