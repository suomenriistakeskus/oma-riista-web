package fi.riista.feature.organization.rhy;

import fi.riista.feature.SimpleAbstractCrudFeature;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitFeature;
import fi.riista.feature.huntingclub.permit.MooselikeHuntingYearDTO;
import fi.riista.feature.huntingclub.permit.MooselikePermitListingDTO;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.address.AddressRepository;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.security.EntityPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Component
public class RiistanhoitoyhdistysCrudFeature
        extends SimpleAbstractCrudFeature<Long, Riistanhoitoyhdistys, RiistanhoitoyhdistysDTO> {

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private AddressRepository addressRepository;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private HuntingClubPermitFeature huntingClubPermitFeature;

    @Override
    protected JpaRepository<Riistanhoitoyhdistys, Long> getRepository() {
        return riistanhoitoyhdistysRepository;
    }

    @Override
    protected void updateEntity(final Riistanhoitoyhdistys rhy, final RiistanhoitoyhdistysDTO dto) {
        rhy.setNameFinnish(dto.getNameFI());
        rhy.setNameSwedish(dto.getNameSV());

        if (dto.isHasOwnPhoneNumber()) {
            rhy.setPhoneNumber(dto.getPhoneNumber());
        } else {
            rhy.setPhoneNumber(null);
        }

        if (dto.isHasOwnEmail()) {
            rhy.setEmail(dto.getEmail());
        } else {
            rhy.setEmail(null);
        }

        if (dto.isHasOwnAddress()) {
            createAddressIfNotExists(rhy);
            Address a = rhy.getAddress();
            copyAddress(a, dto.getAddress());
        } else {
            // DTO says RHY doesn't have own address, so we remove it
            if (rhy.getAddress() != null) {
                addressRepository.delete(rhy.getAddress());
                rhy.setAddress(null);
            }
        }
    }

    private void createAddressIfNotExists(Riistanhoitoyhdistys rhy) {
        if (rhy.getAddress() == null) {
            Address a = new Address();
            rhy.setAddress(a);
            addressRepository.save(a);
        }
    }

    private static void copyAddress(Address entity, AddressDTO dto) {
        entity.setCountry(dto.getCountry());
        entity.setCity(dto.getCity());
        entity.setPostalCode(dto.getPostalCode());
        entity.setStreetAddress(dto.getStreetAddress());
    }

    @Override
    protected Function<Riistanhoitoyhdistys, RiistanhoitoyhdistysDTO> entityToDTOFunction() {
        return rhy -> {
            final RiistanhoitoyhdistysDTO dto = RiistanhoitoyhdistysDTO.create(rhy);
            final boolean editable = activeUserService.checkHasPermission(rhy, EntityPermission.UPDATE);
            dto.setEditable(editable);

            final Person coordinator = findCoordinator(rhy);
            if (coordinator != null) {
                if (!dto.isHasOwnAddress()) {
                    Address publicAddress = coordinator.getAddress();
                    dto.setAddress(AddressDTO.from(publicAddress));
                }
                if (!dto.isHasOwnEmail()) {
                    dto.setEmail(coordinator.getEmail());
                }
                if (!dto.isHasOwnPhoneNumber()) {
                    dto.setPhoneNumber(coordinator.getPhoneNumber());
                }
            }
            return dto;
        };
    }

    private Person findCoordinator(Riistanhoitoyhdistys rhy) {
        List<Occupation> valids = occupationRepository.findActiveByOrganisationAndOccupationType(rhy, OccupationType.TOIMINNANOHJAAJA);
        if (!valids.isEmpty()) {
            return valids.get(0).getPerson();
        }

        return null;
    }

    @Transactional(readOnly = true)
    public List<MooselikePermitListingDTO> listPermits(long rhyId, int year, int officialCodeMoose, Locale locale) {
        userAuthorizationHelper.assertCoordinatorOrModerator(rhyId);

        return harvestPermitRepository.listRhyPermitsByHuntingYearAndSpecies(rhyId, year, officialCodeMoose).stream()
                .map(p -> {
                    MooselikePermitListingDTO dto = huntingClubPermitFeature.getPermitListingDTOWithoutAuthorization(p, officialCodeMoose, null);
                    dto.setCurrentlyViewedRhyIsRelated(rhyId != p.getRhy().getId());
                    return dto;
                })
                .sorted(comparing(MooselikePermitListingDTO::isCurrentlyViewedRhyIsRelated)
                        .thenComparing(comparing(p -> p.getPermitHolder().getNameLocalisation().getAnyTranslation(locale))))
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public List<MooselikeHuntingYearDTO> listMooselikeHuntingYears(long rhyId) {
        userAuthorizationHelper.assertCoordinatorOrModerator(rhyId);

        return harvestPermitRepository.listRhyMooselikeHuntingYears(rhyId);
    }
}
