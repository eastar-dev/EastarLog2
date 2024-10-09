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

@file:Suppress("FunctionName", "unused", "MemberVisibilityCanBePrivate", "ObjectPropertyName", "DEPRECATION", "UNCHECKED_CAST")

package android.log

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.StringWriter
import java.lang.reflect.Method
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.experimental.and
import kotlin.experimental.inv

/** @author eastar*/
object Log {
    const val VERBOSE = android.util.Log.VERBOSE
    const val DEBUG = android.util.Log.DEBUG
    const val INFO = android.util.Log.INFO
    const val WARN = android.util.Log.WARN
    const val ERROR = android.util.Log.ERROR
    const val ASSERT = android.util.Log.ASSERT
    private const val LF = "\n"

    @JvmField
    var LOG = true

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
    var PREFIX_WIDTH = 30

    @JvmField
    var MAX_LOG_LINE_BYTE_SIZE = 3600

    @JvmField
    var NOT_REGEX: Regex = "".toRegex()

    @JvmField
    var NOT_PREDICATE: (StackTraceElement) -> Boolean = { false }

    @JvmStatic
    fun getLocator(stack: StackTraceElement): String = "(%s:%d)".format(stack.fileName, stack.lineNumber)

    //@JvmStatic
    //fun getLocatorWidth(stack: StackTraceElement, width: Int = LOCATOR_WIDTH): String = getLocator(stack).takePadEndWidth(width)

    @JvmStatic
    fun getMethodName(stack: StackTraceElement): String = runCatching { stack.methodName }.getOrDefault("?")

    //@JvmStatic
    //fun getMethodNameWidth(stack: StackTraceElement, width: Int = TAG_WIDTH): String = getMethodName(stack).takePadEndWidth(width)

    @JvmStatic
    fun getClzName(stack: StackTraceElement): String = runCatching { stack.className.takeLastWhile { it != '.' } }.getOrDefault(stack.className)

    fun stack() = Throwable().stackTrace
        .asSequence()
        .filterNot {
            it.fileName == null
        }
        .filterNot {
            it.lineNumber <= 0
        }
        .filterNot {
            it.fileName.endsWith("Log.kt")
        }
        .filterNot {
            it.methodName.endsWith("_onDump")
        }
        .filterNot {
            it.methodName.endsWith("_DUMP")
        }
        .filterNot {
            it.className.startsWith("androidx.")
        }
        .filterNot {
            it.className.startsWith("android.")
        }
        .filterNot {
            it.className.startsWith("com.android.")
        }
        .filterNot {
            it.className.startsWith("kotlinx.")
        }
        .filterNot {
            it.className.startsWith("kotlin.")
        }
        .filterNot {
            it.className.startsWith("io.reactivex.")
        }
        .filterNot(NOT_PREDICATE)
        .filterNot {
            NOT_REGEX.matches(it.className)
        }

    fun firstStack(): StackTraceElement = stack().first()

    //마지막 methodName에 log를 찍는다.
    private fun getStackMethod(methodName: String): StackTraceElement {
        return stack().run {
            lastOrNull { it.methodName == methodName } ?: last()
        }
    }

    //methodName을 호출한 caller를 찾는다
    private fun getStackCaller(methodName: String): StackTraceElement {
        val stackTraceElement = stack()
        val methodIndex = stackTraceElement.indexOfLast { it.methodName == methodName }
        return stackTraceElement.toList().getOrNull(methodIndex + 1) ?: stackTraceElement.last()
    }

    @JvmStatic
    fun pm(priority: Int, method: String, vararg args: Any?): Int {
        if (!LOG) return 0
        val stack = getStackMethod(method)
        return ps(priority, stack, *args)
    }

    @JvmStatic
    fun pc(priority: Int, method: String, vararg args: Any?): Int {
        if (!LOG) return 0
        val stack = getStackCaller(method)
        return ps(priority, stack, *args)
    }

    @JvmStatic
    fun pn(priority: Int, depth: Int, vararg args: Any?): Int {
        if (!LOG) return 0
        val stack = Exception().stackTrace[1 + depth]
        return ps(priority, stack, *args)
    }

    @JvmStatic
    fun p(priority: Int, vararg args: Any?): Int {
        if (!LOG) return 0
        val stack = firstStack()
        return ps(priority, stack, *args)
    }

    @JvmStatic
    fun ps(priority: Int, stack: StackTraceElement, vararg args: Any?): Int {
        if (!LOG) return 0
        val methodName = getMethodName(stack)
        val locator = getLocator(stack)
        return this.pml(priority, methodName, locator, *args)
    }

    @JvmStatic
    fun pml(priority: Int, methodName: String, locator: String, vararg args: Any?): Int {
        if (!LOG) return 0
        val msg = getMessage(*args)
        return printlnInternal(priority, methodName, locator, msg)
    }

    private fun printlnInternal(priority: Int, methodName: String?, locator: String, msg: String?): Int {
        flog(msg)

        val tag = locator
        val prefix = takePadEndWidth(methodName, PREFIX_WIDTH - 1, '.')

        msg ?: return android.util.Log.println(priority, tag, prefix + PREFIX)

        return msg.split(LF)
            .flatMap { it.splitSafe(MAX_LOG_LINE_BYTE_SIZE) }
            .run {
                when (size) {
                    0 -> android.util.Log.println(priority, tag, prefix + PREFIX)
                    1 -> android.util.Log.println(priority, tag, prefix + PREFIX + this[0])
                    else -> {
                        if (PREFIX_MULTILINE.isNotBlank()) android.util.Log.println(priority, tag, prefix + PREFIX_MULTILINE)
                        sumOf { android.util.Log.println(priority, tag, PREFIX + it) }
                    }
                }
            }
    }

    private var lastMillis: Long = 0

    fun debounce(vararg args: Any?) {
        if (!LOG) return
        if (lastMillis > System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(1))
            return
        lastMillis = System.currentTimeMillis()
        i(*args)
    }


    @JvmStatic
    fun clz(clz: Class<*>) {
        if (!LOG) return
        e(clz)
        //@formatter:off
        i("getName              ", clz.name)
        i("getPackage           ", clz.`package`)
        i("getCanonicalName     ", clz.canonicalName)
        i("getDeclaredClasses   ", clz.declaredClasses.contentToString())
        i("getClasses           ", clz.classes.contentToString())
        i("getSigners           ", clz.signers?.contentToString())
        i("getEnumConstants     ", clz.enumConstants?.contentToString())
        i("getTypeParameters    ", clz.typeParameters.contentToString())
        i("getGenericInterfaces ", clz.genericInterfaces.contentToString())
        i("getInterfaces        ", clz.interfaces.contentToString())
        if (clz.isAnnotation     ) i("isAnnotation         ", clz.isAnnotation)
        if (clz.isAnonymousClass ) i("isAnonymousClass     ", clz.isAnonymousClass)
        if (clz.isArray          ) i("isArray              ", clz.isArray)
        if (clz.isEnum           ) i("isEnum               ", clz.isEnum)
        if (clz.isInterface      ) i("isInterface          ", clz.isInterface)
        if (clz.isLocalClass     ) i("isLocalClass         ", clz.isLocalClass)
        if (clz.isMemberClass    ) i("isMemberClass        ", clz.isMemberClass)
        if (clz.isPrimitive      ) i("isPrimitive          ", clz.isPrimitive)
        if (clz.isSynthetic      ) i("isSynthetic          ", clz.isSynthetic)
        //@formatter:on
    }

    /** dump */
    private fun getMessage(vararg args: Any?): String {
        if (args.isEmpty())
            return ""

        return args.joinToString {
            runCatching {
                when (it) {
                    //@formatter:off
                    null -> "null"
                    is Class<*> -> _DUMP(it)
                    is View -> _DUMP(it)
                    is Intent -> _DUMP(it)
                    is Bundle -> _DUMP(it)
                    is Throwable -> _DUMP(it)
                    is Method -> _DUMP(it)
                    is JSONObject -> it.toString(2)
                    is JSONArray -> it.toString(2)
                    is CharSequence -> _DUMP(it.toString())
                    is ByteArray -> it.joinToString("") { "%02X".format(it) }
                    else -> it.toString()
                    //@formatter:on
                }
            }.getOrDefault("")
        }
    }

    private fun _DUMP(text: String): String = runCatching {
        when (text.first() to text.last()) {
            '[' to ']' -> JSONArray(text).toString(2)
            '{' to '}' -> JSONObject(text).toString(2)
            '<' to '>' -> prettyXml(text)
            else -> text
        }
    }.getOrDefault(text)

    private fun _DUMP(method: Method): String = method.run {
        "$modifiers ${returnType.simpleName.padEnd(20).take(20)}${declaringClass.simpleName}.$name(${parameterTypes.joinToString { it.simpleName }})"
    }

    private fun _DUMP(cls: Class<*>?): String {
        return cls?.simpleName ?: "null_Class<?>"
    }

    fun _DUMP(uri: Uri?): String {
        uri ?: return "null_Uri"
        //		return uri.toString();
        val sb = StringBuilder()
        sb.append("\r\n Uri                       $uri")
        sb.append("\r\n Scheme                    ${uri.scheme}")
        sb.append("\r\n Host                      ${uri.host}")
        sb.append("\r\n Path                      ${uri.path}")
        sb.append("\r\n LastPathSegment           ${uri.lastPathSegment}")
        sb.append("\r\n Query                     ${uri.query}")
        sb.append("\r\n Fragment                  ${uri.fragment}")
        return sb.toString()
    }

    fun _DUMP(intent: Intent?): String {
        intent ?: return "null_Intent"
        //@formatter:off
        return (intent.component?.className ?: intent.toUri(0)) +
            intent.action.ifNotBlank { "\nAction    $it" } +
            intent.data  .ifNotBlank { "\nData      $it" } +
            intent.type  .ifNotBlank { "\nType      $it" } +
            intent.scheme.ifNotBlank { "\nScheme    $it" } +
            intent.flags .ifNotBlank { "\nFlags     ${_DUMP_IntentFlags(it)}" } +
            intent.extras.ifNotBlank { "\nextra\n${_DUMP(intent.extras)}" }
        //@formatter:on
    }

    fun _DUMP(bundle: Bundle?): String {
        val b = bundle ?: return "null_Bundle"
        return b.keySet().sorted()
            .joinToString("\n") { k ->
                val v = b.get(k)
                if (v?.javaClass?.isArray == true)
                    "$k:${v.javaClass.simpleName}=${_arrayToString(v)}"
                else
                    "$k:${v?.javaClass?.simpleName}=$v"
            }
    }

    fun _DUMP_IntentFlags(flags: Int): String {
        val flagMap = mapOf(
            0x00000001 to "FLAG_GRANT_READ_URI_PERMISSION",
            0x00000002 to "FLAG_GRANT_WRITE_URI_PERMISSION",
            0x00000004 to "FLAG_FROM_BACKGROUND",
            0x00000008 to "FLAG_DEBUG_LOG_RESOLUTION",
            0x00000010 to "FLAG_EXCLUDE_STOPPED_PACKAGES",
            0x00000020 to "FLAG_INCLUDE_STOPPED_PACKAGES",
            0x00000040 to "FLAG_GRANT_PERSISTABLE_URI_PERMISSION",
            0x00000080 to "FLAG_GRANT_PREFIX_URI_PERMISSION",
            0x00000100 to "FLAG_DIRECT_BOOT_AUTO",
            0x00000200 to "FLAG_ACTIVITY_REQUIRE_DEFAULT",
            0x00000400 to "FLAG_ACTIVITY_REQUIRE_NON_BROWSER",
            0x00000800 to "FLAG_ACTIVITY_MATCH_EXTERNAL",
            0x00001000 to "FLAG_ACTIVITY_LAUNCH_ADJACENT",
            0x00002000 to "FLAG_ACTIVITY_RETAIN_IN_RECENTS",
            0x00004000 to "FLAG_ACTIVITY_TASK_ON_HOME",
            0x00008000 to "FLAG_ACTIVITY_CLEAR_TASK",
            0x00010000 to "FLAG_ACTIVITY_NO_ANIMATION",
            0x00020000 to "FLAG_ACTIVITY_REORDER_TO_FRONT",
            0x00040000 to "FLAG_ACTIVITY_NO_USER_ACTION",
            0x00080000 to "FLAG_ACTIVITY_NEW_DOCUMENT",
            0x00100000 to "FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY",
            0x00200000 to "FLAG_ACTIVITY_RESET_TASK_IF_NEEDED",
            0x00400000 to "FLAG_ACTIVITY_BROUGHT_TO_FRONT",
            0x00800000 to "FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS",
            0x01000000 to "FLAG_ACTIVITY_PREVIOUS_IS_TOP",
            0x02000000 to "FLAG_ACTIVITY_FORWARD_RESULT",
            0x04000000 to "FLAG_ACTIVITY_CLEAR_TOP",
            0x08000000 to "FLAG_ACTIVITY_MULTIPLE_TASK",
            0x10000000 to "FLAG_ACTIVITY_NEW_TASK",
            0x20000000 to "FLAG_ACTIVITY_SINGLE_TOP",
            0x40000000 to "FLAG_ACTIVITY_NO_HISTORY"
        )

        val matchedFlags = flagMap.filter { (flagValue, _) -> (flags and flagValue) != 0 }
            .values
            .joinToString(", ")

        return matchedFlags.ifEmpty {
            "Empty"
        }
    }

    fun _arrayToString(array: Any): String = when (array) {
        is IntArray -> array.joinToString(prefix = "[", postfix = "]")
        is ByteArray -> array.take(MAX_LOG_LINE_BYTE_SIZE / 2).joinToString(prefix = "${array.size}[", postfix = "]") { it.toString(16) }
        is ShortArray -> array.joinToString(prefix = "[", postfix = "]")
        is LongArray -> array.joinToString(prefix = "[", postfix = "]")
        is FloatArray -> array.joinToString(prefix = "[", postfix = "]")
        is DoubleArray -> array.joinToString(prefix = "[", postfix = "]")
        is CharArray -> array.joinToString(prefix = "[", postfix = "]")
        is BooleanArray -> array.joinToString(prefix = "[", postfix = "]")
        is Array<*> -> array.joinToString(prefix = "[", postfix = "]")
        else -> "Unsupported array type"
    }


    private inline fun <T> T?.ifNotBlank(transform: (T) -> String): String = if (this == null || (this as? CharSequence)?.isBlank() == true || (this as? Number) == 0x00) "" else transform(this)

    private fun _DUMP(th: Throwable?): String = th?.stackTraceToString() ?: "Throwable"

    @JvmStatic
    fun _DUMP_object(o: Any?): String {
        return _DUMP_object("", o, HashSet())
    }

    private fun _DUMP_object(k: String, v: Any?, duplication: MutableSet<Any>): String = kotlin.runCatching {
        if (v == null) {
            "$k:null\n"
        } else if (v.javaClass.isArray) {
            "$k:${v.javaClass.simpleName}=${_arrayToString(v)}"
        } else if (v.javaClass.isPrimitive || v.javaClass.isEnum || v is Rect || v is RectF || v is Point || v is Number || v is Boolean || v is CharSequence) {
            "$k:${v.javaClass.simpleName}=$v"
        } else {
            if (duplication.contains(v)) {
                "$k:${v.javaClass.simpleName}=[duplication]"
            } else {
                duplication.add(v)
                if (v is Collection<*>) {
                    "$k:${v.javaClass.simpleName}=\n" + v.joinToString("\n") {
                        _DUMP_object("  $k[item]", it, duplication)
                    }
                } else {
                    "$k:${v.javaClass.simpleName}=\n" + v.javaClass.declaredFields.joinToString("\n") {
                        it.isAccessible = true
                        _DUMP_object("  $k.${it.name}", it[v], duplication)
                    }
                }
            }
        }
    }.getOrDefault("$k:${v?.javaClass?.simpleName}=$v")

    fun provider(context: Context, uri: Uri?) {
        if (!LOG) return

        if (uri == null) {
            e("context==null || uri == null")
            return
        }
        context.contentResolver.query(uri, null, null, null, null).use { it._DUMP(100) }
    }

    private var ticTimer = 0L

    @JvmStatic
    fun tic_s(vararg args: Any? = arrayOf("")) {
        if (!LOG) return
        synchronized(this) {
            ticTimer = System.currentTimeMillis()
            e(String.format(Locale.getDefault(), "%,15d", 0), getMessage(*args))
        }
    }

    @JvmStatic
    fun tic(vararg args: Any? = arrayOf("")) {
        if (!LOG) return
        synchronized(this) {
            val e = System.currentTimeMillis()
            val s = ticTimer
            val interval = if (ticTimer == 0L) 0L else e - s
            ticTimer = e
            e(String.format(Locale.getDefault(), "%,15d", interval), getMessage(*args))
        }
    }

    //flog
    @JvmStatic
    fun flog(vararg args: Any?) {
        FILE_LOG ?: return
        runCatching {
            val info = firstStack()
            val lines = getMessage(*args).split(LF)

            val tag = "%-40s%-40d %-100s ``".format(Date().toString(), SystemClock.elapsedRealtime(), info.toString())

            if (lines.isNotEmpty()) {
                val token = lines.first()
                FILE_LOG!!.appendText(tag + token + LF)
            }

            val space = "%-40s%-40s %-100s ``".format("", "", "")
            repeat(lines.size) { token ->
                FILE_LOG!!.appendText(space + token + LF)
            }
        }
    }


    //xml
    @JvmStatic
    fun prettyXml(xml: String): String {
        val doc = DocumentBuilderFactory.newInstance().run {
            isValidating = false
            newDocumentBuilder().parse(xml.byteInputStream())
        }
        val source = DOMSource(doc)
        val result = StreamResult(StringWriter())

        TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.INDENT, "yes")
            setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            transform(source, result)
        }
        return result.writer.toString()
    }

    @VisibleForTesting
    @JvmStatic
    fun println(vararg args: Any?) {
        if (!LOG) return

        val stack = firstStack()
        val tag = runCatching { getClzName(stack) + "::" + getMethodName(stack) }.getOrDefault(stack.className) + ".." + getLocator(stack)
        val msg = getMessage(*args)

        val sa = msg.split(LF).flatMap { it.splitSafe(MAX_LOG_LINE_BYTE_SIZE) }
        if (sa.isEmpty()) {
            kotlin.io.println(tag + PREFIX)
            return
        }
        if (sa.size == 1) {
            kotlin.io.println(tag + sa[0])
            return
        }
        if (PREFIX_MULTILINE.isNotBlank()) kotlin.io.println(tag + PREFIX_MULTILINE)
        sa.forEach {
            kotlin.io.println(tag + it)
        }
    }

    @JvmOverloads
    @JvmStatic
    fun printStackTrace(th: Throwable = Throwable()) {
        if (!LOG) return
        w(android.util.Log.getStackTraceString(th))
    }

    @JvmStatic
    fun simplePrintStackTrace() {
        val stackTraceElements = stack()
        val first = stackTraceElements.firstOrNull() ?: stackTraceElements.first()
        val log = stackTraceElements.joinToString("\n") {
            it.className + "." + it.methodName + "(" + it.fileName + ":" + it.lineNumber + ")"
        }
        w(first.methodName, log)
    }

    /////////////////////////////////////////////////////////////////////////////
    //over lap func
    @JvmStatic
    fun println(priority: Int, vararg args: Any?): Int {
        if (!LOG) return 0
        return p(priority, *args)
    }

    @JvmStatic
    fun a(vararg args: Any?): Int {
        if (!LOG) return 0
        return p(ASSERT, *args)
    }

    @JvmStatic
    fun e(vararg args: Any?): Int {
        if (!LOG) return 0
        return p(ERROR, *args)
    }

    @JvmStatic
    fun w(vararg args: Any?): Int {
        if (!LOG) return 0
        return p(WARN, *args)
    }

    @JvmStatic
    fun i(vararg args: Any?): Int {
        if (!LOG) return 0
        return p(INFO, *args)
    }

    @JvmStatic
    fun d(vararg args: Any?): Int {
        if (!LOG) return 0
        return p(DEBUG, *args)
    }

    @JvmStatic
    fun v(vararg args: Any?): Int {
        if (!LOG) return 0

        return p(VERBOSE, *args)
    }

    //What a Terrible Failure
    @JvmStatic
    fun wtf(vararg args: Any?): Int {
        if (!LOG) return 0

        p(ASSERT, *args)
        w(android.util.Log.getStackTraceString(Throwable()))
        throw Throwable()
    }

    @JvmStatic
    fun getStackTraceString(th: Throwable): String {
        if (!LOG) return ""
        return android.util.Log.getStackTraceString(th)
    }

    ///////////////////////////////////////////////////////////////////////////
    // etc 없어질것
    ///////////////////////////////////////////////////////////////////////////
    @JvmStatic
    fun viewTree(parent: View, depth: Int = 0) {
        if (!LOG) return

        if (parent !is ViewGroup) {
            pn(ERROR, depth + 2, _DUMP(parent, 0))
            return
        }

        parent.children.forEach { child ->
            pn(ERROR, depth + 2, _DUMP(child, depth + 1))
            if (child is ViewGroup) {
                viewTree(child, depth + 1)
            }
        }
    }

    private fun _DUMP(v: View, depth: Int = 0): String {
        val space = "                    "
        val out = StringBuilder(128)
        out.append(space)
        when (v) {
            is WebView -> out.insert(depth, "W:" + Integer.toHexString(System.identityHashCode(v)) + ":" + v.title)
            is TextView -> out.insert(depth, "T:" + Integer.toHexString(System.identityHashCode(v)) + ":" + v.text)
            else -> out.insert(
                depth,
                "N:" + Integer.toHexString(System.identityHashCode(v)) + ":" + v.javaClass.simpleName
            )
        }
        out.setLength(space.length)
        val id = v.id
        val r = v.resources
        if (id != View.NO_ID && id ushr 24 != 0 && r != null) {
            val pkgname: String = when (id and -0x1000000) {
                0x7f000000 -> "app"
                0x01000000 -> "android"
                else -> r.getResourcePackageName(id)
            }
            val typename = r.getResourceTypeName(id)
            val entryname = r.getResourceEntryName(id)
            out.append(" $pkgname:$typename/$entryname")
        }
        return out.toString()
    }

    fun measure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (!LOG) return
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        d("0x%08x,0x%08x".format(widthMode, heightMode))
        d("%10d,%10d".format(widthSize, heightSize))
    }

    private var LAST_ACTION_MOVE: Long = 0

    @JvmStatic
    fun onTouchEvent(event: MotionEvent) {
        if (!LOG) return
        runCatching {
            val action = event.action and MotionEvent.ACTION_MASK
            if (action == MotionEvent.ACTION_MOVE) {
                val nanoTime = System.nanoTime()
                if (nanoTime - LAST_ACTION_MOVE < 1000000) return
                LAST_ACTION_MOVE = nanoTime
            }
            e(event)
        }
    }

    fun divider(stack: StackTraceElement = firstStack()) {
        ps(VERBOSE, stack, "=".repeat(50))
    }

    fun sbc(stack: StackTraceElement = firstStack(), block: () -> Unit) {
        ps(VERBOSE, stack, "=".repeat(50))
        block()
        ps(WARN, stack, "=".repeat(50))
    }

    ///////////////////////////////////////////////////////////////////////////
    // byte 단위로 한글짜르기
    ///////////////////////////////////////////////////////////////////////////
    internal fun String.splitSafe(lengthByte: Int): List<String> {
        require(lengthByte >= 3) { "min split length getter then 3" }
        val textByteArray = toByteArray()
        if (textByteArray.size <= lengthByte)
            return listOf(this)

        val tokens = mutableListOf<String>()
        var startOffset = 0
        while (startOffset + lengthByte < textByteArray.size) {
            val token = textByteArray.takeSafe(lengthByte, startOffset)
            tokens += token
            startOffset += token.toByteArray().size
        }
        tokens += String(textByteArray, startOffset, textByteArray.size - startOffset)
        return tokens
    }

    internal fun ByteArray.takeSafe(lengthByte: Int, startOffset: Int): String {
        if (size <= startOffset)
            return ""

        //앞에서 문자중간을 건너뜀
        var offset = startOffset
        while (size > offset && get(offset) and 0b1100_0000.toByte() == 0b1000_0000.toByte())
            offset++

        //문자열 길이가 짧은경우 끝까지
        if (size <= offset + lengthByte)
            return String(this, offset, size - offset)

        //char 중간이 아니면 거기까지
        if (get(offset + lengthByte) and 0b1100_0000.toByte() != 0b1000_0000.toByte())
            return String(this, offset, lengthByte)

        //char 중간이거나 끝이면 앞으로 땡김
        var position = offset + lengthByte
        while (get(--position) and 0b1100_0000.toByte() == 0b1000_0000.toByte()) Unit

        val charByteMoveCount = offset + lengthByte - position
        val charByteLength = get(position).inv().countLeadingZeroBits()

        return if (charByteLength == charByteMoveCount)
        //char 끝이면 거기까지
            String(this, offset, lengthByte)
        else
        //char 중간이면 뒤에버림
            String(this, offset, position - offset)
    }

    internal fun String.takeSafe(lengthByte: Int, startOffset: Int = 0) = toByteArray().takeSafe(lengthByte, startOffset)
}

///////////////////////////////////////////////////////////////////////////
// ktx
///////////////////////////////////////////////////////////////////////////
private val String?.singleLog: String
    get() = this?.toByteArray()
        ?.take(3500)
        ?.dropLastWhile { it and 0xc0.toByte() != 0x80.toByte() }
        ?.toByteArray()
        ?.let { String(it) }
        ?.replace("\r", "")
        ?.replace('\n', '↵')
        ?: ""

val Boolean?.IW: Int get() = if (this == true) android.util.Log.INFO else android.util.Log.WARN

//private inline fun <T> T?.ifNotBlank(transform: (T) -> String): String {
//    return if (this == null || (this as? CharSequence)?.isBlank() == true || (this as? Number) == 0x00) "" else transform(this)
//}

///////////////////////////////////////////////////////////////////////////
// 로그 간격
///////////////////////////////////////////////////////////////////////////
private val String.width get() = toByteArray(Charset.forName("euc-kr")).size

/**
 * 강제로 문자열의 폭을 맞추기 위해 뒤에 문자를 채워넣는다.
 * @param width 10
 * @param padChar '.'
 * 가나다 -> 가나다....
 * 가나다라마 -> 가나다라마
 * 가나다라마바 -> 가나다라마
 * 가나다라1마 -> 가나다라1.
 */
fun takePadEndWidth(text: String?, width: Int, padChar: Char = ' '): String {
    var textWidth = text?.take(width).orEmpty()
    while (textWidth.width > width)
        textWidth = textWidth.dropLast(1)
    textWidth = textWidth.padEnd(width - textWidth.width + textWidth.length, padChar)
    return textWidth
}

/**
 * 문자열의 폭을 맞추기 위해 뒤에 문자를 채워넣는다.
 */
fun padEndWidth(text: String?, width: Int, padChar: Char = ' '): String {
    val textWidth = text.orEmpty()
    return if (textWidth.width < width)
        textWidth.padEnd(textWidth.length + width - textWidth.width, padChar)
    else
        textWidth
}

/**
 * 강제로 문자열의 폭을 맞추기 위해 앞에 문자를 채워넣는다.
 * @param width 10
 * @param padChar '.'
 * 가나다 -> ....가나다
 * 가나다라마 -> 가나다라마
 * 가나다라마바 -> 나다라마바
 * 가1나다라마 -> .1가나다라
 */
fun takeLastPadStartWidth(text: String?, width: Int, padChar: Char = ' '): String {
    var textWidth = text?.takeLast(width).orEmpty()
    while (textWidth.width > width)
        textWidth = textWidth.drop(1)
    textWidth = textWidth.padStart(width - textWidth.width + textWidth.length, padChar)
    return textWidth
}

/**
 * 문자열의 폭을 맞추기 위해 앞에 문자를 채워넣는다.
 */
fun padStartWidth(text: String?, width: Int, padChar: Char = ' '): String {
    val textWidth = text.orEmpty()
    return if (textWidth.width < width)
        textWidth.padStart(textWidth.length + width - textWidth.width, padChar)
    else
        textWidth
}

///////////////////////////////////////////////////////////////////////////
// log print width align
///////////////////////////////////////////////////////////////////////////
val String?.`1` get() = takePadEndWidth("$this", 20)
val Number?.`1` get() = takePadEndWidth("$this", 3)
val Boolean?.`1` get() = takePadEndWidth("$this", 6)
fun String?._pad(width: Int = 20) = takePadEndWidth("$this", width)
fun String?._pads(width: Int = 20) = takeLastPadStartWidth("$this", width)
fun Number?._pad(width: Int = 3) = takePadEndWidth("$this", width)
fun Number?._pade(width: Int = 8) = takeLastPadStartWidth("$this", width)
fun Boolean?._pad() = takeLastPadStartWidth("$this", 6)

///////////////////////////////////////////////////////////////////////////
// toast log
///////////////////////////////////////////////////////////////////////////
var logToast: Toast? = null

fun Log.toast(context: Context, vararg args: Any?, duration: Int = Toast.LENGTH_SHORT, priority: Int = VERBOSE) {
    val text = args.joinToString(" ") { it.toString() }
    ps(priority, firstStack(), text)
    logToast = Toast.makeText(context, text, duration)
        .also {
            logToast?.cancel()
            it.show()
        }
}

///////////////////////////////////////////////////////////////////////////
// dump
///////////////////////////////////////////////////////////////////////////
@Suppress("FunctionName")
fun ViewModel._DUMP() {
    val stack = Log.firstStack()
    val prefix = "=".repeat(50) + "\n" + "${javaClass.simpleName} ${hashCode().toString(16)}\n"
    val postfix = "\n" + "=".repeat(50)
    javaClass.declaredFields.filter {
        it.type == SavedStateHandle::class.java
    }.map {
        it.isAccessible = true
        it.get(this) as SavedStateHandle
    }.firstOrNull()?.let { savedStateHandle: SavedStateHandle ->
        savedStateHandle
            .keys()
            .sorted()
            .joinToString("\n", prefix, postfix) {
                val k = it
                val v = savedStateHandle.get<Any>(it)

                if (v?.javaClass?.isArray == true)
                    "$k:${v.javaClass.simpleName}=${Log._arrayToString(v)}"
                else
                    when (v) {
                        is Intent -> "$k:${v.javaClass.simpleName}=${Log._DUMP(v)}"
                        else -> "$k:${v?.javaClass?.simpleName}=$v"
                    }
            }
    }.let {
        Log.ps(Log.DEBUG, stack, it)
    }
}

fun Activity._DUMP(): Unit = intent._DUMP()

fun Fragment._DUMP(): Unit = arguments._DUMP()

fun Intent?._DUMP() {
    val stack = Log.firstStack()
    Log.sbc {
        Log.ps(Log.DEBUG, stack, Log._DUMP(this))
    }
}

fun Bundle?._DUMP(): Unit {
    val stack = Log.firstStack()
    Log.sbc {
        Log.ps(Log.DEBUG, stack, Log._DUMP(this))
    }
}


fun Lifecycle._DUMP(): Unit = addObserver(object : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        Log.ps(Log.INFO, Log.firstStack(), "LifecycleChanged", source.javaClass.simpleName, event)
    }
})

fun SavedStateHandle._DUMP(): Unit = Log.sbc {
    keys()
        .sorted()
        .forEach {
            val k = it
            val v = get<Any>(it)
            val log = if (v?.javaClass?.isArray == true)
                "$k:${v.javaClass.simpleName}=${Log._arrayToString(v)}"
            else
                "$k:${v?.javaClass?.simpleName}=$v"
            Log.i(log)
        }
}

fun ContentValues._DUMP(): Unit = Log.sbc {
    keySet().forEach { k ->
        val v = get(k)
        Log.d("$k:${v.javaClass.simpleName}=$v")
    }
}

fun <T> T._onDump(stack: StackTraceElement = Log.firstStack()): T {
    val result = when (this) {
        is Flow<*> -> {
            onEach {
                it._onDump(stack)
            } as T
        }

        is List<*> -> {
            onEach {
                it._onDump(stack)
            }
        }

        is Result<*> -> {
            onSuccess {
                it._onDump(stack)
            }.onFailure {
                it._onDump(stack)
            } as T
        }

        is Throwable -> {
            Log.ps(Log.WARN, stack, this)
            this
        }

        else -> {
            Log.ps(Log.DEBUG, stack, this)
            this
        }
    }
    return result
}


///////////////////////////////////////////////////////////////////////////
// image
///////////////////////////////////////////////////////////////////////////
fun ByteArray._DUMP(name: String = "bytes") {
    val logDir = Log.FILE_LOG?.parentFile ?: return
    logDir.mkdirs()
    logDir.canWrite()

    File(logDir, "${name}_$timeText.jpg").writeBytes(this)
}

fun Bitmap._DUMP(name: String = "bitmap") {
    val logDir = Log.FILE_LOG?.parentFile ?: return
    logDir.mkdirs()
    logDir.canWrite()

    runCatching {
        val f = File(logDir, "${name}_$timeText.jpg")
        FileOutputStream(f).use { stream ->
            this.compress(CompressFormat.JPEG, 100, stream)
        }
    }
}

///////////////////////////////////////////////////////////////////////////
// internal util
///////////////////////////////////////////////////////////////////////////
private val timeText: String
    get() = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.ENGLISH).format(Date())


///////////////////////////////////////////////////////////////////////////
// hex util
///////////////////////////////////////////////////////////////////////////
fun ByteArray?._toHex(): String = this
    ?.joinToString("") { "%02x".format(it) }
    ?: "!!byte[]"

fun String?._toByteArray(): ByteArray = this
    ?.zipWithNext { a, b -> "$a$b" }
    ?.filterIndexed { index, _ -> index % 2 == 0 }
    ?.map { it.toInt(16).toByte() }
    ?.toByteArray()
    ?: ByteArray(0)


///////////////////////////////////////////////////////////////////////////
// fcm -> json log
///////////////////////////////////////////////////////////////////////////
/**
 *if (BuildConfig.DEBUG) {
 *    sbc { Log.i(JsonLogger.toJson(remoteMessage)) }
 *    JsonLogger.createFile(applicationContext).flog(JsonLogger.toJson(remoteMessage))
 *}
 */
object FcmLogger {
    fun toJson(remoteMessage: RemoteMessage): String = remoteMessage.data.map {
        kotlin.runCatching {
            when {
                it.value.startsWith("[") && it.value.endsWith("]") -> it.key to JSONArray(it.value)
                it.value.startsWith("{") && it.value.endsWith("}") -> it.key to JSONObject(it.value)
                else -> it.key to it.value
            }
        }.getOrDefault(it.key to it.value)
    }.let {
        JSONObject(it.toMap()).toString(2)
    }

    private lateinit var logFile: File
    fun createFile(context: Context): FcmLogger {
        logFile = File(context.filesDir, "push/push.md").also { it.parentFile?.mkdirs(); it.createNewFile() }
        return this
    }

    fun flog(json: String): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            appendTextActor().send(json)
        }
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    fun CoroutineScope.appendTextActor() = actor<String> {
        for (json in channel) {
            logFile.appendText(
                """```json
                    |$json
                    |```
                    |
                    |""".trimMargin()
            )
        }
    }
}

///////////////////////////////////////////////////////////////////////////
// db
///////////////////////////////////////////////////////////////////////////
fun Cursor?._DUMP(limit: Int = Int.MAX_VALUE): Unit = Log.sbc {
    val c = this ?: return@sbc
    Log.v("<${c.count}>")
    Log.d(c.columnNames.contentToString().trim('[', ']'))

    val dat = arrayOfNulls<String>(c.columnCount)
    if (!c.isBeforeFirst) {
        for (i in 0 until c.columnCount)
            dat[i] = if (c.getType(i) == Cursor.FIELD_TYPE_BLOB) "BLOB" else c.getString(i)
        Log.d(dat.contentToString().trim('[', ']'))
    } else {
        val keep = c.position
        while (c.moveToNext() && c.position <= limit) {
            for (i in 0 until c.columnCount)
                dat[i] = if (c.getType(i) == Cursor.FIELD_TYPE_BLOB) "BLOB" else c.getString(i)
            Log.d(dat.contentToString().trim('[', ']'))
        }
        c.moveToPosition(keep)
    }
}
