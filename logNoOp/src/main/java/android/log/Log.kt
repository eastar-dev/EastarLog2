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

@file:Suppress("FunctionName", "unused", "UnusedReceiverParameter")

package android.log

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.log.Log.firstStack
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
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
    var LOG = true

    @JvmField
    var FILE_LOG: File? = null

    @JvmField
    var PREFIX = "``"

    @JvmField
    var PREFIX_MULTILINE: String = "$PREFIX▼"

    @JvmField
    var PREFIX_WIDTH = 30

    @JvmField
    var MAX_LOG_LINE_BYTE_SIZE = 3600

    @JvmField
    var NOT_PREDICATE: (StackTraceElement) -> Boolean = { false }

    @JvmStatic
    fun getLocator(stack: StackTraceElement): String = ""

    @JvmStatic
    fun getMethodName(stack: StackTraceElement): String = ""

    @JvmStatic
    fun getClzName(stack: StackTraceElement): String = ""

    fun stack(): Sequence<StackTraceElement> = emptySequence()

    internal fun firstStack(): StackTraceElement = StackTraceElement("", "", "", 0)

    internal fun _DUMP(intent: Intent?): String = ""

    internal fun _DUMP(bundle: Bundle?): String = ""

    internal fun _arrayToString(array: Any): String = ""

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
    fun obj(o: Any?): Unit = Unit

    fun debounce(vararg args: Any?): Unit = Unit

    @JvmStatic
    fun clz(clz: Class<*>): Unit = Unit


    fun provider(context: Context, uri: Uri?): Unit = Unit

    fun divider(stack: StackTraceElement = firstStack()): Unit = Unit

    fun sbc(stack: StackTraceElement = firstStack(), block: () -> Unit): Unit = Unit

    fun toast(context: Context, vararg args: Any?, duration: Int = Toast.LENGTH_SHORT, priority: Int = VERBOSE): Unit = Unit

    @JvmStatic
    fun tic_s(vararg args: Any? = arrayOf("")): Unit = Unit

    @JvmStatic
    fun tic(vararg args: Any? = arrayOf("")): Unit = Unit

    //flog
    @JvmStatic
    fun flog(vararg args: Any?): Unit = Unit

    @VisibleForTesting
    @JvmStatic
    fun println(vararg args: Any?): Unit = Unit

    @JvmOverloads
    @JvmStatic
    fun printStackTrace(th: Throwable = Throwable()): Unit = Unit

    @JvmStatic
    fun simplePrintStackTrace(): Unit = Unit

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
    @JvmStatic
    fun viewTree(parent: View, depth: Int = 0): Unit = Unit

    fun measure(widthMeasureSpec: Int, heightMeasureSpec: Int): Unit = Unit

    @JvmStatic
    fun onTouchEvent(event: MotionEvent): Unit = Unit

}

///////////////////////////////////////////////////////////////////////////
// ktx
///////////////////////////////////////////////////////////////////////////
val String?.singleLog: String
    get() = ""

///////////////////////////////////////////////////////////////////////////
// 로그 간격
///////////////////////////////////////////////////////////////////////////

fun String?._pad(width: Int = 20): String = ""
fun String?._pads(width: Int = 20): String = ""
fun Number?._pad(width: Int = 3): String = ""
fun Number?._pade(width: Int = 8): String = ""
fun Boolean?._pad(): String = ""

///////////////////////////////////////////////////////////////////////////
// dump
///////////////////////////////////////////////////////////////////////////
fun ViewModel._DUMP(): Unit = Unit
fun Activity._DUMP(): Unit = Unit

fun Fragment._DUMP(): Unit = Unit

fun Intent?._DUMP(): Unit = Unit

fun Bundle?._DUMP(): Unit = Unit


fun Lifecycle._DUMP(): Unit = Unit

fun SavedStateHandle._DUMP(): Unit = Unit

fun ContentValues._DUMP(): Unit = Unit

fun <T> T._onDump(stack: StackTraceElement = firstStack()): T = this

///////////////////////////////////////////////////////////////////////////
// image dump
///////////////////////////////////////////////////////////////////////////
fun ByteArray._DUMP(name: String = "bytes"): Unit = Unit

fun Bitmap._DUMP(name: String = "bitmap"): Unit = Unit

///////////////////////////////////////////////////////////////////////////
// db dump
///////////////////////////////////////////////////////////////////////////
fun Cursor?._DUMP(limit: Int = Int.MAX_VALUE): Unit = Unit

fun File.writeADBLogs(): Unit = Unit

fun Long.yyyymmdd(): String = ""

///////////////////////////////////////////////////////////////////////////
// Internal extension functions
///////////////////////////////////////////////////////////////////////////
internal fun String.splitSafe(lengthByte: Int): List<String> = listOf(this)

internal fun ByteArray.takeSafe(lengthByte: Int, startOffset: Int): String = ""

internal fun String.takeSafe(lengthByte: Int, startOffset: Int = 0): String = ""

