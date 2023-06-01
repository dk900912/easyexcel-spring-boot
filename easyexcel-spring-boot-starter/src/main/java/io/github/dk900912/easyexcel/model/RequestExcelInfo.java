package io.github.dk900912.easyexcel.model;

import io.github.dk900912.easyexcel.annotation.RequestExcel;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author dukui
 */
public class RequestExcelInfo {

    private boolean required;

    private List<SheetInfo> sheetInfoList;

    public RequestExcelInfo(RequestExcel requestExcel) {
        this.required = requestExcel.required();
        this.sheetInfoList = Stream.of(requestExcel.sheets())
                .map(SheetInfo::new)
                .toList();
    }

    public RequestExcelInfo(List<SheetInfo> sheetInfoList) {
        this.sheetInfoList = sheetInfoList;
    }

    public boolean isRequired() {
        return required;
    }

    public List<SheetInfo> getSheetInfoList() {
        return sheetInfoList;
    }

    public void setSheetInfoList(List<SheetInfo> sheetInfoList) {
        this.sheetInfoList = sheetInfoList;
    }
}
