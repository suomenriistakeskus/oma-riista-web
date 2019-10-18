package fi.riista.feature.permit.area.mml;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.entity.PropertyIdentifier;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Component
public class HarvestPermitAreaMmlPdfFeature {
    private static final String JSP_MML = "pdf/application-area-propertylist";
    private static final int HECTARE_IN_SQUARE_METERS = 10_000;

    public static class PdfModel {
        private final String view;
        private final List<HarvestPermitAreaMmlDTO> mmls;

        PdfModel(final String view, final List<HarvestPermitAreaMmlDTO> mmls) {
            this.view = requireNonNull(view);
            this.mmls = requireNonNull(mmls);
        }

        public String getView() {
            return view;
        }

        public List<HarvestPermitAreaMmlDTO> getMmls() {
            return mmls;
        }
    }


    @Resource
    private RequireEntityService requireEntityService;

    @Transactional(readOnly = true)
    public PdfModel getModel(final long id) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(id, EntityPermission.READ);

        return new PdfModel(JSP_MML, getSortedAndFormattedPropertyListFrom(application));
    }

    private static List<HarvestPermitAreaMmlDTO> getSortedAndFormattedPropertyListFrom(final HarvestPermitApplication application) {
        return application.getArea().getMml()
                .stream()
                .map(HarvestPermitAreaMmlPdfFeature::extractPropertyIdentifier)
                .sorted(Comparator.comparing(HarvestPermitAreaMmlDTO::getTunnus))
                .collect(toList());
    }

    private static HarvestPermitAreaMmlDTO extractPropertyIdentifier(final HarvestPermitAreaMml mml) {
        return new HarvestPermitAreaMmlDTO(
                PropertyIdentifier.create(mml.getKiinteistoTunnus()).getDelimitedValue(),
                mml.getPalstaId(),
                mml.getName(),
                mml.getIntersectionArea() / HECTARE_IN_SQUARE_METERS);
    }
}
