package com.wisemapping.importer;

import java.util.*;

public class VersionNumber
        implements Comparable {

    protected String version_d;

    //~ Constructors .........................................................................................

    public VersionNumber(final String version) {
        version_d = version;
    }

    //~ Methods ..............................................................................................

    /**
     * Answers whether the receiver is greater then the given version number.
     *
     * @param versionNumber the version number to compare to
     * @return true if the receiver has a greater version number, false otherwise
     */
    public boolean isGreaterThan(final VersionNumber versionNumber) {
        return this.compareTo(versionNumber) > 0;
    }

    /**
     * Answers whether the receiver is smaller then the given version number.
     *
     * @param versionNumber the version number to compare to
     * @return true if the receiver has a smaller version number, false otherwise
     */
    public boolean isSmallerThan(final VersionNumber versionNumber) {
        return this.compareTo(versionNumber) < 0;
    }

    public String getVersion() {
        return version_d;
    }


    public int compareTo(final Object otherObject) {
        if (this.equals(otherObject)) {
            return 0;
        }

        final StringTokenizer ownTokenizer = this.getTokenizer();
        final StringTokenizer otherTokenizer = ((VersionNumber) otherObject).getTokenizer();

        while (ownTokenizer.hasMoreTokens()) {
            final int ownNumber;
            final int otherNumber;

            try {
                ownNumber = Integer.parseInt(ownTokenizer.nextToken());
                otherNumber = Integer.parseInt(otherTokenizer.nextToken());
            } catch (NoSuchElementException nseex) {
                // only possible if we have more tokens than the other version -
                // if we get to this point then we are always greater
                return 1;
            }

            if (ownNumber > otherNumber) {
                return 1;
            } else if (ownNumber < otherNumber) {
                return -1;
            }
        }

        // if other version still has tokens then it is greater than me!
        otherTokenizer.nextToken();
        return -1;
    }


    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof VersionNumber)) {
            return false;
        }

        final VersionNumber versionNumber = (VersionNumber) o;

        if (version_d != null ? !version_d.equals(versionNumber.version_d)
                : versionNumber.version_d != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return (version_d != null ? version_d.hashCode() : 0);
    }


    protected StringTokenizer getTokenizer() {
        return new StringTokenizer(this.getVersion(), ".");
    }
}
