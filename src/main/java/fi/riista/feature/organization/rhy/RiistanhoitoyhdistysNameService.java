package fi.riista.feature.organization.rhy;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.organization.QRiistakeskuksenAlue;
import fi.riista.util.F;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fi.riista.feature.organization.rhy.MergedRhyMapping.getOfficialCodesOfRhysNotExistingAtYear;
import static java.util.stream.Collectors.toList;

@Service
public class RiistanhoitoyhdistysNameService {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Cacheable(value = "rhyNameIndex")
    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public Map<Long, RiistanhoitoyhdistysNameDTO> getNameIndex() {
        return F.index(getRhyNames(Optional.empty()), RiistanhoitoyhdistysNameDTO::getRhyId);
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public List<RiistanhoitoyhdistysNameDTO> getRhyNames(final int year) {
        return getRhyNames(Optional.of(year));
    }

    private List<RiistanhoitoyhdistysNameDTO> getRhyNames(final Optional<Integer> yearOption) {
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        final QRiistakeskuksenAlue RKA = QRiistakeskuksenAlue.riistakeskuksenAlue;

        final BooleanExpression rhyCodeExclusionPredicate = yearOption
                .map(year -> RHY.officialCode.notIn(getOfficialCodesOfRhysNotExistingAtYear(year)))
                .orElse(null);

        return jpqlQueryFactory
                .select(RHY.id, RHY.officialCode, RHY.nameFinnish, RHY.nameSwedish,
                        RKA.id, RKA.officialCode, RKA.nameFinnish, RKA.nameSwedish)
                .from(RHY)
                .join(RHY.parentOrganisation, RKA._super)
                .where(rhyCodeExclusionPredicate)
                .fetch()
                .stream()
                .map(tuple -> new RiistanhoitoyhdistysNameDTO(
                        tuple.get(RHY.id), tuple.get(RKA.id),
                        tuple.get(RHY.officialCode), tuple.get(RKA.officialCode),
                        tuple.get(RHY.nameFinnish), tuple.get(RHY.nameSwedish),
                        tuple.get(RKA.nameFinnish), tuple.get(RKA.nameSwedish)))
                .collect(toList());
    }
}
