/*
 * Copyright (C) 2024 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.dataskin.ops;

import com.google.common.base.Preconditions;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility methods.
 */
public final class Util {

    private Util() {
    }

    /**
     * Generate human words from a {@code camelCase} name.
     *
     * @param string camel case string
     * @return normal readable string
     * @throws IllegalArgumentException if {@code string} is null
     */
    public static String nameFromCamelCase(String string) {
        Preconditions.checkArgument(string != null, "null string");

        // Split camel case into words       ↓ no preceding uppercase  ↓ yes following uppercase
        final String[] words = string.split("(?<=[^\\p{javaUpperCase}])(?=\\p{javaUpperCase})", 0);

        // Capitalize each word and join with spaces
        return Stream.of(words)
          .map(word -> word.substring(0, 1).toUpperCase(Locale.ROOT).concat(word.substring(1)))
          .collect(Collectors.joining(" "));
    }
}
