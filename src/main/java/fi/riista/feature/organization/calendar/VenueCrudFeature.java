package fi.riista.feature.organization.calendar;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.address.AddressRepository;
import fi.riista.security.EntityPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

@Component
public class VenueCrudFeature extends AbstractCrudFeature<Long, Venue, VenueDTO> {

    @Resource
    private VenueRepository venueRepository;

    @Resource
    private AddressRepository addressRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Override
    protected JpaRepository<Venue, Long> getRepository() {
        return venueRepository;
    }

    @Override
    protected VenueDTO toDTO(@Nonnull final Venue entity) {
        return VenueDTO.create(entity, entity.getAddress());
    }

    @Override
    protected void updateEntity(final Venue entity, final VenueDTO dto) {
        entity.setName(dto.getName());
        entity.setInfo(dto.getInfo());
        createAddressIfNecessary(entity);
        if (dto.getAddress() != null) {
            copyAddress(entity.getAddress(), dto.getAddress());
        }
    }

    private void createAddressIfNecessary(final Venue entity) {
        if (entity.getAddress() == null) {
            Address address = new Address();
            addressRepository.save(address);
            entity.setAddress(address);
        }
    }

    private static void copyAddress(final Address to, final VenueAddressDTO from) {
        to.setStreetAddress(from.getStreetAddress());
        to.setPostalCode(from.getPostalCode());
        to.setCity(from.getCity());
    }

    @Transactional
    public VenueDTO createForOrganisation(final long organisationId, final VenueDTO dto) {
        final VenueDTO createdDto = create(dto);

        final Venue venue = requireEntityService.requireVenue(createdDto.getId(), EntityPermission.NONE);
        final Organisation organisation = requireEntityService.requireOrganisation(organisationId, EntityPermission.NONE);

        organisation.addVenue(venue);
        organisationRepository.save(organisation);

        return createdDto;
    }

    @Transactional
    public void attachVenue(final long organisationId, final long venueId) {
        final Venue venue = requireEntityService.requireVenue(venueId, EntityPermission.UPDATE);
        final Organisation organisation = requireEntityService.requireOrganisation(organisationId, EntityPermission.NONE);

        organisation.addVenue(venue);
        organisationRepository.save(organisation);
    }

    @Transactional
    public void detachVenue(final long organisationId, final long venueId) {
        final Venue venue = requireEntityService.requireVenue(venueId, EntityPermission.UPDATE);
        final Organisation organisation = requireEntityService.requireOrganisation(organisationId, EntityPermission.NONE);

        organisation.removeVenue(venue);
        organisationRepository.save(organisation);
    }

}
