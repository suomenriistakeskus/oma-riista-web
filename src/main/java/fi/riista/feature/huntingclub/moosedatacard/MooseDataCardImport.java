package fi.riista.feature.huntingclub.moosedatacard;

import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroup_;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.util.jpa.CriteriaUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Access(value = AccessType.FIELD)
public class MooseDataCardImport extends LifecycleEntity<Long> implements HasBeginAndEndDate {

    private Long id;

    @Column
    private LocalDate beginDate;

    @Column
    private LocalDate endDate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hunting_group_id", nullable = false)
    private HuntingClubGroup group;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(unique = true, nullable = false)
    private PersistentFileMetadata xmlFileMetadata;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(unique = true, nullable = false)
    private PersistentFileMetadata pdfFileMetadata;

    @NotNull
    @Column(nullable = false)
    private DateTime filenameTimestamp;

    @OneToMany(mappedBy = "mooseDataCardImport")
    private Set<GroupHuntingDay> huntingDays = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "moose_data_card_import_message",
            joinColumns = @JoinColumn(name = "import_id") ,
            uniqueConstraints = @UniqueConstraint(columnNames = { "import_id", "ordinal" }) )
    @Column(name = "message", nullable = false)
    @OrderColumn(name = "ordinal", nullable = false)
    private List<String> messages = new ArrayList<>();

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "moose_data_card_import_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    public HuntingClubGroup getGroup() {
        return group;
    }

    public void setGroup(final HuntingClubGroup group) {
        CriteriaUtils.updateInverseCollection(HuntingClubGroup_.mooseDataCardImports, this, this.group, group);
        this.group = group;
    }

    public PersistentFileMetadata getXmlFileMetadata() {
        return xmlFileMetadata;
    }

    public void setXmlFileMetadata(final PersistentFileMetadata xmlFileMetadata) {
        this.xmlFileMetadata = xmlFileMetadata;
    }

    public PersistentFileMetadata getPdfFileMetadata() {
        return pdfFileMetadata;
    }

    public void setPdfFileMetadata(final PersistentFileMetadata pdfFileMetadata) {
        this.pdfFileMetadata = pdfFileMetadata;
    }

    public DateTime getFilenameTimestamp() {
        return filenameTimestamp;
    }

    public void setFilenameTimestamp(final DateTime filenameTimestamp) {
        this.filenameTimestamp = filenameTimestamp;
    }

    // Intentionally package-private
    Set<GroupHuntingDay> getHuntingDays() {
        return huntingDays;
    }

    public List<String> getMessages() {
        return messages;
    }

}
