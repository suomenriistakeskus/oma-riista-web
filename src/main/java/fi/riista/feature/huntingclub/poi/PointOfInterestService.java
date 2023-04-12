package fi.riista.feature.huntingclub.poi;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;

@Component
public class PointOfInterestService {

    @Resource
    private HuntingClubRepository clubRepository;

    @Resource
    private PoiIdAllocationRepository idAllocationRepository;

    @Resource
    private PoiLocationGroupRepository poiRepository;

    @Resource
    private ActiveUserService activeUserService;

    // No authorization
    @Transactional(readOnly = true)
    public boolean canEdit(final long clubId) {
        return clubRepository.findById(clubId)
                .map(club -> {
                    if(activeUserService.checkHasPermission(club, EntityPermission.UPDATE)) {
                        return true;
                    }

                    final Person activePerson = activeUserService.requireActivePerson();
                    for (final Occupation o : activePerson.getClubSpecificOccupations()) {
                        if (o.getOccupationType().equals(RYHMAN_METSASTYKSENJOHTAJA)
                                && Objects.requireNonNull(o.getOrganisation().getParentOrganisation().getId()).equals(clubId)) {
                            return true;
                        }
                    }
                    return false;
                })
                .orElseThrow(() -> new NotFoundException("Club not found with id " + clubId));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public PoiIdAllocation getOrCreate(final HuntingClub club) {
        return idAllocationRepository.findByClub(club).stream()
                .findFirst()
                .orElseGet(() -> idAllocationRepository.save(new PoiIdAllocation(club)));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<PoiLocationGroup> listForClub(final HuntingClub club) {
        return idAllocationRepository.findByClub(club).stream()
                .findFirst()
                .map(poiRepository::findAllByPoiIdAllocation)
                .orElse(Collections.emptyList());
    }

}
