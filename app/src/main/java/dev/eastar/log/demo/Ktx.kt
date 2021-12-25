package dev.eastar.log.demo

import android.content.res.Resources
import android.util.TypedValue

val Number.sp: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
        toFloat(),
        Resources.getSystem().displayMetrics)
