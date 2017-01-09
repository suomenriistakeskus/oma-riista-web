package fi.riista.integration.lupahallinta;

import java.io.Serializable;
import java.util.List;

public class HarvestPermitImportResultDTO implements Serializable {

    private final List<PermitParsingError> allErrors;
    private final int modifiedOrAddedCount;
    private final List<String> messages;

    public HarvestPermitImportResultDTO(int modifiedOrAddedCount, List<String> messages) {
        this.allErrors = null;
        this.modifiedOrAddedCount = modifiedOrAddedCount;
        this.messages = messages;
    }

    public HarvestPermitImportResultDTO(List<PermitParsingError> allErrors) {
        this.allErrors = allErrors;
        this.modifiedOrAddedCount = 0;
        this.messages = null;
    }

    public List<PermitParsingError> getAllErrors() {
        return allErrors;
    }

    public int getModifiedOrAddedCount() {
        return modifiedOrAddedCount;
    }

    public List<String> getMessages() {
        return messages;
    }

    public static class PermitParsingError implements Serializable {

        private int row;
        private String permitNumber;
        private List<String> errors;

        public PermitParsingError(int row, String permitNumber, List<String> errors) {
            this.row = row;
            this.permitNumber = permitNumber;
            this.errors = errors;
        }

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public String getPermitNumber() {
            return permitNumber;
        }

        public void setPermitNumber(String permitNumber) {
            this.permitNumber = permitNumber;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }
    }

}
