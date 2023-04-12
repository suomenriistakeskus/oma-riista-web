package fi.riista.feature.permit.application.conflict;

import fi.riista.feature.permit.application.PrintApplicationApproachMapFeatureCollection;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class PrintApplicationConflictMapModel {
    private final PrintApplicationApproachMapFeatureCollection union;
    private final List<PrintApplicationApproachMapFeatureCollection> components;
    private final List<PropertyInfo> propertyList;
    private final String firstApplicant;
    private final String secondApplicant;

    public PrintApplicationConflictMapModel(final PrintApplicationApproachMapFeatureCollection union,
                                            final List<PrintApplicationApproachMapFeatureCollection> components,
                                            final List<PropertyInfo> propertyList,
                                            final String firstApplicant,
                                            final String secondApplicant) {
        this.union = requireNonNull(union);
        this.components = requireNonNull(components);
        this.propertyList = requireNonNull(propertyList);
        this.firstApplicant = firstApplicant;
        this.secondApplicant = secondApplicant;
    }

    public PrintApplicationApproachMapFeatureCollection getUnion() {
        return union;
    }

    public List<PrintApplicationApproachMapFeatureCollection> getComponents() {
        return components;
    }

    public List<PropertyInfo> getPropertyList() {
        return propertyList;
    }

    public String getFirstApplicant() {
        return firstApplicant;
    }

    public String getSecondApplicant() {
        return secondApplicant;
    }

    public static class PropertyInfo {
        private String propertyNumber;
        private String propertyName;
        private double propertyAreaSize;
        private double propertyLandAreaSize;
        private double propertyWaterAreaSize;
        private double propertyConflictAreaSize;
        private double propertyConflictLandAreaSize;
        private double propertyConflictWaterAreaSize;

        public PropertyInfo(final String propertyNumber, final String propertyName,
                            final double propertyAreaSize, final double propertyWaterAreaSize,
                            final double propertyConflictAreaSize, final double propertyConflictWaterAreaSize) {
            this.propertyNumber = requireNonNull(propertyNumber);
            this.propertyName = propertyName;
            this.propertyAreaSize = propertyAreaSize;
            this.propertyWaterAreaSize = propertyWaterAreaSize;
            this.propertyLandAreaSize = this.propertyAreaSize - this.propertyWaterAreaSize;
            this.propertyConflictAreaSize = propertyConflictAreaSize;
            this.propertyConflictWaterAreaSize = propertyConflictWaterAreaSize;
            this.propertyConflictLandAreaSize = this.propertyConflictAreaSize - this.propertyConflictWaterAreaSize;
        }

        public String getPropertyNumber() {
            return propertyNumber;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public double getPropertyAreaSize() {
            return propertyAreaSize;
        }

        public double getPropertyLandAreaSize() {
            return propertyLandAreaSize;
        }

        public double getPropertyWaterAreaSize() {
            return propertyWaterAreaSize;
        }

        public double getPropertyConflictLandAreaSize() {
            return propertyConflictLandAreaSize;
        }

        public double getPropertyConflictWaterAreaSize() {
            return propertyConflictWaterAreaSize;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final PropertyInfo that = (PropertyInfo) o;
            return this.propertyNumber.equals(that.propertyNumber) &&
                    this.propertyName.equals(that.propertyName) &&
                    Double.compare(this.propertyAreaSize, that.propertyAreaSize) == 0 &&
                    Double.compare(this.propertyWaterAreaSize, that.propertyWaterAreaSize) == 0 &&
                    Double.compare(this.propertyLandAreaSize, that.propertyLandAreaSize) == 0 &&
                    Double.compare(this.propertyConflictAreaSize, that.propertyConflictAreaSize) == 0 &&
                    Double.compare(this.propertyConflictWaterAreaSize, that.propertyConflictWaterAreaSize) == 0 &&
                    Double.compare(this.propertyConflictLandAreaSize, that.propertyConflictLandAreaSize) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(propertyNumber, propertyName,
                    propertyAreaSize, propertyWaterAreaSize, propertyLandAreaSize,
                    propertyConflictAreaSize, propertyConflictWaterAreaSize, propertyConflictLandAreaSize);
        }
    }
}
