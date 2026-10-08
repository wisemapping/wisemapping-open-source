/*
 *    Copyright [2007-2025] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */


package com.wisemapping.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Guards the message keys that {@code samples/tutorial.vm} depends on.
 *
 * <p>That template is rendered by {@code UserServiceImpl.buildTutorialMindmap} to
 * create the welcome mindmap for every new account, and it resolves each label
 * through {@code messages.getMessage("TUTORIAL.X", $noArgs, $locale)} — an overload
 * with no default, so a missing key throws {@code NoSuchMessageException} and the
 * new user gets no tutorial map.
 *
 * <p>This test exists because those 26 keys are referenced only from a Velocity
 * template. Nothing in Java mentions them, so a "find unused keys" sweep reports
 * them as dead, and the rest of the suite does not cover tutorial-map creation —
 * deleting all 26 once passed every other test in the project.
 *
 * <p>Deliberately not a {@code @SpringBootTest}: the subject is the resource
 * bundles themselves, so it builds a MessageSource mirroring the production
 * settings (basename {@code messages}, UTF-8, no system-locale fallback) and runs
 * in milliseconds.
 */
class TutorialTemplateMessagesTest {

    private static final String TEMPLATE = "/samples/tutorial.vm";

    /** Every locale shipped as messages_<tag>.properties. */
    private static final List<Locale> LOCALES = List.of(
            Locale.forLanguageTag("ar"), Locale.forLanguageTag("de"), Locale.forLanguageTag("en"),
            Locale.forLanguageTag("es"), Locale.forLanguageTag("fr"), Locale.forLanguageTag("hi"),
            Locale.forLanguageTag("it"), Locale.forLanguageTag("ja"), Locale.forLanguageTag("pt"),
            Locale.forLanguageTag("ru"), Locale.forLanguageTag("uk"), Locale.forLanguageTag("zh-CN"),
            Locale.forLanguageTag("zh"));

    private static ResourceBundleMessageSource messageSource() {
        final ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setDefaultEncoding(StandardCharsets.UTF_8.name());
        source.setFallbackToSystemLocale(false);
        return source;
    }

    private static Set<String> keysReferencedByTemplate() throws IOException {
        final Set<String> keys = new LinkedHashSet<>();
        try (InputStream in = TutorialTemplateMessagesTest.class.getResourceAsStream(TEMPLATE)) {
            assertNotNull(in, TEMPLATE + " must be on the classpath");
            final String body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            // matches: messages.getMessage("SOME.KEY", $noArgs, $locale)
            final Matcher m = Pattern.compile("getMessage\\(\\s*\"([^\"]+)\"").matcher(body);
            while (m.find()) {
                keys.add(m.group(1));
            }
        }
        return keys;
    }

    @Test
    @DisplayName("tutorial.vm still references the keys it is supposed to, so a gutted template is caught too")
    void templateStillReferencesItsKeys() throws IOException {
        final Set<String> keys = keysReferencedByTemplate();
        assertTrue(keys.size() >= 20,
                "tutorial.vm should reference at least 20 message keys, found " + keys.size()
                        + ". If the template was intentionally trimmed, lower this bound deliberately.");
        assertTrue(keys.stream().allMatch(k -> k.startsWith("TUTORIAL.")),
                "every key tutorial.vm resolves should be a TUTORIAL.* key, got: " + keys);
    }

    @Test
    @DisplayName("every key tutorial.vm resolves is resolvable in every locale (base bundle included)")
    void everyTemplateKeyResolvesInEveryLocale() throws IOException {
        final ResourceBundleMessageSource source = messageSource();
        final Set<String> keys = keysReferencedByTemplate();

        final StringBuilder problems = new StringBuilder();
        for (Locale locale : LOCALES) {
            for (String key : keys) {
                try {
                    final String value = source.getMessage(key, null, locale);
                    if (value == null || value.isBlank()) {
                        problems.append("\n  blank: ").append(key).append(" [").append(locale).append(']');
                    } else if (value.equals(key)) {
                        problems.append("\n  unresolved (returned the key): ").append(key)
                                .append(" [").append(locale).append(']');
                    }
                } catch (RuntimeException e) {
                    // NoSuchMessageException is what a deleted key produces, and it is
                    // exactly what breaks tutorial-map creation at runtime.
                    problems.append("\n  missing: ").append(key).append(" [").append(locale)
                            .append("] -> ").append(e.getClass().getSimpleName());
                }
            }
        }
        if (problems.length() > 0) {
            fail("tutorial.vm message keys are not resolvable; new users would get no tutorial map:" + problems);
        }
    }

    @Test
    @DisplayName("the WELCOME key used for the tutorial map title resolves in every locale")
    void welcomeTitleKeyResolvesInEveryLocale() {
        // buildTutorialMindmap sets the title from messageSource.getMessage("WELCOME", null, locale),
        // another no-default overload.
        final ResourceBundleMessageSource source = messageSource();
        for (Locale locale : LOCALES) {
            final String value = source.getMessage("WELCOME", null, locale);
            assertNotNull(value, "WELCOME must resolve for " + locale);
            assertFalse(value.isBlank(), "WELCOME must not be blank for " + locale);
        }
    }

    @Test
    @DisplayName("every locale file physically declares every tutorial key, with no fallback masking a gap")
    void everyLocaleFileDeclaresEveryTemplateKeyWithoutFallback() throws IOException {
        // getMessage() alone is not enough: ResourceBundle falls back to the base
        // bundle, so a key deleted from messages_fr still resolves (as English) and
        // the resolution test above stays green. CLAUDE.md requires every locale to
        // carry every key, so assert against the files directly.
        final Set<String> keys = keysReferencedByTemplate();
        final Path dir = Path.of("src", "main", "resources");
        final StringBuilder problems = new StringBuilder();

        try (var paths = Files.list(dir)) {
            final List<Path> bundles = paths
                    .filter(p -> p.getFileName().toString().matches("messages_.+\\.properties"))
                    .sorted()
                    .toList();
            assertFalse(bundles.isEmpty(), "no messages_*.properties found under " + dir.toAbsolutePath());

            for (Path bundle : bundles) {
                final Set<String> declared = new LinkedHashSet<>();
                for (String line : Files.readAllLines(bundle, StandardCharsets.UTF_8)) {
                    final int eq = line.indexOf('=');
                    if (eq > 0 && !line.stripLeading().startsWith("#")) {
                        declared.add(line.substring(0, eq).strip());
                    }
                }
                for (String key : keys) {
                    if (!declared.contains(key)) {
                        problems.append("\n  ").append(bundle.getFileName()).append(" is missing ").append(key);
                    }
                }
            }
        }
        if (problems.length() > 0) {
            fail("locale bundles have drifted; CLAUDE.md requires every locale to carry every key:" + problems);
        }
    }
}
