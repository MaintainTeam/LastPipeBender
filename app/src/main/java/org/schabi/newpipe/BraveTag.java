package org.schabi.newpipe;

public final class BraveTag {

    /**
     * This just truncate the string to have 23 chars.
     * <p>
     * This has to be <= 23 chars on devices running Android 7 or lower (API <= 25)
     * or it fails with an IllegalArgumentException
     * https://stackoverflow.com/a/54744028
     *
     * @param longTag the tag you want to shorten
     * @return the 23 chars tag string
     */
    public String tagShort23(final String longTag) {
        if (longTag.length() > 23) {
            return longTag.substring(0, 22);
        } else {
            return longTag;
        }
    }
}
