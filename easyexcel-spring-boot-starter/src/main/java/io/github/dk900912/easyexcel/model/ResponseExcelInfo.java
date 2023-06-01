package io.github.dk900912.easyexcel.model;

import com.alibaba.excel.support.ExcelTypeEnum;
import io.github.dk900912.easyexcel.annotation.ResponseExcel;
import io.github.dk900912.easyexcel.support.Scene;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author dukui
 */
public class ResponseExcelInfo {

    private String name;

    private ExcelTypeEnum suffix;

    private List<SheetInfo> sheetInfoList;

    private Scene scene;

    public ResponseExcelInfo(ResponseExcel responseExcel) {
        this.name = responseExcel.name();
        this.suffix = responseExcel.suffix();
        this.sheetInfoList = Stream.of(responseExcel.sheets())
                .map(SheetInfo::new)
                .toList();
        this.scene = responseExcel.scene();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExcelTypeEnum getSuffix() {
        return suffix;
    }

    public void setSuffix(ExcelTypeEnum suffix) {
        this.suffix = suffix;
    }

    public List<SheetInfo> getSheetInfoList() {
        return sheetInfoList;
    }

    public void setSheetInfoList(List<SheetInfo> sheetInfoList) {
        this.sheetInfoList = sheetInfoList;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
}
