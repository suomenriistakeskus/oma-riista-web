package fi.riista.feature.organization;

import fi.riista.feature.SimpleAbstractCrudFeature;
import fi.riista.feature.organization.calendar.CalendarEventDTO;
import fi.riista.feature.organization.calendar.VenueDTO;
import fi.riista.feature.organization.calendar.CalendarEventRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Function;

@Component
public class OrganisationCrudFeature extends SimpleAbstractCrudFeature<Long, Organisation, OrganisationDTO> {

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private CalendarEventRepository calendarEventRepository;

    @Override
    protected JpaRepository<Organisation, Long> getRepository() {
        return organisationRepository;
    }

    @Override
    protected void updateEntity(final Organisation org, final OrganisationDTO dto) {
    }

    @Transactional(readOnly = true)
    public List<CalendarEventDTO> listEvents(final Long id) {
        final Organisation org = requireEntity(id, EntityPermission.READ);
        return F.mapNonNullsToList(calendarEventRepository.findByOrganisation(org), CalendarEventDTO::create);
    }

    @Transactional(readOnly = true)
    public List<VenueDTO> listVenues(final Long id) {
        final Organisation org = organisationRepository.getOneFetchingVenues(id);
        activeUserService.assertHasPermission(org, EntityPermission.READ);
        return F.mapNonNullsToList(org.getVenues(), VenueDTO::create);
    }

    @Override
    protected Function<Organisation, OrganisationDTO> entityToDTOFunction() {
        return OrganisationDTO::create;
    }
}
