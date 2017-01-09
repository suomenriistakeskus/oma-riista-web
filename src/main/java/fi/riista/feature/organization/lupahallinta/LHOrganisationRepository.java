package fi.riista.feature.organization.lupahallinta;

import fi.riista.feature.common.repository.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LHOrganisationRepository extends BaseRepository<LHOrganisation, Long> {

    List<LHOrganisation> findByOfficialCode(String officialCode);

    @Query("select lh from LHOrganisation lh" +
            " where true = trgm_match(?1, lh.nameFinnish)" +
            " and trgm_dist(?1, lh.nameFinnish) < ?2" +
            " order by trgm_dist(?1, lh.nameFinnish)")
    List<LHOrganisation> findFuzzyFinnishName(String searchQuery, double maxDistance, Pageable page);

    @Query("select lh from LHOrganisation lh" +
            " where true = trgm_match(?1, lh.nameSwedish)" +
            " and trgm_dist(?1, lh.nameSwedish) < ?2" +
            " order by trgm_dist(?1, lh.nameSwedish)")
    List<LHOrganisation> findFuzzySwedishName(String searchQuery, double maxDistance, Pageable page);

}
