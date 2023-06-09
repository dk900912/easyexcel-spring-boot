package io.github.dk900912.easyexcel.support;

import java.time.Instant;

/**
 * @author dukui
 */
public class TimestampFileNameGenerator implements FileNameGenerator {
    @Override
    public String generateFileName() {
        return String.valueOf(Instant.now().toEpochMilli());
    }
}
