package fi.riista.feature.huntingclub;

import com.google.common.base.Preconditions;
import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.common.entity.FinnishBusinessIdEntity;
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
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.security.EntityPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @Resource
    private PersonRepository personRepository;

    @Override
    protected JpaRepository<HuntingClub, Long> getRepository() {
        return huntingClubRepository;
    }

    @Override
    protected void updateEntity(final HuntingClub club, final HuntingClubDTO dto) {
        if (club.isNew() || activeUserService.isModeratorOrAdmin()) {
            club.setNameFinnish(dto.getNameFI());
            club.setNameSwedish(dto.getNameSV());
        }

        if (club.isNew()) {
            club.updateLocation(dto.getGeoLocation(), gisQueryService);
        }

        club.setSubtype(dto.getSubtype());

        club.setClubPerson(null);
        club.setBusinessId(null);
        club.setAssociationRegistryNumber(null);

        if (dto.getSubtype() != null) {
            switch (dto.getSubtype()) {
                case PERSON:
                    if (activeUserService.isModeratorOrAdmin()) {
                        if (dto.getClubPerson() != null) {
                            club.setClubPerson(personRepository.getOne(dto.getClubPerson().getId()));
                        }
                    } else {
                        club.setClubPerson(activeUserService.requireActivePerson());
                    }
                    break;
                case BUSINESS:
                    if (dto.getBusinessId() != null) {
                        club.setBusinessId(new FinnishBusinessIdEntity(dto.getBusinessId()));
                    }
                    break;
                case RY:
                    club.setAssociationRegistryNumber(dto.getAssociationRegistryNumber());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown enum value" + dto.getSubtype());
            }
        }
    }

    @Override
    protected void afterCreate(final HuntingClub club, final HuntingClubDTO dto) {
        // needs id, create official code after flush
        club.setOfficialCode(Long.toString(HuntingClub.CREATED_CLUB_MIN_OFFICIAL_CODE + club.getId()));

        final Person person = resolvePerson(dto);
        final Occupation contactPerson = new Occupation(person, club, OccupationType.SEURAN_YHDYSHENKILO,
                ContactInfoShare.ALL_MEMBERS, 0);
        contactPerson.setBeginDate(OrganisationType.CLUB.getBeginDateForNewOccupation());
        occupationRepository.save(contactPerson);
    }

    private Person resolvePerson(final HuntingClubDTO dto) {
        if (activeUserService.isModeratorOrAdmin()) {
            // Moderator is creating for person, there should be exactly one personId availble
            final List<OccupationDTO> contactPersons = dto.getYhdyshenkilot();
            Preconditions.checkArgument(contactPersons.size() == 1);
            return personRepository.getOne(contactPersons.get(0).getPersonId());
        }
        return activeUserService.requireActivePerson();
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
                .sorted(OccupationSort.BY_CALL_ORDER.thenComparing(OccupationSort.BY_LAST_NAME).thenComparing(OccupationSort.BY_BYNAME))
                .collect(toList());

        return clubOccupationDTOTransformer.apply(occupations);
    }

    @Transactional
    public GeoLocation updateLocation(final long clubId, final GeoLocation geoLocation) {
        final HuntingClub club = requireEntity(clubId, EntityPermission.UPDATE);
        club.updateLocation(geoLocation, gisQueryService);
        return club.getGeoLocation();
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional
    public void updateActive(final long clubId, final boolean active) {
        final HuntingClub club = huntingClubRepository.getOne(clubId);
        club.setActive(active);
    }
}
