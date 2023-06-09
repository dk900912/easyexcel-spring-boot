package io.github.dk900912.easyexcel.support;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author dukui
 */
public class LocalDateTimeFileNameGenerator implements FileNameGenerator {
    @Override
    public String generateFileName() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
