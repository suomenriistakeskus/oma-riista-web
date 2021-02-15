package fi.riista.feature.harvestpermit.list;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.permit.DocumentNumberUtil;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.decision.PermitDecision;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Comparator.reverseOrder;

public class ApplicationDecisionPermitListDTOBuilder {
    // Sorted by permitNumber
    private TreeMap<String, ApplicationDecisionPermitListDTO> results = new TreeMap<>(reverseOrder());

    // Use large artificial application number for drafts to sort them first
    private AtomicInteger draftApplicationNumberSequence = new AtomicInteger(999_99999);

    public void addApplication(final @Nonnull HarvestPermitApplication a) {
        final String permitNumber = Optional.ofNullable(a.getApplicationNumber())
                .map(applicationNumber -> DocumentNumberUtil.createDocumentNumber(a.getApplicationYear(), 1, applicationNumber))
                .orElseGet(() -> createDraftApplicationNumber(a));
        final ApplicationDecisionPermitListDTO existingDto = findByApplication(a);

        if (existingDto != null) {
            existingDto.setApplication(ListPermitApplicationDTO.create(a));
        } else {
            results.put(permitNumber, new ApplicationDecisionPermitListDTO(ListPermitApplicationDTO.create(a)));
        }
    }

    public void addDecision(final @Nonnull PermitDecision d) {
        final ApplicationDecisionPermitListDTO existingDto = findByApplication(d.getApplication());

        if (existingDto != null) {
            existingDto.setDecision(ListDecisionDTO.create(d));
        } else {
            results.put(d.createPermitNumber(), new ApplicationDecisionPermitListDTO(
                    ListPermitApplicationDTO.create(d.getApplication()),
                    ListDecisionDTO.create(d)));
        }
    }

    public void addPermit(final @Nonnull HarvestPermit p) {
        final ApplicationDecisionPermitListDTO existingDto = findByPermit(p);

        if (existingDto != null) {
            if (!existingDto.containsPermit(p)) {
                existingDto.addPermit(ListHarvestPermitDTO.create(p));
            }
        } else {
            results.put(p.getPermitNumber(), new ApplicationDecisionPermitListDTO(ListHarvestPermitDTO.create(p)));
        }
    }

    public List<ApplicationDecisionPermitListDTO> build() {
        return ImmutableList.copyOf(results.values());
    }

    @Nonnull
    private String createDraftApplicationNumber(@Nonnull final HarvestPermitApplication a) {
        final int huntingYear = a.getApplicationYear();
        final int applicationNumber = draftApplicationNumberSequence.getAndDecrement();

        return DocumentNumberUtil.createDocumentNumber(huntingYear, 1, applicationNumber);
    }

    private ApplicationDecisionPermitListDTO findByApplication(final @Nonnull HarvestPermitApplication application) {
        return results.values().stream().filter(dto -> dto.equalsApplication(application)).findFirst().orElse(null);
    }

    private ApplicationDecisionPermitListDTO findByDecision(final @Nonnull PermitDecision decision) {
        return results.values().stream().filter(dto -> dto.equalsDecision(decision)).findFirst().orElse(null);
    }

    private ApplicationDecisionPermitListDTO findByPermit(final @Nonnull HarvestPermit permit) {
        return Optional.ofNullable(permit.getPermitDecision()).map(this::findByDecision).orElse(null);
    }
}
