package fi.riista.feature.organization.rhy;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.address.AddressRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

@Component
public class RiistanhoitoyhdistysCrudFeature
        extends AbstractCrudFeature<Long, Riistanhoitoyhdistys, RiistanhoitoyhdistysDTO> {

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Resource
    private RiistanhoitoyhdistysCoordinatorService riistanhoitoyhdistysCoordinatorService;

    @Resource
    private AddressRepository addressRepository;

    @Override
    protected JpaRepository<Riistanhoitoyhdistys, Long> getRepository() {
        return riistanhoitoyhdistysRepository;
    }

    @Transactional(readOnly = true)
    public OrganisationNameDTO getPublicInfo(final long id) {
        return OrganisationNameDTO.createWithOfficialCode(riistanhoitoyhdistysRepository.getOne(id));
    }

    @Override
    protected RiistanhoitoyhdistysDTO toDTO(@Nonnull final Riistanhoitoyhdistys rhy) {
        final RiistanhoitoyhdistysDTO dto = RiistanhoitoyhdistysDTO.create(rhy);
        dto.setEditable(activeUserService.checkHasPermission(rhy, EntityPermission.UPDATE));

        final Person coordinator = riistanhoitoyhdistysCoordinatorService.findCoordinator(rhy);
        if (coordinator != null) {
            if (!dto.isHasOwnAddress()) {
                // Coordinator's address is public.
                dto.setAddress(AddressDTO.from(coordinator.getAddress()));
            }
            if (!dto.isHasOwnEmail()) {
                dto.setEmail(coordinator.getEmail());
            }
            if (!dto.isHasOwnPhoneNumber()) {
                dto.setPhoneNumber(coordinator.getPhoneNumber());
            }
        }
        return dto;
    }

    @Override
    protected void updateEntity(final Riistanhoitoyhdistys rhy, final RiistanhoitoyhdistysDTO dto) {
        rhy.setNameFinnish(dto.getNameFI());
        rhy.setNameSwedish(dto.getNameSV());

        rhy.setPhoneNumber(dto.isHasOwnPhoneNumber() ? dto.getPhoneNumber() : null);
        rhy.setEmail(dto.isHasOwnEmail() ? dto.getEmail() : null);

        if (dto.isHasOwnAddress()) {
            createAddressIfNotExists(rhy);
            copyAddress(rhy.getAddress(), dto.getAddress());
        } else {
            // DTO says RHY doesn't have own address, so we remove it
            if (rhy.getAddress() != null) {
                addressRepository.delete(rhy.getAddress());
                rhy.setAddress(null);
            }
        }
    }

    private void createAddressIfNotExists(final Riistanhoitoyhdistys rhy) {
        if (rhy.getAddress() == null) {
            final Address a = new Address();
            rhy.setAddress(a);
            addressRepository.save(a);
        }
    }

    private static void copyAddress(final Address entity, final AddressDTO dto) {
        entity.setCountry(dto.getCountry());
        entity.setCity(dto.getCity());
        entity.setPostalCode(dto.getPostalCode());
        entity.setStreetAddress(dto.getStreetAddress());
    }
}
