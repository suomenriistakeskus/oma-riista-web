package fi.riista.feature.gis.verotuslohko;

import com.google.common.collect.ImmutableList;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQueryFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

public class GISVerotusLohkoRepositoryImpl implements GISVerotusLohkoRepositoryCustom {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<GISVerotusLohkoDTO> findWithoutGeometry(final int huntingYear, final Collection<String> officialCodes) {
        if (officialCodes.isEmpty()){
            return ImmutableList.of();
        }

        final QGISVerotusLohko LOHKO = QGISVerotusLohko.gISVerotusLohko;
        return jpqlQueryFactory.select(createDTOProjection(LOHKO)).from(LOHKO)
                .where(LOHKO.huntingYear.eq(huntingYear),
                        LOHKO.officialCode.in(officialCodes))
                .fetch();
    }

    @Nonnull
    private static ConstructorExpression<GISVerotusLohkoDTO> createDTOProjection(final QGISVerotusLohko LOHKO) {
        return Projections.constructor(GISVerotusLohkoDTO.class, LOHKO.officialCode, LOHKO.huntingYear, LOHKO.name);
    }

}
