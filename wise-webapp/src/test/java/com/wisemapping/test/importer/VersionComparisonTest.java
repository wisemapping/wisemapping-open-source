package com.wisemapping.test.importer;

import com.wisemapping.importer.ImporterException;
import com.wisemapping.importer.VersionNumber;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class VersionComparisonTest {

    @Test
    public void testVersionComparison() throws ImporterException {

        final VersionNumber greatest = new VersionNumber("1.0.1");
        final VersionNumber smaller = new VersionNumber("0.9.0");
        final VersionNumber intermediate = new VersionNumber("1.0.0");

        Assert.assertTrue(smaller.isSmallerThan(intermediate));
        Assert.assertFalse(greatest.isSmallerThan(intermediate));
        Assert.assertTrue(greatest.isGreaterThan(smaller));
        Assert.assertFalse(intermediate.isGreaterThan(greatest));
        Assert.assertTrue(intermediate.equals(intermediate));
        Assert.assertFalse(greatest.equals(smaller));

    }

}
