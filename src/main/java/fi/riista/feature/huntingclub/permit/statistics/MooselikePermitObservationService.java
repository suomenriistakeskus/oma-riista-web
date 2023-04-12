package fi.riista.feature.huntingclub.permit.statistics;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.QObservation;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.MooselikePermitObservationSummaryDTO;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.QGroupHuntingDay;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

import static fi.riista.util.F.coalesceAsInt;

@Component
public class MooselikePermitObservationService {

    @Resource
    private GameSpeciesRepository speciesRepository;

    @Resource
    private JPQLQueryFactory queryFactory;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public MooselikePermitObservationSummaryDTO getObservationSummaryDTO(final HarvestPermit permit, final int speciesCode) {

        final MooselikePermitObservationSummaryDTO dto = new MooselikePermitObservationSummaryDTO();

        speciesRepository.findByOfficialCode(speciesCode)
                .ifPresent(species -> fetchObservations(permit, species).forEach(
                        o -> addStatistics(dto, o)));

        return dto;
    }

    private void addStatistics(final MooselikePermitObservationSummaryDTO dto, final Observation o) {
        dto.addAdultMale(coalesceAsInt(o.getMooselikeMaleAmount(), 0))
                .addAdultFemaleNoCalfs(coalesceAsInt(o.getMooselikeFemaleAmount(), 0))
                .addAdultFemaleOneCalf(coalesceAsInt(o.getMooselikeFemale1CalfAmount(), 0))
                .addAdultFemaleTwoCalfs(coalesceAsInt(o.getMooselikeFemale2CalfsAmount(), 0))
                .addAdultFemaleThreeCalfs(coalesceAsInt(o.getMooselikeFemale3CalfsAmount(), 0))
                .addAdultFemaleFourCalfs(coalesceAsInt(o.getMooselikeFemale4CalfsAmount(), 0))
                .addSolitaryCalf(coalesceAsInt(o.getMooselikeCalfAmount(), 0))
                .addUnknown(coalesceAsInt(o.getMooselikeUnknownSpecimenAmount(), 0));
    }

    private List<Observation> fetchObservations(final HarvestPermit permit, final GameSpecies species) {
        final QObservation OBSERVATION = QObservation.observation;
        final QGroupHuntingDay DAY = QGroupHuntingDay.groupHuntingDay;
        final QHuntingClubGroup GROUP = QHuntingClubGroup.huntingClubGroup;
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;

        return queryFactory.selectFrom(OBSERVATION)
                .join(OBSERVATION.huntingDayOfGroup, DAY)
                .join(DAY.group, GROUP)
                .join(GROUP.harvestPermit, PERMIT)
                .where(PERMIT.eq(permit))
                .where(OBSERVATION.species.eq(species))
                .fetch();

    }
}
