package fi.riista.feature.permit.application.conflict;

import fi.riista.integration.mapexport.MapPdfModel;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class PrintApplicationConflictMapModel {
    private final MapPdfModel union;
    private final List<MapPdfModel> components;
    private final List<PropertyInfo> propertyList;

    public PrintApplicationConflictMapModel(final MapPdfModel union,
                                            final List<MapPdfModel> components,
                                            final List<PropertyInfo> propertyList) {
        this.union = requireNonNull(union);
        this.components = requireNonNull(components);
        this.propertyList = requireNonNull(propertyList);
    }

    public MapPdfModel getUnion() {
        return union;
    }

    public List<MapPdfModel> getComponents() {
        return components;
    }

    public List<PropertyInfo> getPropertyList() {
        return propertyList;
    }

    public static class PropertyInfo {
        private String propertyNumber;
        private String propertyName;

        public PropertyInfo(final String propertyNumber, final String propertyName) {
            this.propertyNumber = requireNonNull(propertyNumber);
            this.propertyName = propertyName;
        }

        public String getPropertyNumber() {
            return propertyNumber;
        }

        public String getPropertyName() {
            return propertyName;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final PropertyInfo that = (PropertyInfo) o;
            return propertyNumber.equals(that.propertyNumber);
        }

        @Override
        public int hashCode() {
            return Objects.hash(propertyNumber);
        }
    }
}
