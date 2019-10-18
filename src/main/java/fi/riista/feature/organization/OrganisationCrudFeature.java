package fi.riista.feature.organization;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.organization.calendar.CalendarEventDTO;
import fi.riista.feature.organization.calendar.CalendarEventDTOTransformer;
import fi.riista.feature.organization.calendar.CalendarEventRepository;
import fi.riista.feature.organization.calendar.VenueDTO;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class OrganisationCrudFeature extends AbstractCrudFeature<Long, Organisation, OrganisationDTO> {

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private CalendarEventRepository calendarEventRepository;

    @Resource
    private CalendarEventDTOTransformer calendarEventDtoTransformer;

    @Override
    protected JpaRepository<Organisation, Long> getRepository() {
        return organisationRepository;
    }

    @Override
    protected OrganisationDTO toDTO(@Nonnull final Organisation entity) {
        return OrganisationDTO.create(entity);
    }

    @Override
    protected void updateEntity(final Organisation org, final OrganisationDTO dto) {
    }

    @Transactional(readOnly = true)
    public List<CalendarEventDTO> listEvents(final long organisationId) {
        final Organisation org = requireEntity(organisationId, EntityPermission.READ);
        return calendarEventDtoTransformer.apply(calendarEventRepository.findByOrganisation(org));
    }

    @Transactional(readOnly = true)
    public List<CalendarEventDTO> listEventsByYear(final long organisationId, final int year) {
        final Organisation org = requireEntity(organisationId, EntityPermission.READ);
        final Date startTime = DateUtil.toDateNullSafe(new LocalDate(year, 1, 1));
        final Date endTime = DateUtil.toDateNullSafe(new LocalDate(year, 12, 31));
        return calendarEventDtoTransformer.apply(calendarEventRepository.findByOrganisation(org, startTime, endTime));
    }

    @Transactional(readOnly = true)
    public List<VenueDTO> listVenues(final long organisationId) {
        final Organisation org = organisationRepository.getOneFetchingVenues(organisationId);
        activeUserService.assertHasPermission(org, EntityPermission.READ);
        return F.mapNonNullsToList(org.getVenues(), venue -> VenueDTO.create(venue, venue.getAddress()));
    }
}
