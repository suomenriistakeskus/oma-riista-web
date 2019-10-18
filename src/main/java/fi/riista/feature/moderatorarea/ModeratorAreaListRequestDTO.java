package fi.riista.feature.moderatorarea;

import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class ModeratorAreaListRequestDTO {

    @NotNull
    private Integer page;

    @NotNull
    private Integer size;

    @Pattern(regexp = "\\d{3,3}")
    private String rkaCode;

    @Min(2000)
    @Max(2100)
    private Integer year;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String searchText;

    public Integer getPage() {
        return page;
    }

    public void setPage(final Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(final Integer size) {
        this.size = size;
    }

    public String getRkaCode() {
        return rkaCode;
    }

    public void setRkaCode(final String rkaCode) {
        this.rkaCode = rkaCode;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(final Integer year) {
        this.year = year;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(final String searchText) {
        this.searchText = searchText;
    }
}
