package fi.riista.feature.gamediary;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersionSupport;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.util.Functions;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class GameDiaryEntryDTO extends BaseEntityDTO<Long> {

    private Long id;

    private Integer rev;

    private final GameDiaryEntryType type;

    @Valid
    @NotNull
    @HarvestSpecVersionSupport(since = HarvestSpecVersion._1)
    private GeoLocation geoLocation;

    @NotNull
    @HarvestSpecVersionSupport(since = HarvestSpecVersion._1)
    private LocalDateTime pointOfTime;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @HarvestSpecVersionSupport(since = HarvestSpecVersion._1)
    private String description;

    @HarvestSpecVersionSupport(since = HarvestSpecVersion._1)
    private boolean canEdit;

    @HarvestSpecVersionSupport(since = HarvestSpecVersion._1)
    private final List<UUID> imageIds = new ArrayList<>();

    protected GameDiaryEntryDTO(@Nonnull final GameDiaryEntryType type) {
        this.type = Objects.requireNonNull(type);
    }

    public boolean hasImageId(final UUID id) {
        return imageIds.contains(id);
    }

    // Accessors -->

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public GameDiaryEntryType getType() {
        return type;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public LocalDateTime getPointOfTime() {
        return pointOfTime;
    }

    public void setPointOfTime(final LocalDateTime pointOfTime) {
        this.pointOfTime = pointOfTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public List<UUID> getImageIds() {
        return imageIds;
    }

    public void setImageIds(@Nullable final List<UUID> imageIds) {
        if (!this.imageIds.isEmpty()) {
            this.imageIds.clear();
        }
        if (imageIds != null) {
            this.imageIds.addAll(imageIds);
        }
    }

    protected static abstract class Builder<DTO extends GameDiaryEntryDTO, SELF extends Builder<DTO, SELF>> {

        protected DTO dto;

        public Builder() {
            dto = createDTO();
        }

        public SELF withIdAndRev(@Nonnull final BaseEntity<Long> entity) {
            Objects.requireNonNull(entity);
            DtoUtil.copyBaseFields(entity, dto);
            return self();
        }

        public SELF withGeoLocation(final GeoLocation location) {
            dto.setGeoLocation(location);
            return self();
        }

        public SELF withPointOfTime(final LocalDateTime pointOfTime) {
            dto.setPointOfTime(pointOfTime);
            return self();
        }

        public SELF withDescription(final String description) {
            dto.setDescription(description);
            return self();
        }

        public SELF withCanEdit(final boolean canEdit) {
            dto.setCanEdit(canEdit);
            return self();
        }

        public SELF withImageIds(@Nullable final List<UUID> imageIds) {
            dto.setImageIds(imageIds);
            return self();
        }

        protected SELF populateWithEntry(@Nonnull final GameDiaryEntry entry) {
            Objects.requireNonNull(entry);

            return withIdAndRev(entry)
                    .withGeoLocation(entry.getGeoLocation())
                    .withPointOfTime(Optional
                            .ofNullable(DateUtil.toLocalDateTimeNullSafe(entry.getPointOfTime()))
                            .orElse(null))
                    .withDescription(entry.getDescription());
        }

        public SELF populateWith(@Nullable final Iterable<GameDiaryImage> images) {
            if (images != null) {
                F.mapNonNulls(images, dto.getImageIds(), Functions.idOf(GameDiaryImage::getFileMetadata));
            }
            return self();
        }

        public SELF chain(@Nonnull final Consumer<SELF> consumer) {
            Objects.requireNonNull(consumer);
            consumer.accept(self());
            return self();
        }

        public DTO build() {
            return dto;
        }

        protected abstract DTO createDTO();

        protected abstract SELF self();
    }

}
