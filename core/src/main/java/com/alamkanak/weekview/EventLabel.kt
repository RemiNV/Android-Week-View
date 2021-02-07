package com.alamkanak.weekview

import android.text.StaticLayout

internal class EventLabel private constructor(
    private var layoutField: StaticLayout,
    private var cachedWidth: Float,
    private var cachedHeight: Float
) {
    val layout
        get() = layoutField

    constructor(
        availableWidth: Float,
        availableHeight: Float,
        eventChip: EventChip,
        textFitter: TextFitter
    ) : this(textFitter.fit(eventChip), availableWidth, availableHeight)

    fun calculateLayout(
        availableWidth: Float,
        availableHeight: Float,
        eventChip: EventChip,
        textFitter: TextFitter
    ) {
        if (cachedWidth == availableWidth && cachedHeight == availableHeight) return
        layoutField = textFitter.fit(eventChip)
        cachedWidth = availableWidth
        cachedHeight = availableHeight
    }
}
