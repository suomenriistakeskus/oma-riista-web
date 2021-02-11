package fi.riista.feature.gis.mobile;

import com.google.common.collect.Streams;
import fi.riista.feature.account.area.PersonalArea;
import fi.riista.feature.account.area.PersonalAreaRepository;
import fi.riista.feature.account.area.QPersonalArea;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.gis.zone.GISZoneWithoutGeometryDTO;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.area.HuntingClubAreaRepository;
import fi.riista.feature.moderatorarea.ModeratorArea;
import fi.riista.feature.moderatorarea.ModeratorAreaRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.HarvestPermitAreaRepository;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static fi.riista.util.Collect.idList;
import static java.util.stream.Collectors.toList;

@Component
public class MobileMapFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HuntingClubAreaRepository huntingClubAreaRepository;

    @Resource
    private HarvestPermitAreaRepository harvestPermitAreaRepository;

    @Resource
    private PersonalAreaRepository personalAreaRepository;

    @Resource
    private ModeratorAreaRepository moderatorAreaRepository;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Transactional(readOnly = true)
    public MobileAreaDTO findByExternalId(final String externalId) {
        final Optional<HuntingClubArea> clubAreaOptional = huntingClubAreaRepository.findByExternalId(externalId);

        if (clubAreaOptional.isPresent() && zoneAreaSizePositive(clubAreaOptional.get().getZone())) {
            return new MobileAreaDTO(clubAreaOptional.get());
        }

        final Optional<HarvestPermitArea> permitAreaOptional = harvestPermitAreaRepository.findByExternalId(externalId);

        if (permitAreaOptional.isPresent() && zoneAreaSizePositive(permitAreaOptional.get().getZone())) {
            return new MobileAreaDTO(permitAreaOptional.get());
        }

        final Optional<PersonalArea> personalAreaOptional = personalAreaRepository.findByExternalId(externalId);

        if (personalAreaOptional.isPresent() && zoneAreaSizePositive(personalAreaOptional.get().getZone())) {
            return new MobileAreaDTO(personalAreaOptional.get());
        }

        final Optional<ModeratorArea> moderatorAreaOptional = moderatorAreaRepository.findByExternalId(externalId);

        if (moderatorAreaOptional.isPresent()) {
            return new MobileAreaDTO(moderatorAreaOptional.get());
        }
        return null;
    }

    private boolean zoneAreaSizePositive(final GISZone zone) {
        return Optional.ofNullable(zone)
                .map(BaseEntity::getId)
                .map(gisZoneRepository::getAreaSize)
                .map(GISZoneSizeDTO::hasAreaSizeGreaterThanTenAres)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<MobileAreaDTO> listClubMaps(final Integer requestHuntingYear) {
        final Person person = activeUserService.requireActivePerson();
        final List<Occupation> clubOccupations = occupationRepository.findActiveByPersonAndOrganisationTypes(
                person, Collections.singleton(OrganisationType.CLUB));
        final HashSet<Organisation> clubs = F.mapNonNullsToSet(clubOccupations, Occupation::getOrganisation);
        final int huntingYear = requestHuntingYear != null ? requestHuntingYear : DateUtil.huntingYear();

        final List<HuntingClubArea> clubAreas = new LinkedList<>();
        final List<HarvestPermitArea> permitAreas = new LinkedList<>();

        for (final Organisation org : clubs) {
            final HuntingClub club = (HuntingClub) org;

            clubAreas.addAll(huntingClubAreaRepository.findByClubAndYear(club, huntingYear, true, false));
            permitAreas.addAll(harvestPermitAreaRepository.listActiveApplicationAreas(club, huntingYear));
        }

        final List<PersonalArea> personalAreas =
                personalAreaRepository.findAllAsList(QPersonalArea.personalArea.person.eq(person));
        final Predicate<GISZone> areaSizeFilter = filterWithZoneAreaSizePositive(clubAreas, permitAreas, personalAreas);

        return Streams.concat(
                filterClubAreas(clubAreas, areaSizeFilter),
                filterPermitAreas(permitAreas, areaSizeFilter),
                filterPersonalAreas(personalAreas, areaSizeFilter))
                .collect(toList());
    }

    private static Stream<MobileAreaDTO> filterClubAreas(final List<HuntingClubArea> clubAreas,
                                                         final Predicate<GISZone> areaSizeFilter) {
        return clubAreas.stream()
                .filter(a -> areaSizeFilter.test(a.getZone()))
                .filter(a -> StringUtils.hasText(a.getExternalId()))
                .map(MobileAreaDTO::new);
    }

    private static Stream<MobileAreaDTO> filterPermitAreas(final List<HarvestPermitArea> permitAreas,
                                                           final Predicate<GISZone> areaSizeFilter) {
        return permitAreas.stream()
                .filter(a -> areaSizeFilter.test(a.getZone()))
                .filter(a -> StringUtils.hasText(a.getExternalId()))
                .map(MobileAreaDTO::new);
    }

    private static Stream<MobileAreaDTO> filterPersonalAreas(final List<PersonalArea> personalAreas,
                                                             final Predicate<GISZone> areaSizeFilter) {
        return personalAreas.stream()
                .filter(a -> areaSizeFilter.test(a.getZone()))
                .filter(a -> StringUtils.hasText(a.getExternalId()))
                .map(MobileAreaDTO::new);
    }

    private Predicate<GISZone> filterWithZoneAreaSizePositive(final List<HuntingClubArea> clubAreas,
                                                              final List<HarvestPermitArea> permitAreas,
                                                              final List<PersonalArea> personalAreas) {
        final Set<Long> zoneIds = new HashSet<>();
        zoneIds.addAll(clubAreas.stream().map(HuntingClubArea::getZone).collect(idList()));
        zoneIds.addAll(permitAreas.stream().map(HarvestPermitArea::getZone).collect(idList()));
        zoneIds.addAll(personalAreas.stream().map(PersonalArea::getZone).collect(idList()));

        final Map<Long, GISZoneWithoutGeometryDTO> zoneMapping = gisZoneRepository.fetchWithoutGeometry(zoneIds);

        return zone -> Optional.ofNullable(F.getId(zone))
                .map(zoneMapping::get)
                .map(GISZoneWithoutGeometryDTO::getSize)
                .map(GISZoneSizeDTO::hasAreaSizeGreaterThanTenAres)
                .orElse(false);
    }

}
