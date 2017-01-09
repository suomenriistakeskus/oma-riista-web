package fi.riista.feature.gamediary.harvest;

import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestpermit.season.HarvestQuotaDTO;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFieldsDTO;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFields;
import fi.riista.feature.harvestpermit.season.HarvestQuotaRepository;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFieldsRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.QGroupHuntingDay;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.F;
import fi.riista.util.Functions;
import fi.riista.util.jpa.CriteriaUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class HarvestDTOTransformer extends HarvestDTOTransformerBase<HarvestDTO> {

    @Resource
    private HarvestQuotaRepository harvestQuotaRepository;

    @Resource
    private HarvestReportFieldsRepository harvestReportFieldsRepository;

    @Resource
    private JPAQueryFactory queryFactory;

    // Transactional propagation not mandated since entity associations are not traversed.
    @Transactional(readOnly = true)
    @Nonnull
    @Override
    public List<HarvestDTO> transform(@Nonnull final List<Harvest> harvests) {
        Objects.requireNonNull(harvests);

        final Person authenticatedPerson = getAuthenticatedPerson();

        final Function<Harvest, GameSpecies> harvestToSpecies = getGameDiaryEntryToSpeciesMapping(harvests);
        final Function<Harvest, Person> harvestToAuthor = getGameDiaryEntryToAuthorMapping(harvests);
        final Function<Harvest, Person> harvestToHunter = getHarvestToHunterMapping(harvests);

        final Function<Harvest, HarvestPermit> harvestToPermit = getHarvestToPermitMapping(harvests);
        final Function<Harvest, HarvestQuota> harvestToQuota = getHarvestToQuotaMapping(harvests);
        final Function<Harvest, HarvestReport> harvestToReport = getHarvestToReportMapping(harvests);
        final Function<Harvest, HarvestReportFields> fieldsMapping = getHarvestToFieldsMapping(harvests);

        final Map<Harvest, List<HarvestSpecimen>> groupedSpecimens = getSpecimensGroupedByHarvests(harvests);
        final Map<Harvest, List<GameDiaryImage>> groupedImages = getImagesGroupedByHarvests(harvests);
        final Predicate<Harvest> isContactPersonOfPermittedHarvestTester =
                getContactPersonOfPermittedHarvestTester(authenticatedPerson);

        final Map<Harvest, Organisation> harvestToGroupOfHuntingDay = getGroupOfHuntingDay(harvests);
        final Function<Harvest, Person> harvestToHuntingDayApprover = getApproverToHuntingDay(harvests);

        return harvests.stream().filter(Objects::nonNull).map(harvest -> {
            final Person author = harvestToAuthor.apply(harvest);
            final Person hunter = harvestToHunter.apply(harvest);
            final Organisation groupOfHuntingDay = harvestToGroupOfHuntingDay.get(harvest);
            final Person approverToHuntingDay = harvestToHuntingDayApprover.apply(harvest);

            final HarvestReport report = harvestToReport.apply(harvest);

            return createDTO(
                    harvest,
                    harvestToSpecies.apply(harvest),
                    groupedSpecimens.get(harvest),
                    groupedImages.get(harvest),
                    author,
                    hunter,
                    harvestToPermit.apply(harvest),
                    harvestToQuota.apply(harvest),
                    report,
                    fieldsMapping.apply(harvest),
                    authenticatedPerson,
                    canEdit(authenticatedPerson, harvest, report, isContactPersonOfPermittedHarvestTester),
                    groupOfHuntingDay,
                    approverToHuntingDay);

        }).collect(toList());
    }

    private static HarvestDTO createDTO(
            final Harvest harvest,
            final GameSpecies species,
            final List<HarvestSpecimen> specimens,
            final Iterable<GameDiaryImage> images,
            final Person author,
            final Person actor,
            final HarvestPermit harvestPermit,
            final HarvestQuota harvestQuota,
            final HarvestReport harvestReport,
            final HarvestReportFields harvestReportFields,
            final Person authenticatedPerson,
            final boolean canEdit,
            final Organisation groupOfHuntingDay,
            final Person approverToHuntingDay) {

        final HarvestDTO dto = HarvestDTO.builder()
                .populateWith(harvest)
                .populateWith(species)
                .populateSpecimensWith(specimens)
                .withAuthorInfo(author)
                .withActorInfo(actor)
                .withCanEdit(canEdit)
                .withGroupOfHuntingDay(groupOfHuntingDay)
                .withApproverToHuntingDay(approverToHuntingDay)
                .build();

        final boolean readOnly = !author.equals(authenticatedPerson) && !actor.equals(authenticatedPerson);
        dto.setReadOnly(readOnly);

        if (readOnly) {
            dto.setDescription(null);
        }

        if (author.equals(authenticatedPerson)) {
            dto.setAuthoredByMe(true);
        }
        dto.setReportedForMe(!dto.isAuthoredByMe());

        if (harvestPermit != null) {
            dto.setPermitNumber(harvestPermit.getPermitNumber());
        }

        if (harvestQuota != null) {
            dto.setHarvestQuota(HarvestQuotaDTO.create(harvestQuota));
        }

        if (harvestReportFields != null) {
            dto.setFields(HarvestReportFieldsDTO.createWithSpecies(harvestReportFields, species));
        }

        if (harvestReport != null && !harvestReport.isInDeletedState()) {
            dto.setHarvestReportState(harvestReport.getState());
            dto.setHarvestReportId(harvestReport.getId());
        }

        if (images != null && !readOnly) {
            F.mapNonNulls(images, dto.getImageIds(), Functions.idOf(GameDiaryImage::getFileMetadata));
        }

        return dto;
    }

    private Function<Harvest, Person> getHarvestToHunterMapping(final Iterable<Harvest> harvests) {
        return createGameDiaryEntryToPersonMapping(harvests, Harvest::getActualShooter);
    }

    private Function<Harvest, HarvestQuota> getHarvestToQuotaMapping(final Iterable<Harvest> harvests) {
        return CriteriaUtils.singleQueryFunction(harvests, Harvest::getHarvestQuota, harvestQuotaRepository, false);
    }

    private Function<Harvest, HarvestReportFields> getHarvestToFieldsMapping(final Iterable<Harvest> harvests) {
        return CriteriaUtils
                .singleQueryFunction(harvests, Harvest::getHarvestReportFields, harvestReportFieldsRepository, false);
    }

    private Function<Harvest, Person> getApproverToHuntingDay(final Iterable<Harvest> harvests) {
        return createGameDiaryEntryToPersonMapping(harvests, Harvest::getApproverToHuntingDay, false);
    }

    private Map<Harvest, Organisation> getGroupOfHuntingDay(final List<Harvest> harvests) {
        final QHarvest harvest = QHarvest.harvest;
        final QGroupHuntingDay day = QGroupHuntingDay.groupHuntingDay;
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;
        return queryFactory.select(harvest, group)
                .from(harvest)
                .join(harvest.huntingDayOfGroup, day)
                .join(day.group, group)
                .where(harvest.in(harvests))
                .fetch()
                .stream().collect(toMap(t -> t.get(0, Harvest.class), t -> t.get(1, HuntingClubGroup.class)));
    }
}
