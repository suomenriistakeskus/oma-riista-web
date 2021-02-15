package fi.riista.integration.metsastajarekisteri.jht;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.util.DateUtil;
import fi.riista.util.JaxbUtils;
import org.joda.time.LocalDate;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MrJhtExportFeature {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource(name = "mrJhtExportMarshaller")
    private Jaxb2Marshaller jaxbMarshaller;

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_MR_JHT')")
    public String exportAsXml() {
        final MR_JHT_Jht root = export();
        return JaxbUtils.marshalToString(root, jaxbMarshaller);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasPrivilege('EXPORT_MR_JHT')")
    public MR_JHT_Jht export() {
        final QOccupation OCC = QOccupation.occupation;
        final QOrganisation ORG = QOrganisation.organisation;
        final QPerson PERSON = QPerson.person;

        final EnumSet<OccupationType> jhtValues = OccupationType.jhtValues();
        jhtValues.add(OccupationType.TOIMINNANOHJAAJA);

        final LocalDate today = DateUtil.today();

        final List<MR_JHT_Occupation> dtos = jpqlQueryFactory
                .select(OCC.id, ORG.officialCode,
                        OCC.occupationType, OCC.beginDate, OCC.endDate,
                        PERSON.ssn, PERSON.hunterNumber)
                .from(OCC)
                .join(OCC.organisation, ORG).on(ORG.organisationType.eq(OrganisationType.RHY))
                .join(OCC.person, PERSON)
                .where(OCC.occupationType.in(jhtValues))
                .where(OCC.beginDate.isNull().or(OCC.beginDate.loe(today)))
                .where(OCC.endDate.isNull().or(OCC.endDate.goe(today)))
                .orderBy(ORG.officialCode.asc(), OCC.id.asc())
                .fetch()
                .stream().map(t -> {
                    final MR_JHT_Occupation dto = new MR_JHT_Occupation();
                    dto.setId(t.get(OCC.id));
                    dto.setRhyOfficialCode(t.get(ORG.officialCode));
                    dto.setOccupationType(occType(t.get(OCC.occupationType)));
                    dto.setBeginDate(t.get(OCC.beginDate));
                    dto.setEndDate(t.get(OCC.endDate));

                    final String ssn = t.get(PERSON.ssn);
                    final String hunterNumber = t.get(PERSON.hunterNumber);
                    dto.setSsn(StringUtils.hasText(hunterNumber) ? null : ssn);
                    dto.setHunterNumber(hunterNumber);

                    return dto;
                })
                .collect(Collectors.toList());

        final MR_JHT_Jht root = new MR_JHT_Jht();
        root.setOccupation(dtos);
        return root;
    }

    private static MR_JHT_OccupationTypeEnum occType(final OccupationType occupationType) {
        return MR_JHT_OccupationTypeEnum.fromValue(occupationType.name());
    }
}
