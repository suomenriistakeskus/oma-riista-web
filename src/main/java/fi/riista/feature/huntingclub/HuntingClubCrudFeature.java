package fi.riista.feature.huntingclub;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.gis.hta.GISHirvitalousalueDTO;
import fi.riista.feature.huntingclub.members.HuntingClubOccupationDTOTransformer;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationDTO;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationSort;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.ContactInfoShare;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class HuntingClubCrudFeature extends AbstractCrudFeature<Long, HuntingClub, HuntingClubDTO> {

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private HuntingClubOccupationDTOTransformer clubOccupationDTOTransformer;

    @Resource
    private GISQueryService gisQueryService;

    @Override
    protected JpaRepository<HuntingClub, Long> getRepository() {
        return huntingClubRepository;
    }

    @Override
    protected void updateEntity(final HuntingClub club, final HuntingClubDTO dto) {
        club.setNameFinnish(dto.getNameFI());
        club.setNameSwedish(dto.getNameSV());

        if (club.isNew()) {
            club.updateLocation(dto.getGeoLocation(), gisQueryService);
        }
    }

    @Override
    protected void afterCreate(final HuntingClub club) {
        // needs id, create official code after flush
        club.setOfficialCode(Long.toString(HuntingClub.CREATED_CLUB_MIN_OFFICIAL_CODE + club.getId()));

        final Person person = activeUserService.requireActivePerson();
        final Occupation contactPerson = new Occupation(person, club, OccupationType.SEURAN_YHDYSHENKILO);
        contactPerson.setBeginDate(OrganisationType.CLUB.getBeginDateForNewOccupation());
        contactPerson.setContactInfoShare(ContactInfoShare.ALL_MEMBERS);
        occupationRepository.save(contactPerson);
    }

    @Override
    protected HuntingClubDTO toDTO(@Nonnull final HuntingClub club) {
        final boolean editable = activeUserService.checkHasPermission(club, EntityPermission.UPDATE);
        final GISHirvitalousalueDTO mooseAreaDto = GISHirvitalousalueDTO.create(club.getMooseArea());
        return HuntingClubDTO.create(club, editable, getYhdyshenkilot(club), mooseAreaDto);
    }

    private List<OccupationDTO> getYhdyshenkilot(final HuntingClub club) {
        final List<Occupation> occupations = occupationRepository.findActiveByOrganisationAndOccupationType(club, OccupationType.SEURAN_YHDYSHENKILO)
                .stream()
                .sorted(OccupationSort.BY_LAST_NAME.thenComparing(OccupationSort.BY_BYNAME))
                .collect(toList());

        return clubOccupationDTOTransformer.apply(occupations);
    }

    @Transactional
    public GeoLocation updateLocation(long clubId, GeoLocation geoLocation) {
        final HuntingClub club = requireEntity(clubId, EntityPermission.UPDATE);

        club.updateLocation(geoLocation, gisQueryService);
        return club.getGeoLocation();
    }
}
