package com.alamkanak.weekview

import android.os.Parcel
import android.os.Parcelable
import android.view.View.BaseSavedState
import java.time.LocalDate

internal class SavedState : BaseSavedState {

    var numberOfVisibleDays: Int = 0
    var firstVisibleDate: LocalDate = LocalDate.now()

    constructor(superState: Parcelable) : super(superState)

    constructor(
        superState: Parcelable,
        numberOfVisibleDays: Int,
        firstVisibleDate: LocalDate
    ) : super(superState) {
        this.numberOfVisibleDays = numberOfVisibleDays
        this.firstVisibleDate = firstVisibleDate
    }

    constructor(source: Parcel) : super(source) {
        numberOfVisibleDays = source.readInt()
        firstVisibleDate = source.readSerializable() as LocalDate
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeInt(numberOfVisibleDays)
        out.writeSerializable(firstVisibleDate)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
            override fun createFromParcel(source: Parcel) = SavedState(source)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }
}
