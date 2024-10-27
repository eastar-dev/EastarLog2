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

@file:Suppress("FunctionName", "unused", "MemberVisibilityCanBePrivate", "UNUSED_PARAMETER")

package android.log

import android.app.Application
import android.content.Context
import android.net.Uri
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
    var LOG_SYSOUT = false

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
    var TAG_WIDTH = 34

    @JvmField
    var LOCATOR_WIDTH = 40

    @JvmField
    var MAX_LOG_LINE_BYTE_SIZE = 3600

    @JvmField
    var defaultLogFilterClassNameRegex: Regex = "".toRegex()

    @JvmField
    var NOT_REGEX: Regex = "".toRegex()

    @JvmField
    var NOT_PREDICATE: (StackTraceElement) -> Boolean = { false }

    @JvmStatic
    fun getLocator(stack: StackTraceElement): String = ""

    @JvmStatic
    fun getMethodName(stack: StackTraceElement): String = ""

    @JvmStatic
    fun getClzName(stack: StackTraceElement): String = ""

    @JvmStatic
    fun stack(): StackTraceElement = StackTraceElement("", "", "", 0)

    @JvmStatic
    fun pm(priority: Int, method: String, vararg args: Any?): Int = 0

    @JvmStatic
    fun pc(priority: Int, method: String, vararg args: Any?): Int = 0

    @JvmStatic
    fun pn(priority: Int, depth: Int, vararg args: Any?): Int = 0

    @JvmStatic
    fun p(priority: Int, vararg args: Any?): Int = 0

    @JvmStatic
    fun ps(priority: Int, stack: StackTraceElement, vararg args: Any?): Int = 0

    @JvmStatic
    fun pml(priority: Int, methodName: String, locator: String, vararg args: Any?): Int = 0

    ///////////////////////////////////////////////////////////////////////////
    // toString for log
    ///////////////////////////////////////////////////////////////////////////
    fun toLog(vararg args: Any?): String = ""

    ///////////////////////////////////////////////////////////////////////////
    // case by log
    ///////////////////////////////////////////////////////////////////////////
    @JvmStatic
    fun obj(o: Any?): String = ""

    fun debounce(vararg args: Any?) = Unit

    @JvmStatic
    fun clz(clz: Class<*>) = Unit

    fun provider(context: Context, uri: Uri?) = Unit

    fun divider(vararg args: Any?) = Unit
    fun sbc(vararg args: Any?, action: (Any?) -> Unit) = Unit
    fun toast(vararg args: Any?) = Unit


    @JvmStatic
    fun tic_s(vararg args: Any? = arrayOf("")) = Unit

    @JvmStatic
    fun tic(vararg args: Any? = arrayOf("")) = Unit

    //flog
    @JvmStatic
    fun flog(vararg args: Any?) = Unit

    @VisibleForTesting
    @JvmStatic
    fun println(vararg args: Any?) = Unit

    @JvmOverloads
    @JvmStatic
    fun printStackTrace(th: Throwable = Throwable()) = Unit

    @JvmStatic
    fun simplePrintStackTrace() = Unit

    /////////////////////////////////////////////////////////////////////////////
    //over lap func
    @JvmStatic
    fun println(priority: Int, vararg args: Any?): Int = 0

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

    //What a Terrible Failure
    @JvmStatic
    fun wtf(vararg args: Any?): Int = 0

    @JvmStatic
    fun getStackTraceString(th: Throwable): String = ""
}

val String?.singleLog: String get() = ""

///////////////////////////////////////////////////////////////////////////
// log print width align
///////////////////////////////////////////////////////////////////////////
val Any?.`1` get() = ""
fun Any?._pad(width: Int = 0) = ""
fun Any?._pads(width: Int = 0) = ""
fun Any?._pade(width: Int = 0) = ""

///////////////////////////////////////////////////////////////////////////
// Log Ktx
///////////////////////////////////////////////////////////////////////////
fun Any?._DUMP(vararg args: Any?) = Unit
fun <T> T._onDump(stack: Any? = null): T = this

fun Application.logLifeCycle() = Unit
