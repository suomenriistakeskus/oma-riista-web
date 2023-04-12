package fi.riista.feature.otherwisedeceased;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.repository.BaseRepositoryImpl;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.util.F;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static fi.riista.util.F.mapNullable;
import static java.util.Optional.ofNullable;

@Repository
public class OtherwiseDeceasedRepositoryImpl implements OtherwiseDeceasedRepositoryCustom {

    private NamedParameterJdbcOperations jdbcTemplate;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<Long> findReindeerAreaLocated(Collection<OtherwiseDeceased> otherwiseDeceasedCollection) {
        if (otherwiseDeceasedCollection.isEmpty()) {
            return Collections.emptyList();
        }

        final Set<Long> otherwiseDeceasedIds = F.getUniqueIds(otherwiseDeceasedCollection);

        final String sql = "SELECT deceased.otherwise_deceased_id " +
                "FROM otherwise_deceased deceased " +
                "JOIN harvest_area area " +
                "ON ST_Contains(area.geom, ST_SetSRID(ST_MakePoint(deceased.longitude, deceased.latitude), 3067)) " +
                "WHERE deceased.otherwise_deceased_id in (:otherwiseDeceasedIds) " +
                "AND area.type = 'PORONHOITOALUE'";

        return jdbcTemplate.query(sql,
                new MapSqlParameterSource("otherwiseDeceasedIds", otherwiseDeceasedIds),
                (rs, rowNum) -> rs.getLong("otherwise_deceased_id"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OtherwiseDeceased> search(final OtherwiseDeceasedFilterDTO filterDTO) {
        return getQuery(filterDTO).fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<OtherwiseDeceased> searchPage(final OtherwiseDeceasedFilterDTO filterDTO, final Pageable pageable) {

        return BaseRepositoryImpl.toSlice(
                getQuery(filterDTO)
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize() + 1)
                        .fetch(), pageable);
    }

    private JPQLQuery<OtherwiseDeceased> getQuery(final OtherwiseDeceasedFilterDTO filterDTO) {

        final QOtherwiseDeceased ENTITY = QOtherwiseDeceased.otherwiseDeceased;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;
        QOrganisation RKA = new QOrganisation("rka");
        QOrganisation RHY = new QOrganisation("rhy");

        BooleanExpression speciesPredicate =
                mapNullable(filterDTO.getGameSpeciesCode(), code -> SPECIES.officialCode.eq(code));

        // If rhy specified, use that as predicate, else try with rka
        BooleanExpression organisationPredicate = ofNullable(filterDTO.getRhyOfficialCode())
                .map(rhy -> RHY.officialCode.eq(rhy))
                .orElseGet(() -> mapNullable(filterDTO.getRkaOfficialCode(), rka -> RKA.officialCode.eq(rka)));

        BooleanExpression causePredicate = mapNullable(filterDTO.getCause(), cause -> ENTITY.cause.eq(cause));

        return jpqlQueryFactory.select(ENTITY)
                .from(ENTITY)
                .join(ENTITY.species, SPECIES)
                .join(ENTITY.rhy, RHY)
                .join(RHY.parentOrganisation, RKA)
                .where(ENTITY.pointOfTime.between(
                        filterDTO.getBeginDate().toDateTimeAtStartOfDay(),
                        filterDTO.getEndDate().plusDays(1).toDateTimeAtStartOfDay()))
                .where(speciesPredicate)
                .where(organisationPredicate)
                .where(causePredicate)
                .where(ENTITY.rejected.eq(filterDTO.isShowRejected()))
                .orderBy(ENTITY.pointOfTime.desc());
    }

}
