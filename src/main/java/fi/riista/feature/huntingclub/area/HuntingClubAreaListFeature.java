package fi.riista.feature.huntingclub.area;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Component
public class HuntingClubAreaListFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HuntingClubAreaRepository huntingClubAreaRepository;

    @Resource
    private HuntingClubAreaDTOTransformer huntingClubAreaDTOTransformer;

    @Transactional(readOnly = true)
    public List<HuntingClubAreaDTO> listByClubAndYear(final long clubId,
                                                      final Integer year,
                                                      final boolean activeOnly,
                                                      final boolean includeEmpty) {
        final HuntingClub club = requireEntityService.requireHuntingClub(clubId, EntityPermission.READ);
        return huntingClubAreaDTOTransformer.apply(huntingClubAreaRepository.findByClubAndYear(club, year, activeOnly, includeEmpty));
    }

    @Transactional(readOnly = true)
    public List<Integer> listHuntingYears(Long clubId) {
        final HuntingClub club = requireEntityService.requireHuntingClub(clubId, EntityPermission.READ);

        return huntingClubAreaRepository.listHuntingYears(club);
    }
}
