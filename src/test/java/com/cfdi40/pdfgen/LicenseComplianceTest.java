/*
 * Copyright (C) 2025 The cfdi-4.0-pdf-generador Authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.cfdi40.pdfgen;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Static AGPL-3.0 compliance checks. These guard against regressions introduced
 * by future contributors who might accidentally drop the license header from a
 * new file or leak proprietary brand identifiers back into the source tree.
 *
 *  - Every {@code .java} file under {@code src/main/java} must contain the AGPL
 *    boilerplate header (matched by the phrase "GNU Affero General Public
 *    License").
 *  - No source file under {@code src/main} may contain the historical brand or
 *    company identifiers that were stripped during the public open-sourcing.
 */
class LicenseComplianceTest {

    private static final Path SRC_MAIN_JAVA = Paths.get("src", "main", "java");
    private static final Path SRC_MAIN = Paths.get("src", "main");

    @Test
    void everyJavaSourceFileHasAgplHeader() throws IOException {
        try (Stream<Path> stream = Files.walk(SRC_MAIN_JAVA)) {
            List<Path> missing = stream
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> {
                        try {
                            String head = readFirstChars(p, 4096);
                            return !head.contains("GNU Affero General Public License");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
            assertThat(missing)
                    .as("All Java source files must carry the AGPL-3.0 license header")
                    .isEmpty();
        }
    }

    @Test
    void noProprietaryBrandIdentifiersLeakIntoSources() throws IOException {
        // Case-insensitive, word-bounded match for stripped brand tokens.
        Pattern brandPattern = Pattern.compile(
                "(?i)\\b(conectum|globogo|provac|fundacion|posadas|nexttech|miguel\\.?martinez)\\b");

        try (Stream<Path> stream = Files.walk(SRC_MAIN)) {
            List<String> hits = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String s = p.toString();
                        return s.endsWith(".java")
                                || s.endsWith(".properties")
                                || s.endsWith(".xml")
                                || s.endsWith(".factories");
                    })
                    .flatMap(p -> {
                        try {
                            String body = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
                            if (brandPattern.matcher(body).find()) {
                                return Stream.of(p.toString());
                            }
                            return Stream.empty();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
            assertThat(hits)
                    .as("Proprietary brand identifiers must not appear in source")
                    .isEmpty();
        }
    }

    private static String readFirstChars(Path p, int max) throws IOException {
        byte[] all = Files.readAllBytes(p);
        int n = Math.min(all.length, max);
        return new String(all, 0, n, StandardCharsets.UTF_8);
    }
}
