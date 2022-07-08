/*
 * Copyright 2017 copyright eastar Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("FunctionName", "unused", "UNUSED_PARAMETER")

package android.log

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.MotionEvent
import android.view.View
import androidx.annotation.VisibleForTesting
import java.io.File

/** @author eastar*/
object Log {
    const val VERBOSE = android.util.Log.VERBOSE
    const val DEBUG = android.util.Log.DEBUG
    const val INFO = android.util.Log.INFO
    const val WARN = android.util.Log.WARN
    const val ERROR = android.util.Log.ERROR
    const val ASSERT = android.util.Log.ASSERT

    @JvmField
    var LOG = false

    @JvmField
    var FILE_LOG: File? = null

    @JvmField
    var START = android.util.Log.DEBUG

    @JvmField
    var LIFECYCLE_CREATE = android.util.Log.DEBUG

    @JvmField
    var LIFECYCLE_DESTROYED = android.util.Log.DEBUG

    @JvmField
    var PREFIX = "``"

    @JvmField
    var PREFIX_MULTILINE: String = "$PREFIX▼"

    @JvmField
    var TAG_WIDTH = 35

    @JvmField
    var LOCATOR_WIDTH = 40

    @JvmField
    var MAX_LOG_LINE_BYTE_SIZE = 3600

    @JvmField
    var defaultLogFilterClassNameRegex: Regex = "".toRegex()

    @JvmField
    var logFilterClassNameRegex: Regex = "".toRegex()

    @JvmField
    var logFilterPredicate: (StackTraceElement) -> Boolean = { false }

    @JvmStatic
    fun getLocator(stack: StackTraceElement): String = ""

    @JvmStatic
    fun getLocatorWidth(stack: StackTraceElement, width: Int = LOCATOR_WIDTH): String = ""

    @JvmStatic
    fun getMethodName(stack: StackTraceElement): String = ""

    @JvmStatic
    fun getMethodNameWidth(stack: StackTraceElement, width: Int = TAG_WIDTH): String = ""

    @JvmStatic
    fun getClzName(stack: StackTraceElement): String = ""

    @JvmStatic
    fun getClzMethod(stack: StackTraceElement): String = ""

    @JvmStatic
    fun pm(priority: Int, method: String, vararg args: Any?): Int = 0

    @JvmStatic
    fun pc(priority: Int, method: String, vararg args: Any?): Int = 0

    @JvmStatic
    fun pl(priority: Int, tag: String, locator: String, vararg args: Any?): Int = 0

    @JvmStatic
    fun pn(priority: Int, depth: Int, vararg args: Any?): Int = 0

    @JvmStatic
    fun p(priority: Int, vararg args: Any?): Int = 0

    @JvmStatic
    fun ps(priority: Int, stack: StackTraceElement, vararg args: Any?): Int = 0

    @JvmStatic
    fun ptl(priority: Int, tag: String, locator: String, vararg args: Any?): Int = 0


    @JvmStatic
    fun toast(context: Context, vararg args: Any?) = Unit


    @JvmStatic
    fun debounce(vararg args: Any?) = Unit

    @JvmStatic
    fun viewTree(parent: View, depth: Int = 0) = Unit

    @JvmStatic
    fun clz(clz: Class<*>) = Unit

    /** dump */

    @JvmStatic
    fun _toHexString(byteArray: ByteArray?): String = ""

    @JvmStatic
    fun _toByteArray(hexString: String): ByteArray = "".toByteArray()

    @JvmStatic
    fun _DUMP_object(o: Any?): String = ""


    fun provider(context: Context, uri: Uri?) = Unit


    @JvmStatic
    fun tic(vararg args: Any? = arrayOf("")) = Unit


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //image save
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @JvmStatic
    fun compress(name: String, data: ByteArray) = Unit

    @JvmStatic
    fun compress(name: String, bmp: Bitmap) = Unit


    //flog
    @JvmStatic
    fun flog(vararg args: Any?) = Unit

    fun measure(widthMeasureSpec: Int, heightMeasureSpec: Int) = Unit


    @JvmStatic
    fun onTouchEvent(event: MotionEvent) = Unit

    //xml
    @JvmStatic
    fun prettyXml(xml: String): String = ""

    class TraceLog : Throwable()

    @VisibleForTesting
    @JvmStatic
    fun println(vararg args: Any?) = Unit

    /////////////////////////////////////////////////////////////////////////////
    //over lap func
    @JvmStatic
    fun println(priority: Int, tag: String?, msg: String?): Int = 0

    @JvmStatic
    fun a(vararg args: Any?): Int = 0

    @JvmStatic
    fun e(vararg args: Any?): Int = 0

    @JvmStatic
    fun w(vararg args: Any?): Int = 0

    @JvmStatic
    fun i(vararg args: Any?): Int = 0

    @JvmStatic
    fun d(vararg args: Any?): Int = 0

    @JvmStatic
    fun v(vararg args: Any?): Int = 0

    @JvmStatic
    fun printStackTrace() = Unit

    @JvmStatic
    fun printStackTrace(th: Throwable) = Unit

    @JvmStatic
    fun getStackTraceString(th: Throwable): String = ""

}
