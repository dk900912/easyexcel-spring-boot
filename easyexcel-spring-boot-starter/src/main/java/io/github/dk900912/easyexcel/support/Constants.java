package io.github.dk900912.easyexcel.support;

/**
 * @author dukui
 */
public class Constants {

    private Constants() {}

    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    public static final String MULTIPART_FORM_DATA = "multipart/form-data";

    public static final String APPLICATION_VND_EXCEL = "application/vnd.ms-excel";

    public static final String APPLICATION_VND_OFFICE_DOC = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public static final String TEXT_CSV = "text/csv";

    public static final String RESPONSE_EXCEL_CONTENT_TYPE = APPLICATION_VND_OFFICE_DOC;

    public static final String RESPONSE_EXCEL_CONTENT_DISPOSITION = "Content-disposition";

    public static final String RESPONSE_EXCEL_ATTACHMENT = "attachment;filename=";

    public static final String DEFAULT_FILE_NAME_GENERATOR = "io.github.dk900912.easyexcel.support.DefaultFileNameGenerator";

}
