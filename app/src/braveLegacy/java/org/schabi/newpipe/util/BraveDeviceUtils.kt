package org.schabi.newpipe.util

import android.os.Build
import android.view.InputDevice

fun supportsSource(inputDevice: InputDevice, source: Int): Boolean {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        (inputDevice.sources and source) == source
    } else {
        inputDevice.supportsSource(source)
    }
}
