package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.error.RevisionConflictException;
import fi.riista.feature.gamediary.GameSpeciesNotFoundException;
import fi.riista.feature.gamediary.OutOfBoundsSpecimenAmountException;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestChangeReasonRequiredException;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestHuntingDayChangeForbiddenException;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestPermitChangeForbiddenException;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestReportingTypeChangeForbiddenException;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestSeasonChangeForbiddenException;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestSpeciesChangeForbiddenException;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestSpeciesRequiresPermitException;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenFieldName;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenValidationException;
import fi.riista.feature.gamediary.harvest.specimen.MultipleSpecimenNotAllowedException;
import fi.riista.feature.gis.RhyNotResolvableByGeoLocationException;
import fi.riista.feature.harvestpermit.HarvestPermitNotFoundException;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountNotFound;
import fi.riista.feature.organization.person.PersonNotFoundException;
import fi.riista.util.LocalisedString;
import io.sentry.Sentry;
import io.sentry.SentryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class HarvestExceptionMapper {
    private static final Logger LOG = LoggerFactory.getLogger(HarvestExceptionMapper.class);

    @Nonnull
    private static HarvestValidationFailureDTO singleErrorMessage(final LocalisedString msg) {
        return new HarvestValidationFailureDTO(Collections.singletonList(HarvestValidationFailureDTO.message(msg)));
    }

    @Nonnull
    private static ResponseEntity<HarvestValidationFailureDTO> badRequest(final HarvestValidationFailureDTO dto) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
    }

    @Nonnull
    public ResponseEntity<HarvestValidationFailureDTO> handleException(final RuntimeException e) {
        final ResponseEntity<HarvestValidationFailureDTO> response = mapExceptionInternal(e);
        final SentryClient sentry = Sentry.getStoredClient();

        if (sentry != null) {
            sentry.sendException(e);
        }

        if (response != null) {
            LOG.error("Harvest validation failed with exception " + e.getClass().getSimpleName() + " : " + e.getMessage());

            return response;
        }

        LOG.error("Harvest update failed with unknown exception", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    private static ResponseEntity<HarvestValidationFailureDTO> mapExceptionInternal(final RuntimeException e) {
        if (e instanceof RevisionConflictException) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        } else if (e instanceof HarvestSpecVersionNotSupportedException) {
            return badRequest(mapException((HarvestSpecVersionNotSupportedException) e));

        } else if (e instanceof HarvestSpecimenValidationException) {
            return badRequest(mapException((HarvestSpecimenValidationException) e));

        } else if (e instanceof HarvestFieldValidationException) {
            return badRequest(mapException((HarvestFieldValidationException) e));

        } else if (e instanceof OutOfBoundsSpecimenAmountException) {
            return badRequest(mapException((OutOfBoundsSpecimenAmountException) e));

        } else if (e instanceof HarvestSpeciesRequiresPermitException) {
            return badRequest(mapException((HarvestSpeciesRequiresPermitException) e));

        } else if (e instanceof HarvestPermitNotFoundException) {
            return badRequest(mapException((HarvestPermitNotFoundException) e));

        } else if (e instanceof HarvestPermitSpeciesAmountNotFound) {
            return badRequest(mapException((HarvestPermitSpeciesAmountNotFound) e));

        } else if (e instanceof PersonNotFoundException) {
            return badRequest(mapException((PersonNotFoundException) e));

        } else if (e instanceof GameSpeciesNotFoundException) {
            return badRequest(mapException((GameSpeciesNotFoundException) e));

        } else if (e instanceof RhyNotResolvableByGeoLocationException) {
            return badRequest(mapException((RhyNotResolvableByGeoLocationException) e));

        } else if (e instanceof MultipleSpecimenNotAllowedException) {
            return badRequest(singleErrorMessage(LocalisedString.of(
                    "Valitulle eläinlajille ei voi syöttää useita yksilöitä",
                    "Flera individer kan inte föras in i valda djurarter",
                    "Multiple specimen not allowed for species")));

        } else if (e instanceof HarvestSpeciesChangeForbiddenException) {
            return badRequest(singleErrorMessage(LocalisedString.of(
                    "Eläinlajia ei voi vaihtaa",
                    "Djurarter kan inte ändras",
                    "Species can not be changed")));

        } else if (e instanceof HarvestSeasonChangeForbiddenException) {
            return badRequest(singleErrorMessage(LocalisedString.of(
                    "Metsästyskautta ei voi vaihtaa",
                    "Jaktperioden kan inte ändras",
                    "Harvest season can not be changed")));

        } else if (e instanceof HarvestReportingTypeChangeForbiddenException) {
            return badRequest(singleErrorMessage(LocalisedString.of(
                    "Ilmoitustyyppiä ei voi vaihtaa",
                    "Ilmoitustyyppiä ei voi vaihtaa",
                    "Reporting type can not be changed")));

        } else if (e instanceof HarvestPermitChangeForbiddenException) {
            return badRequest(singleErrorMessage(LocalisedString.of(
                    "Lupaa ei voi vaihtaa",
                    "Licens kan inte ändras",
                    "Harvest permit can not be changed")));

        } else if (e instanceof HarvestHuntingDayChangeForbiddenException) {
            return badRequest(singleErrorMessage(LocalisedString.of(
                    "Metsästyspäivää ei voi vaihtaa",
                    "Jaktdag kan inte ändras",
                    "Hunting day can not be changed")));

        } else if (e instanceof HarvestChangeReasonRequiredException) {
            return badRequest(singleErrorMessage(LocalisedString.of(
                    "Syy muutokselle vaaditaan",
                    "Anledningen till ändringen är nödvändig",
                    "The reason for the change is required")));
        }

        return null;
    }

    @Nonnull
    private static HarvestValidationFailureDTO mapException(final HarvestSpecVersionNotSupportedException e) {
        return singleErrorMessage(LocalisedString.of(
                "Käyttämäsi ohjelmistoversio ei tue toimintoa",
                "Din programvara stöder inte den här funktionen",
                "Client software version is not supported for this functionality"));
    }

    @Nonnull
    private static HarvestValidationFailureDTO mapException(final RhyNotResolvableByGeoLocationException e) {
        return singleErrorMessage(LocalisedString.of(
                String.format("Sijaintia longitude %d latitude %d vastaavaa RHY:tä ei löydy", e.getLongitude(), e.getLatitude()),
                String.format("Jaktvårdsförening kan inte hittas på longitude %d och latitude %d", e.getLongitude(), e.getLatitude()),
                String.format("Could not find RHY for longitude %d and latitude %d", e.getLongitude(), e.getLatitude())));
    }

    @Nonnull
    private static HarvestValidationFailureDTO mapException(final HarvestPermitNotFoundException e) {
        return singleErrorMessage(LocalisedString.of(
                String.format("Lupanumeroa ei löydy %s", e.getPermitNumber()),
                String.format("License med nummer %s kan inte hittas", e.getPermitNumber()),
                String.format("Permit number %s could not be found", e.getPermitNumber())));
    }

    @Nonnull
    private static HarvestValidationFailureDTO mapException(final HarvestPermitSpeciesAmountNotFound e) {
        return singleErrorMessage(LocalisedString.of(
                String.format("Eläinlajin %d metsästys ei ole sallittua luvalle %s annettuna päivänä %s", e.getGameSpeciesCode(), e.getPermitNumber(), e.getHarvestDate()),
                String.format("Jakt på djurslaget %d är inte tillåtet med licens %s på datum %s", e.getGameSpeciesCode(), e.getPermitNumber(), e.getHarvestDate()),
                String.format("Harvest for species %d for permit %s on %s is not supported", e.getGameSpeciesCode(), e.getPermitNumber(), e.getHarvestDate())));
    }

    @Nonnull
    private static HarvestValidationFailureDTO mapException(final PersonNotFoundException e) {
        if (e.getPersonId() != null) {
            return singleErrorMessage(LocalisedString.of(
                    String.format("Henkilöä id=%d ei löydy", e.getPersonId()),
                    String.format("Personen med id=%d hittas inte", e.getPersonId()),
                    String.format("Person with id=%d not found", e.getPersonId())));

        } else if (e.getHunterNumber() != null) {
            return singleErrorMessage(LocalisedString.of(
                    String.format("Henkilöä ei löydy metsästäjänumerolla %s", e.getHunterNumber()),
                    String.format("Personen med jägarenummer %s hittas inte", e.getHunterNumber()),
                    String.format("Person with hunter number %s not found", e.getHunterNumber())));

        } else {
            return singleErrorMessage(LocalisedString.of(
                    "Henkilöä ei löydy",
                    "Personen hittas inte",
                    "Person not found"));
        }
    }

    @Nonnull
    private static HarvestValidationFailureDTO mapException(final GameSpeciesNotFoundException e) {
        return singleErrorMessage(LocalisedString.of(
                String.format("Tuntematon laji %d", e.getGameSpeciesCode()),
                String.format("Okänd djurart %d", e.getGameSpeciesCode()),
                String.format("Unknown species %d", e.getGameSpeciesCode())));
    }

    @Nonnull
    private static HarvestValidationFailureDTO mapException(final HarvestFieldValidationException e) {
        final List<HarvestValidationFailureDTO.Error> errorList = new ArrayList<>();

        for (final HarvestFieldName fieldName : e.getMissingFields()) {
            errorList.add(HarvestValidationFailureDTO.message(missingFieldMessage(fieldName.name())));
        }

        for (final HarvestFieldName fieldName : e.getIllegalFields()) {
            errorList.add(HarvestValidationFailureDTO.message(illegalFieldMessage(fieldName.name())));
        }

        return new HarvestValidationFailureDTO(errorList);
    }

    @Nonnull
    private static HarvestValidationFailureDTO mapException(final HarvestSpecimenValidationException e) {
        final List<HarvestValidationFailureDTO.Error> errorList = new ArrayList<>();

        for (final HarvestSpecimenFieldName fieldName : e.getMissingFields()) {
            errorList.add(HarvestValidationFailureDTO.message(missingFieldMessage(fieldName.name())));
        }

        for (final HarvestSpecimenFieldName fieldName : e.getIllegalFields()) {
            errorList.add(HarvestValidationFailureDTO.message(illegalFieldMessage(fieldName.name())));
        }

        for (final Map.Entry<HarvestSpecimenFieldName, String> field : e.getIllegalValues().entrySet()) {
            errorList.add(HarvestValidationFailureDTO.message(illegalFieldValue(field.getKey(), field.getValue())));
        }

        return new HarvestValidationFailureDTO(errorList);
    }

    @Nonnull
    private static HarvestValidationFailureDTO mapException(final OutOfBoundsSpecimenAmountException e) {
        return singleErrorMessage(LocalisedString.of(
                String.format("Syötettyjen yksilöiden määrä %d ei ole välillä %d - %d", e.getGivenAmount(), e.getMinimumAmount(), e.getMaximumAmount()),
                String.format("Antalet personer som angetts %d är inte mellan %d och %d", e.getGivenAmount(), e.getMinimumAmount(), e.getMaximumAmount()),
                String.format("Specimen amount %d is not between %d - %d", e.getGivenAmount(), e.getMinimumAmount(), e.getMaximumAmount())
        ));
    }

    @Nonnull
    private static HarvestValidationFailureDTO mapException(final HarvestSpeciesRequiresPermitException e) {
        return singleErrorMessage(LocalisedString.of(
                String.format("Lupanumero vaaditaan annetulle lajille %d", e.getGameSpeciesCode()),
                String.format("Licensnumret krävs för arten %d", e.getGameSpeciesCode()),
                String.format("Permit number is required for species %d", e.getGameSpeciesCode())));
    }

    @Nonnull
    private static LocalisedString illegalFieldMessage(final String fieldName) {
        return LocalisedString.of(
                "Tietoa ei tueta " + fieldName,
                "Informationen stöds inte " + fieldName,
                "Illegal field " + fieldName);
    }

    @Nonnull
    private static LocalisedString illegalFieldValue(final HarvestSpecimenFieldName fieldName, final String fieldValue) {
        return LocalisedString.of(
                "Annettu tieto ei kelpaa " + fieldName + " : " + fieldValue,
                "Data är ogiltig " + fieldName + " : " + fieldValue,
                "Invalid value for field " + fieldName + " : " + fieldValue);
    }

    @Nonnull
    private static LocalisedString missingFieldMessage(final String fieldName) {
        return LocalisedString.of(
                "Tieto puuttuu " + fieldName,
                "Information saknas " + fieldName,
                "Missing field " + fieldName);
    }
}
