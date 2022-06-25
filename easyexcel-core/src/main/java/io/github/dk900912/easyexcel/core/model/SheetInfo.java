package io.github.dk900912.easyexcel.core.model;

import io.github.dk900912.easyexcel.core.annotation.Sheet;

/**
 * @author dukui
 */
public class SheetInfo {

    private String name;

    private int index;

    private Class<?> headClazz;

    private int headRowNumber;

    public SheetInfo(Sheet sheet) {
        this.name = sheet.name();
        this.index = sheet.index();
        this.headClazz = sheet.headClazz();
        this.headRowNumber = sheet.headRowNumber();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Class<?> getHeadClazz() {
        return headClazz;
    }

    public void setHeadClazz(Class<?> headClazz) {
        this.headClazz = headClazz;
    }

    public int getHeadRowNumber() {
        return headRowNumber;
    }

    public void setHeadRowNumber(int headRowNumber) {
        this.headRowNumber = headRowNumber;
    }
}
