package fi.riista.api.organisation;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysDTO;
import fi.riista.feature.organization.rhy.gamedamageinspection.GameDamageInspectionEventCrudFeature;
import fi.riista.feature.organization.rhy.gamedamageinspection.GameDamageInspectionEventDTO;
import fi.riista.feature.organization.rhy.gamedamageinspection.GameDamageInspectionEventExcelView;
import fi.riista.feature.organization.rhy.gamedamageinspection.GameDamageInspectionEventExportDTO;
import fi.riista.feature.organization.rhy.gamedamageinspection.GameDamageInspectionEventExportService;
import fi.riista.feature.organization.rhy.gamedamageinspection.GameDamageInspectionEventSummaryDTO;
import fi.riista.feature.organization.rhy.gamedamageinspection.GameDamageInspectionEventSummaryExcelView;
import fi.riista.feature.organization.rhy.gamedamageinspection.GameDamageInspectorLookupFeature;
import fi.riista.feature.organization.rhy.gamedamageinspection.GameDamageType;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.joda.time.LocalDate;
import org.springframework.context.MessageSource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static fi.riista.util.MediaTypeExtras.APPLICATION_EXCEL_VALUE;

@RestController
@RequestMapping(value = "/api/v1/riistanhoitoyhdistys", produces = MediaType.APPLICATION_JSON_VALUE)
public class RhyGameDamageInspectionEventApiResource {
    @Resource
    private GameDamageInspectionEventCrudFeature gameDamageInspectionEventCrudFeature;

    @Resource
    private GameDamageInspectionEventExportService exportService;

    @Resource
    private MessageSource messageSource;

    @Resource
    private GameDamageInspectorLookupFeature gameDamageInspectorLookupFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/gamedamagetypes")
    public List<GameDamageType> readEventTypes() {
        return Arrays.asList(GameDamageType.values());
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{rhyId:\\d+}/gamedamageinspectionevents/{year:\\d+}")
    public List<GameDamageInspectionEventDTO> getEvents(final @PathVariable Long rhyId,
                                                        final @PathVariable Integer year) {
        return gameDamageInspectionEventCrudFeature.listGameDamageInspectionEvents(rhyId, year);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "{rhyId:\\d+}/gamedamageinspectionevents", consumes = MediaType.APPLICATION_JSON_VALUE)
    public GameDamageInspectionEventDTO createEvent(@PathVariable final long rhyId,
                                                    @RequestBody @Validated final GameDamageInspectionEventDTO dto) {

        dto.setRhy(new RiistanhoitoyhdistysDTO());
        dto.getRhy().setId(rhyId);
        return gameDamageInspectionEventCrudFeature.create(dto);
    }

    @PutMapping(value = "gamedamageinspectionevents/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public GameDamageInspectionEventDTO updateEvent(@RequestBody @Validated final GameDamageInspectionEventDTO dto,
                                                    @PathVariable final long id) {

        dto.setId(id);
        return gameDamageInspectionEventCrudFeature.update(dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "gamedamageinspectionevents/{id:\\d+}")
    public void deleteEvent(@PathVariable final long id) {
        gameDamageInspectionEventCrudFeature.delete(id);
    }

    @PostMapping(value = "/{rhyId:\\d+}/gamedamageinspectionevents/{year:\\d+}/excel/{gameDamageType}",
            produces = APPLICATION_EXCEL_VALUE)
    public ModelAndView exportExcel(final Locale locale,
                                    @PathVariable final Long rhyId,
                                    @PathVariable final Integer year,
                                    @PathVariable final GameDamageType gameDamageType) {

        final GameDamageInspectionEventExportDTO dto =
                exportService.getGameDamageInspectionEventExport(rhyId, year, gameDamageType);

        return new ModelAndView(
                new GameDamageInspectionEventExcelView(locale, new EnumLocaliser(messageSource, locale), dto));
    }

    @PostMapping(value = "/gamedamageinspectionevents/{year:\\d+}/excel/{gameDamageType}/summary",
            produces = APPLICATION_EXCEL_VALUE)
    public ModelAndView exportExcelSummary(final Locale locale,
                                           @PathVariable final Integer year,
                                           @PathVariable final GameDamageType gameDamageType) {

        final List<GameDamageInspectionEventSummaryDTO> dtos =
                exportService.getGameDamageInspectionEventSummary(year, gameDamageType);

        return new ModelAndView(
                new GameDamageInspectionEventSummaryExcelView(locale, new EnumLocaliser(messageSource, locale),
                        year, gameDamageType, dtos));
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "{rhyId:\\d+}/gamedamageinspectionevents/inspectors/{date}")
    public List<PersonContactInfoDTO> getInspectors(final @PathVariable Long rhyId,
                                                    final @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return gameDamageInspectorLookupFeature.listActiveOccupations(
                rhyId,
                OccupationType.RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA,
                date);
    }
}
