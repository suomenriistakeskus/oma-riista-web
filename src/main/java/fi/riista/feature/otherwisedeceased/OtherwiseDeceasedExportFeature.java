package fi.riista.feature.otherwisedeceased;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.integration.common.export.otherwisedeceased.ODA_DeathCauseEnum;
import fi.riista.integration.common.export.otherwisedeceased.ODA_DeceasedAnimal;
import fi.riista.integration.common.export.otherwisedeceased.ODA_DeceasedAnimals;
import fi.riista.integration.common.export.otherwisedeceased.ODA_GameAgeEnum;
import fi.riista.integration.common.export.otherwisedeceased.ODA_GameGenderEnum;
import fi.riista.integration.common.export.otherwisedeceased.ODA_GeoLocation;
import fi.riista.integration.common.export.otherwisedeceased.ObjectFactory;
import fi.riista.util.DateUtil;
import fi.riista.util.JaxbUtils;
import org.joda.time.DateTime;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.otherwisedeceased.OtherwiseDeceasedCause.OTHER;
import static fi.riista.feature.otherwisedeceased.OtherwiseDeceasedCause.UNDER_INVESTIGATION;
import static java.util.stream.Collectors.toList;

@Service
public class OtherwiseDeceasedExportFeature {

    @Resource
    private OtherwiseDeceasedRepository repository;

    @Resource(name = "otherwiseDeceasedExportJaxbMarshaller")
    private Jaxb2Marshaller jaxb2Marshaller;

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_OTHERWISE_DECEASED')")
    public ODA_DeceasedAnimals export(final int year) {
        final DateTime begin = DateUtil.beginOfCalendarYear(year);
        final DateTime end = DateUtil.beginOfCalendarYear(year).plusYears(1).minusMillis(1);
        final List<OtherwiseDeceased> entities = repository.findAllByPointOfTimeBetween(begin, end).stream()
                .filter(e -> e.isRejected() == false)
                .filter(e -> e.getCause() != UNDER_INVESTIGATION)
                .collect(toList());
        final ObjectFactory f = new ObjectFactory();
        return createDeceasedAnimalsDto(entities, f);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_OTHERWISE_DECEASED')")
    public String exportXml(final int year) {
        return JaxbUtils.marshalToString(export(year), jaxb2Marshaller);
    }

    private ODA_DeceasedAnimals createDeceasedAnimalsDto(final List<OtherwiseDeceased> entities, final ObjectFactory f) {
        return f.createODA_DeceasedAnimals()
                .withDeceasedAnimal(
                        entities.stream()
                                .map(e -> createDeceasedAnimalDto(e, f))
                                .collect(toList()));
    }

    private ODA_DeceasedAnimal createDeceasedAnimalDto(final OtherwiseDeceased entity, final ObjectFactory f) {
        return f.createODA_DeceasedAnimal()
                .withDeceasedAnimalId(entity.getId())
                .withGameSpeciesCode(entity.getSpecies().getOfficialCode())
                .withAge(ODA_GameAgeEnum.fromValue(entity.getAge().name()))
                .withGender(ODA_GameGenderEnum.fromValue(entity.getGender().name()))
                .withPointOfTime(entity.getPointOfTime().toLocalDateTime())
                .withCause(ODA_DeathCauseEnum.fromValue(entity.getCause().name()))
                .withCauseOther(entity.getCause() == OTHER ? entity.getCauseDescription() : null)
                .withDescription(entity.getDescription())
                .withGeoLocation(createGeoLocationDto(entity.getGeoLocation(), entity.getNoExactLocation(), f));
    }

    private ODA_GeoLocation createGeoLocationDto(final GeoLocation geoLocation,
                                                 final boolean noExactLocation,
                                                 final ObjectFactory f) {
        return f.createODA_GeoLocation()
                .withLatitude(geoLocation.getLatitude())
                .withLongitude(geoLocation.getLongitude())
                .withNoExactLocation(noExactLocation);
    }
}
