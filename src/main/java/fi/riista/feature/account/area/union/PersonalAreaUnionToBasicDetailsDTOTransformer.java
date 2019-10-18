package fi.riista.feature.account.area.union;

import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.HarvestPermitAreaRepository;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.CriteriaUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.function.Function;

@Component
public class PersonalAreaUnionToBasicDetailsDTOTransformer extends ListTransformer<PersonalAreaUnion,
        PersonalAreaUnionBasicDetailsDTO> {

    @Resource
    private HarvestPermitAreaRepository harvestPermitAreaRepository;

    @Nonnull
    private Function<PersonalAreaUnion, HarvestPermitArea> getAreaUnionToPermitAreaMapping(final Iterable<PersonalAreaUnion> items) {
        return CriteriaUtils.singleQueryFunction(items, PersonalAreaUnion::getHarvestPermitArea,
                harvestPermitAreaRepository, true);
    }

    @Nonnull
    @Override
    protected List<PersonalAreaUnionBasicDetailsDTO> transform(@Nonnull final List<PersonalAreaUnion> list) {
        final Function<PersonalAreaUnion, HarvestPermitArea> areaUnionToPermitAreaMapping =
                getAreaUnionToPermitAreaMapping(list);

        return F.mapNonNullsToList(list, accountArea -> {
            final HarvestPermitArea harvestPermitArea = areaUnionToPermitAreaMapping.apply(accountArea);

            return new PersonalAreaUnionBasicDetailsDTO(
                    accountArea.getId(),
                    accountArea.getName(),
                    harvestPermitArea.getExternalId());
        });

    }

}
