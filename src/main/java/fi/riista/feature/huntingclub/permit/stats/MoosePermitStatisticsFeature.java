package fi.riista.feature.huntingclub.permit.stats;

import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLTemplates;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gis.hta.QGISHirvitalousalue;
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

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Component
public class MoosePermitStatisticsFeature {

    private static final Logger LOG = LoggerFactory.getLogger(MoosePermitStatisticsFeature.class);

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private SQLTemplates sqlTemplates;

    @PersistenceContext
    private EntityManager em;

    @Resource
    private MoosePermitStatisticsService moosePermitStatisticsService;

    private <T> JPASQLQuery<T> createNativeQuery() {
        return new JPASQLQuery<>(em, sqlTemplates);
    }

    @Transactional(readOnly = true)
    public List<OrgList> listOrganisations(final long rhyId, final Locale locale) {
        final Riistanhoitoyhdistys viewedRhy = requireEntityService.requireRiistanhoitoyhdistys(rhyId, EntityPermission.READ);

        final String viewedRhyHtaOfficialCode = findHta(rhyId);

        final QGISHirvitalousalue hta = QGISHirvitalousalue.gISHirvitalousalue;
        final List<OrgList.Org> htas = listOrgs(locale, viewedRhyHtaOfficialCode, hta, hta.number, hta.nameFinnish, hta.nameSwedish);

        final QRiistakeskuksenAlue rka = QRiistakeskuksenAlue.riistakeskuksenAlue;
        final List<OrgList.Org> rkas = listOrgs(locale, viewedRhy.getParentOrganisation().getOfficialCode(), rka, rka.officialCode, rka.nameFinnish, rka.nameSwedish);

        final QRiistanhoitoyhdistys rhy = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        final List<OrgList.Org> rhys = listOrgs(locale, viewedRhy.getOfficialCode(), rhy, rhy.officialCode, rhy.nameFinnish, rhy.nameSwedish);

        return Arrays.asList(new OrgList(OrgType.RHY, rhys), new OrgList(OrgType.HTA, htas), new OrgList(OrgType.RKA, rkas));
    }

    private List<OrgList.Org> listOrgs(final Locale locale,
                                       final String viewedRhyHtaOfficialCode,
                                       final EntityPathBase<?> hta,
                                       final StringPath number,
                                       final StringPath nameFinnish,
                                       final StringPath nameSwedish) {
        return jpqlQueryFactory.select(number, nameFinnish, nameSwedish)
                .from(hta)
                .fetch()
                .stream()
                .map(t -> {
                    final String name = LocalisedString.of(t.get(nameFinnish), t.get(nameSwedish))
                            .getAnyTranslation(locale);
                    final String officialCode = t.get(number);
                    return new OrgList.Org(officialCode, name, Objects.equals(officialCode, viewedRhyHtaOfficialCode));
                })
                .sorted(comparing(o -> o.name))
                .collect(toList());
    }

    private String findHta(final long rhyId) {
        final SQOrganisation org = SQOrganisation.organisation;
        final SQHta hta = SQHta.hta;
        final List<String> htas = createNativeQuery().select(hta.numero)
                .from(org)
                .join(hta).on(hta.geom.intersects(GISUtils.createPointWithDefaultSRID(org.longitude, org.latitude)))
                .where(org.organisationId.eq(rhyId),
                        org.latitude.isNotNull(),
                        org.longitude.isNotNull())
                .fetch();

        if (htas.size() == 1) {
            return htas.get(0);
        }
        LOG.warn("Could not resolve HTA for rhyId:" + rhyId + ", htas:" + htas);
        return htas.isEmpty() ? null : htas.get(0);
    }

    public enum OrgType {
        RHY, RKA, HTA
    }

    public static class OrgList {
        public final OrgType type;
        public final List<Org> organisations;

        public OrgList(OrgType type, List<Org> organisations) {
            this.type = type;
            this.organisations = organisations;
        }

        static class Org {
            public final String officialCode;
            public final String name;
            public final boolean selected;

            public Org(final String officialCode, final String name, final boolean selected) {
                this.officialCode = officialCode;
                this.name = name;
                this.selected = selected;
            }
        }
    }

    @Transactional(readOnly = true)
    public List<MoosePermitStatisticsDTO> calculateByHolder(final long rhyId, final Locale locale, final int speciesCode,
                                                            final int huntingYear, final OrgType orgType, final String orgCode) {

        requireEntityService.requireRiistanhoitoyhdistys(rhyId, EntityPermission.READ);
        return moosePermitStatisticsService.calculateByHolder(locale, speciesCode, huntingYear, orgType, orgCode);
    }
}
