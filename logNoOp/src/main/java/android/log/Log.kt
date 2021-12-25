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
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.view.MotionEvent
import android.view.View
import java.io.File

/** @author eastar*/
object Log {
    const val VERBOSE = android.util.Log.VERBOSE
    const val DEBUG = android.util.Log.DEBUG
    const val INFO = android.util.Log.INFO
    const val WARN = android.util.Log.WARN
    const val ERROR = android.util.Log.ERROR
    const val ASSERT = android.util.Log.ASSERT
    var LOG = false
    var FILE_LOG: File? = null

    var logFilterClassNameRegex: Regex = "^android\\..+|^com\\.android\\..+|^java\\..+".toRegex()
    var logFilterPredicate: (StackTraceElement) -> Boolean = { false }

    fun getTag(stack: StackTraceElement = StackTraceElement("", "", "", 0)): String = ""

    fun String.takeLastSafe(length: Int): String = ""

    @JvmStatic
    fun p(priority: Int, vararg args: Any?): Int = 0

    @JvmStatic
    fun pt(priority: Int, tag: String, vararg args: Any?): Int = 0

    @JvmStatic
    fun ps(priority: Int, info: StackTraceElement, vararg args: Any?): Int = 0

    @JvmStatic
    fun pn(priority: Int, depth: Int, vararg args: Any?): Int = 0

    @JvmStatic
    fun pc(priority: Int, method: String, vararg args: Any?): Int = 0

    @JvmStatic
    fun pm(priority: Int, method: String, vararg args: Any?): Int = 0

    @JvmStatic
    fun toast(context: Context, vararg args: Any?) = Unit


    @JvmStatic
    fun debounce(vararg args: Any?) = Unit

    @JvmStatic
    fun viewTree(parent: View, depth: Int = 0) = Unit

    @JvmStatic
    fun clz(clz: Class<*>) = Unit

    @JvmStatic
    fun _toHexString(byteArray: ByteArray?): String = ""

    @JvmStatic
    fun _toByteArray(hexString: String): ByteArray = byteArrayOf()

    @JvmStatic
    fun _DUMP_object(o: Any?): String = ""

    fun provider(context: Context, uri: Uri?) = Unit

    var SEED_S = 0L

    @JvmStatic
    fun tic(vararg args: Any? = arrayOf("")) = Unit

    private fun cursor(c: Cursor?) = Unit

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

    class TraceLog : Throwable()

    fun println(vararg args: Any?) = Unit
    //etc
    fun String.splitSafe(lengthByte: Int): List<String> = emptyList()

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