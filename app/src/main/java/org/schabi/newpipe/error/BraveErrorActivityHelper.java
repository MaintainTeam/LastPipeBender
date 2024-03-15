package org.schabi.newpipe.error;

import java.util.ArrayList;
import java.util.List;

public final class BraveErrorActivityHelper {

    private BraveErrorActivityHelper() {
    }

    /**
     * Skip some traces as we might get TransactionTooLargeException exception.
     *
     * @param stackTraces the full stack traces
     * @return the truncated traces list that will not crash the Binder or whatever.
     */
    public static List<String> truncateAsNeeded(final String[] stackTraces) {
        final int limit = 104857; // limit to around 100k

        int size = 0;
        final List<String> finalList = new ArrayList<>();

        for (final String trace : stackTraces) {
            if (limit < size) {
                finalList.add("BraveNewPipe TRUNCATED trace");
                break;
            }
            size += trace.length();
            finalList.add(trace);
        }
        return finalList;
    }
}
