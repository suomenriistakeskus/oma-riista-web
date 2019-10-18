package fi.riista.feature.permit.application.statistics;

import com.google.common.collect.ImmutableSet;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.user.QSystemUser;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.QRiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.QPermitDecision;
import io.vavr.Tuple;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fi.riista.util.Collect.tuplesToMap;

@Component
public class HarvestPermitApplicationStatisticsFeature {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public List<HarvestPermitApplicationStatusTableDTO> statusTable(int year) {
        final Map<String, Map<String, Integer>> mooselike =
                calculateHKV(ImmutableSet.of(HarvestPermitCategory.MOOSELIKE), year);
        final Map<String, Map<String, Integer>> mooselikeAmendment =
                calculateHKV(ImmutableSet.of(HarvestPermitCategory.MOOSELIKE_NEW), year);
        final Map<String, Map<String, Integer>> bird = calculateHKV(ImmutableSet.of(HarvestPermitCategory.BIRD), year);
        final Map<String, Map<String, Integer>> carnivore =
                calculateHKV(HarvestPermitCategory.getLargeCarnivoreCategories(), year);

        final QRiistakeskuksenAlue RKA = QRiistakeskuksenAlue.riistakeskuksenAlue;

        return jpqlQueryFactory.selectFrom(RKA)
                .orderBy(RKA.officialCode.asc())
                .fetch()
                .stream()
                .map(rka -> {
                    final HarvestPermitApplicationStatusTableDTO dto = new HarvestPermitApplicationStatusTableDTO();
                    dto.setRka(OrganisationNameDTO.createWithOfficialCode(rka));

                    final Map<String, Map<String, Integer>> permitTypeToStatus = new HashMap<>();
                    permitTypeToStatus.put(HarvestPermitCategory.MOOSELIKE.name(),
                            mooselike.get(rka.getOfficialCode()));
                    permitTypeToStatus.put(HarvestPermitCategory.MOOSELIKE_NEW.name(),
                            mooselikeAmendment.get(rka.getOfficialCode()));
                    permitTypeToStatus.put(HarvestPermitCategory.BIRD.name(), bird.get(rka.getOfficialCode()));
                    permitTypeToStatus.put(HarvestPermitCategory.LARGE_CARNIVORE_CATEGORIES,
                            carnivore.get(rka.getOfficialCode()));
                    dto.setPermitCategoryToStatus(permitTypeToStatus);
                    return dto;
                }).collect(Collectors.toList());
    }

    private Map<String, Map<String, Integer>> calculateHKV(final Collection<HarvestPermitCategory> categories,
                                                           int year) {
        final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;
        final QSystemUser HANDLER = QSystemUser.systemUser;

        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        final QOrganisation RKA = new QOrganisation("rka");

        final NumberExpression<Integer> h = countingCase(HANDLER.isNull(), "H");
        final NumberExpression<Integer> k =
                countingCase(HANDLER.isNotNull().and(DECISION.status.eq(PermitDecision.Status.DRAFT)), "K");
        final NumberExpression<Integer> v =
                countingCase(HANDLER.isNotNull().and(DECISION.status.ne(PermitDecision.Status.DRAFT)), "V");

        return jpqlQueryFactory.select(RKA.officialCode, h, k, v)
                .from(APPLICATION)
                .leftJoin(APPLICATION.decision, DECISION)
                .leftJoin(DECISION.handler, HANDLER)
                .join(APPLICATION.rhy, RHY)
                .join(RHY.parentOrganisation, RKA)
                .where(APPLICATION.applicationYear.eq(year))
                .where(APPLICATION.harvestPermitCategory.in(categories))
                .where(APPLICATION.status.in(HarvestPermitApplication.Status.ACTIVE,
                        HarvestPermitApplication.Status.AMENDING))
                .groupBy(RKA.id, RKA.officialCode, RKA.nameFinnish, RKA.nameSwedish)
                .orderBy(RKA.officialCode.asc())
                .fetch().stream().map(t -> {
                    final Map<String, Integer> hkv = new HashMap<>();
                    hkv.put("H", t.get(h));
                    hkv.put("K", t.get(k));
                    hkv.put("V", t.get(v));
                    final String rkaCode = t.get(RKA.officialCode);
                    return Tuple.of(rkaCode, hkv);
                }).collect(tuplesToMap());
    }

    private static NumberExpression<Integer> countingCase(final BooleanExpression predicate, final String alias) {
        return new CaseBuilder().when(predicate).then(1).otherwise(0).sum().as(alias);
    }
}
