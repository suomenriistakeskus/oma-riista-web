package fi.riista.feature.organization.calendar;

import fi.riista.util.DtoUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class VenueSearchFeature {
    @Resource
    private VenueRepository venueRepository;

    @Transactional(readOnly = true)
    public Page<VenueDTO> searchVenue(String searchTerm, Pageable page) {
        final Page<Venue> venues = venueRepository.searchByName(searchTerm, page);
        return DtoUtil.toDTO(venues, page, venue -> VenueDTO.create(venue, venue.getAddress()));
    }
}
