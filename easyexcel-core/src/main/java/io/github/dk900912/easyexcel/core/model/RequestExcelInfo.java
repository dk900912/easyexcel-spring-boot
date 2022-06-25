package io.github.dk900912.easyexcel.core.model;

import io.github.dk900912.easyexcel.core.annotation.RequestExcel;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author dukui
 */
public class RequestExcelInfo {

    private List<SheetInfo> sheetInfoList;

    public RequestExcelInfo(RequestExcel requestExcel) {
        this.sheetInfoList = Stream.of(requestExcel.sheets())
                .map(SheetInfo::new)
                .collect(Collectors.toList());
    }

    public RequestExcelInfo(List<SheetInfo> sheetInfoList) {
        this.sheetInfoList = sheetInfoList;
    }

    public List<SheetInfo> getSheetInfoList() {
        return sheetInfoList;
    }

    public void setSheetInfoList(List<SheetInfo> sheetInfoList) {
        this.sheetInfoList = sheetInfoList;
    }
}
