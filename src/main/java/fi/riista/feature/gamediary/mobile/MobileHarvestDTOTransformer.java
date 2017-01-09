package fi.riista.feature.gamediary.mobile;

import static java.util.stream.Collectors.toList;

import fi.riista.feature.gamediary.harvest.HarvestDTOTransformerBase;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.F;
import fi.riista.util.Functions;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
public class MobileHarvestDTOTransformer extends HarvestDTOTransformerBase<MobileHarvestDTO> {

    // Transactional propagation not mandated since entity associations are not traversed.
    @Transactional(readOnly = true)
    @Nullable
    public List<MobileHarvestDTO> apply(
            @Nullable final List<Harvest> list, @Nonnull final HarvestSpecVersion specVersion) {

        return list == null ? null : transform(list, specVersion);
    }

    // Transactional propagation not mandated since entity associations are not traversed.
    @Transactional(readOnly = true)
    @Nullable
    public MobileHarvestDTO apply(
            @Nullable final Harvest harvest, @Nonnull final HarvestSpecVersion specVersion) {

        if (harvest == null) {
            return null;
        }

        final List<MobileHarvestDTO> singletonList = apply(Collections.singletonList(harvest), specVersion);

        if (singletonList.size() != 1) {
            throw new IllegalStateException(
                    "Expected list containing exactly one harvest but has: " + singletonList.size());
        }

        return singletonList.get(0);
    }

    @Override
    protected List<MobileHarvestDTO> transform(final List<Harvest> harvests) {
        throw new UnsupportedOperationException("No transformation without harvestSpecVersion supported");
    }

    protected List<MobileHarvestDTO> transform(
            @Nonnull final List<Harvest> harvests, @Nonnull final HarvestSpecVersion specVersion) {

        Objects.requireNonNull(harvests, "harvests must not be null");
        Objects.requireNonNull(specVersion, "specVersion must not be null");

        final Function<Harvest, GameSpecies> harvestToSpecies = getGameDiaryEntryToSpeciesMapping(harvests);

        final Map<Harvest, List<GameDiaryImage>> groupedImages = getImagesGroupedByHarvests(harvests);
        final Map<Harvest, List<HarvestSpecimen>> groupedSpecimens = getSpecimensGroupedByHarvests(harvests);

        final Function<Harvest, HarvestPermit> harvestToPermit = getHarvestToPermitMapping(harvests);
        final Predicate<Harvest> isContactPersonOfPermittedHarvestTester =
                getContactPersonOfPermittedHarvestTester(getAuthenticatedPerson());
        final Function<Harvest, HarvestReport> harvestToReport = getHarvestToReportMapping(harvests);

        final Person authenticatedPerson = getAuthenticatedPerson();

        return harvests.stream().filter(Objects::nonNull).map(harvest -> {
            final HarvestReport report = harvestToReport.apply(harvest);

            return createDTO(
                    harvest,
                    specVersion,
                    harvestToSpecies.apply(harvest),
                    groupedSpecimens.get(harvest),
                    groupedImages.get(harvest),
                    harvestToPermit.apply(harvest),
                    report,
                    canEdit(authenticatedPerson, harvest, report, isContactPersonOfPermittedHarvestTester));
        }).collect(toList());
    }

    private static MobileHarvestDTO createDTO(
            final Harvest harvest,
            final HarvestSpecVersion specVersion,
            final GameSpecies species,
            final List<HarvestSpecimen> specimens,
            final Iterable<GameDiaryImage> images,
            final HarvestPermit permit,
            final HarvestReport report,
            final boolean canEdit) {

        final MobileHarvestDTO dto = MobileHarvestDTO.builder(specVersion)
                .populateWith(harvest)
                .populateWith(species)
                .populateSpecimensWith(specimens)
                .withCanEdit(canEdit)
                .build();

        if (images != null) {
            dto.setImageIds(F.mapNonNullsToList(images, Functions.idOf(GameDiaryImage::getFileMetadata)));
        }

        // showing any information of the report doesn't make sense, since harvest author might not have any access to the report
        if (report != null && !report.isInDeletedState() && (permit == null || !permit.isHarvestsAsList())) {
            dto.setHarvestReportDone(true);
            dto.setHarvestReportState(report.getState());
        }

        if (specVersion.supportsHarvestPermitState() && permit != null) {
            dto.setPermitNumber(permit.getPermitNumber());
            dto.setPermitType(permit.getPermitType());
        }

        return dto;
    }

}
