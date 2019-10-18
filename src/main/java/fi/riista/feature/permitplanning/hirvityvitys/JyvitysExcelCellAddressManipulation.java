package fi.riista.feature.permitplanning.hirvityvitys;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;

public interface JyvitysExcelCellAddressManipulation {

    default CellAddress address(String address) {
        return new CellAddress(address);
    }

    default Cell getCell(CellAddress address) {
        return getSheet().getRow(address.getRow()).getCell(address.getColumn());
    }

    Sheet getSheet();
}
