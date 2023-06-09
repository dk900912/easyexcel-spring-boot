package io.github.dk900912.easyexcel.support;

import java.util.UUID;

/**
 * @author dukui
 */
public class DefaultFileNameGenerator implements FileNameGenerator {
    @Override
    public String generateFileName() {
        return UUID.randomUUID().toString();
    }
}
