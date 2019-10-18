package fi.riista.feature.permitplanning.hirvityvitys;

public class JyvitysExcelCellFormula {
    private final String cellAddress;
    private final String formula;

    private JyvitysExcelCellFormula(final String cellAddress, final String formula) {
        this.cellAddress = cellAddress;
        this.formula = formula;
    }

    public static JyvitysExcelCellFormula of(final String cellAddress, final String formula) {
        return new JyvitysExcelCellFormula(cellAddress, formula);
    }

    public String getCellAddress() {
        return cellAddress;
    }

    public String getFormula() {
        return formula;
    }
}
