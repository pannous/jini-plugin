package com.pannous.jini.settings;

import org.junit.Test;
import org.junit.platform.commons.annotation.Testable;

import static org.junit.jupiter.api.Assertions.*;

@Testable
public class OptionsTest {
// create test for OptionsTest
    @Test
    public void testOptions() {
        Options newFile = Options.newFile;
        Options options = Options.newFile.or(Options.replace);
        assertNotEquals(newFile, options);
    }

}
