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

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
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
    var logFilterClassNameRegex: Regex = "".toRegex()

    @JvmField
    var logFilterPredicate: (StackTraceElement) -> Boolean = { false }

    @JvmField
    var getTag: (methodName: String?, locator: String) -> String = { _, _ -> "" }

    @JvmField
    var getPreMsg: (methodName: String?, locator: String) -> String = { _, _ -> "" }

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
    fun getStack(filterNot: Regex): StackTraceElement = StackTraceElement("", "", "", 0)

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

    fun debounce(vararg args: Any?) = Unit

    @JvmStatic
    fun clz(clz: Class<*>) = Unit

    @JvmStatic
    fun _DUMP_object(o: Any?): String = ""

    fun provider(context: Context, uri: Uri?) = Unit

    @JvmStatic
    fun tic_s(vararg args: Any? = arrayOf("")) = Unit

    @JvmStatic
    fun tic(vararg args: Any? = arrayOf("")) = Unit

    //flog
    @JvmStatic
    fun flog(vararg args: Any?) = Unit


    //xml
    @JvmStatic
    fun prettyXml(xml: String): String = ""

    @VisibleForTesting
    @JvmStatic
    fun println(vararg args: Any?) = Unit

    @JvmOverloads
    @JvmStatic
    fun printStackTrace(th: Throwable = Throwable()) = Unit

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

    ///////////////////////////////////////////////////////////////////////////
    // etc 없어질것
    ///////////////////////////////////////////////////////////////////////////
}

private val String?.singleLog: String
    get() = ""

val Boolean?.IW: Int get() = android.util.Log.INFO

///////////////////////////////////////////////////////////////////////////
// Log Ktx
///////////////////////////////////////////////////////////////////////////

fun Lifecycle._DUMP() = Unit

fun Activity._DUMP() = Unit

fun Fragment._DUMP() = Unit

fun Intent?._DUMP() = Unit
fun Instrumentation.ActivityResult._DUMP() = Unit
fun Bundle?._DUMP() = Unit
fun SavedStateHandle._DUMP() = Unit

///////////////////////////////////////////////////////////////////////////
// db
///////////////////////////////////////////////////////////////////////////

fun Cursor?._DUMP(limit: Int = Int.MAX_VALUE) = Unit

///////////////////////////////////////////////////////////////////////////
// image
///////////////////////////////////////////////////////////////////////////
fun ByteArray._DUMP(name: String = "bytes") = Unit

fun Bitmap._DUMP(name: String = "bitmap") = Unit

///////////////////////////////////////////////////////////////////////////
// internal util
///////////////////////////////////////////////////////////////////////////

fun sbc(block: () -> Unit) = Unit

///////////////////////////////////////////////////////////////////////////
// hex util
///////////////////////////////////////////////////////////////////////////
fun ByteArray?._toHex(): String = ""

fun String?._toByteArray(): ByteArray = ByteArray(0)
