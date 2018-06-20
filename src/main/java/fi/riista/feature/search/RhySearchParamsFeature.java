package fi.riista.feature.search;

import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLTemplates;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gis.hta.QGISHirvitalousalue;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.QRiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.security.EntityPermission;
import fi.riista.sql.SQHta;
import fi.riista.sql.SQOrganisation;
import fi.riista.util.GISUtils;
import fi.riista.util.LocalisedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Component
public class RhySearchParamsFeature {

    public enum RhySearchOrgType {
        RHY, RKA, HTA
    }

    public static class RhySearchOrgList {
        public final RhySearchOrgType type;
        public final List<RhySearchOrgList.Org> organisations;

        RhySearchOrgList(RhySearchOrgType type, List<RhySearchOrgList.Org> organisations) {
            this.type = type;
            this.organisations = organisations;
        }

        private static class Org {
            public final String officialCode;
            public final String name;
            public final boolean selected;

            Org(final String officialCode, final String name, final boolean selected) {
                this.officialCode = officialCode;
                this.name = name;
                this.selected = selected;
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(RhySearchParamsFeature.class);

    @Resource
    private SQLTemplates sqlTemplates;

    @PersistenceContext
    private EntityManager em;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private RequireEntityService requireEntityService;

    @Transactional(readOnly = true)
    public List<RhySearchOrgList> listOrganisations(final Long rhyId, final Locale locale) {
        // no need to authorize
        final Optional<Riistanhoitoyhdistys> viewedRhy = Optional.ofNullable(rhyId)
                .map(id -> requireEntityService.requireRiistanhoitoyhdistys(id, EntityPermission.NONE));

        final String viewedRhyHtaOfficialCode = rhyId != null ? findHta(rhyId) : null;

        final QGISHirvitalousalue HTA = QGISHirvitalousalue.gISHirvitalousalue;
        final List<RhySearchOrgList.Org> htas = listOrgs(locale, viewedRhyHtaOfficialCode, HTA, HTA.number, HTA.nameFinnish, HTA.nameSwedish);

        final QRiistakeskuksenAlue RKA = QRiistakeskuksenAlue.riistakeskuksenAlue;
        final List<RhySearchOrgList.Org> rkas = listOrgs(locale, getParentOfficialCode(viewedRhy), RKA, RKA.officialCode, RKA.nameFinnish, RKA.nameSwedish);

        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        final List<RhySearchOrgList.Org> rhys = listOrgs(locale, getOfficialCode(viewedRhy), RHY, RHY.officialCode, RHY.nameFinnish, RHY.nameSwedish);

        return Arrays.asList(
                new RhySearchOrgList(RhySearchOrgType.RHY, rhys),
                new RhySearchOrgList(RhySearchOrgType.HTA, htas),
                new RhySearchOrgList(RhySearchOrgType.RKA, rkas)
        );
    }

    private String getParentOfficialCode(final Optional<Riistanhoitoyhdistys> viewedRhy) {
        return viewedRhy.map(Riistanhoitoyhdistys::getParentOrganisation).map(Organisation::getOfficialCode).orElse(null);
    }

    private String getOfficialCode(final Optional<Riistanhoitoyhdistys> viewedRhy) {
        return viewedRhy.map(Riistanhoitoyhdistys::getOfficialCode).orElse(null);
    }

    private String findHta(final long rhyId) {
        final SQOrganisation ORG = SQOrganisation.organisation;
        final SQHta HTA = SQHta.hta;
        final List<String> htas = createNativeQuery().select(HTA.numero)
                .from(ORG)
                .join(HTA).on(HTA.geom.intersects(GISUtils.createPointWithDefaultSRID(ORG.longitude, ORG.latitude)))
                .where(ORG.organisationId.eq(rhyId))
                .where(ORG.latitude.isNotNull())
                .where(ORG.longitude.isNotNull())
                .fetch();

        if (htas.size() == 1) {
            return htas.get(0);
        }
        LOG.warn("Could not resolve HTA for rhyId:" + rhyId + ", htas:" + htas);
        return htas.isEmpty() ? null : htas.get(0);
    }

    private List<RhySearchOrgList.Org> listOrgs(final Locale locale,
                                                final String viewedOfficialCode,
                                                final EntityPathBase<?> table,
                                                final StringPath number,
                                                final StringPath nameFinnish,
                                                final StringPath nameSwedish) {
        return jpqlQueryFactory.select(number, nameFinnish, nameSwedish)
                .from(table)
                .fetch()
                .stream()
                .map(t -> {
                    final String name = LocalisedString.of(t.get(nameFinnish), t.get(nameSwedish))
                            .getAnyTranslation(locale);
                    final String officialCode = t.get(number);
                    return new RhySearchOrgList.Org(officialCode, name, Objects.equals(officialCode, viewedOfficialCode));
                })
                .sorted(comparing(o -> o.name))
                .collect(toList());
    }

    private <T> JPASQLQuery<T> createNativeQuery() {
        return new JPASQLQuery<>(em, sqlTemplates);
    }

}
