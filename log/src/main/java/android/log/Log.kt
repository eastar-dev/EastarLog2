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

@file:Suppress("FunctionName", "unused", "MemberVisibilityCanBePrivate", "UNUSED_ANONYMOUS_PARAMETER")

package android.log

import android.app.Activity
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
import androidx.activity.result.ActivityResult
import androidx.annotation.VisibleForTesting
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
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
    var defaultLogFilterClassNameRegex: Regex = "^android\\..+|^com\\.android\\..+|^java\\..+".toRegex()

    @JvmField
    var logFilterClassNameRegex: Regex = "".toRegex()

    @JvmField
    var filterNotPredicate: (StackTraceElement) -> Boolean = { false }

    @JvmField
    var getTag: (methodName: String?, locator: String) -> String = { methodName, locator ->
        (methodName ?: "").takePadEndSafeWidth(TAG_WIDTH)
    }

    @JvmField
    var getPreMsg: (methodName: String?, locator: String) -> String = { methodName, locator ->
        locator.padEndWidth(LOCATOR_WIDTH)
    }

    @JvmStatic
    fun getLocator(stack: StackTraceElement): String = "(%s:%d)".format(stack.fileName, stack.lineNumber)

    @JvmStatic
    fun getLocatorWidth(stack: StackTraceElement, width: Int = LOCATOR_WIDTH): String = getLocator(stack).takePadEndSafeWidth(width)

    @JvmStatic
    fun getMethodName(stack: StackTraceElement): String = runCatching { stack.methodName }.getOrDefault("?")

    @JvmStatic
    fun getMethodNameWidth(stack: StackTraceElement, width: Int = TAG_WIDTH): String = getMethodName(stack).takePadEndSafeWidth(width)

    @JvmStatic
    fun getClzName(stack: StackTraceElement): String = runCatching { stack.className.takeLastWhile { it != '.' } }.getOrDefault(stack.className)

    @JvmStatic
    fun getClzMethod(stack: StackTraceElement): String = runCatching { getClzName(stack) + "::" + getMethodName(stack) }.getOrDefault(stack.className)

    //외부에서 직접 지정할때 사용
    @JvmStatic
    fun getStack(filterNot: Regex): StackTraceElement {
        val stackTraceElement = Throwable().stackTrace
            .filterNot { it.className == this.javaClass.name }

        return stackTraceElement
            .asSequence()
            .filterNot { it.className.matches(filterNot) }
            .filterNot { it.lineNumber < 0 }
            .firstOrNull()
            ?: stackTraceElement.first()
    }

    private fun getStack(): StackTraceElement {
        val stackTraceElement = Throwable().stackTrace
            .filterNot { it.className == this.javaClass.name }

        return stackTraceElement
            .asSequence()
            .filterNot { it.className.matches(defaultLogFilterClassNameRegex) }
            .filterNot { it.className.matches(logFilterClassNameRegex) }
            .filterNot(filterNotPredicate)
            .filterNot { it.lineNumber < 0 }
            .firstOrNull()
            ?: stackTraceElement.first()
    }

    //methodName을 호출한 caller를 찾는다
    private fun getStackMethod(methodName: String): StackTraceElement {
        val stackTraceElement = Throwable().stackTrace
            .filterNot { it.className == this.javaClass.name }

        return stackTraceElement
            .filterNot(filterNotPredicate)
            .lastOrNull { it.methodName == methodName }
            ?: stackTraceElement.last()
    }

    //methodName을 호출한 caller를 찾는다
    private fun getStackCaller(methodName: String): StackTraceElement {
        val stackTraceElement = Throwable().stackTrace
            .filterNot { it.className == this.javaClass.name }

        val methodIndex = stackTraceElement.indexOfLast { it.methodName == methodName }

        return stackTraceElement
            .getOrElse(methodIndex + 1) {
                stackTraceElement.last()
            }
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
        val stack = getStack()
        return ps(priority, stack, *args)
    }

    @JvmStatic
    fun ps(priority: Int, stack: StackTraceElement, vararg args: Any?): Int {
        if (!LOG) return 0
        if (LOG_SYSOUT) return println(*args).let { 0 }
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

        val preMsg = getPreMsg(methodName, locator)
        val tag = getTag(methodName, locator)

        msg ?: return android.util.Log.println(priority, tag, preMsg + PREFIX)

        return msg.split(LF)
            .flatMap { it.splitSafe(MAX_LOG_LINE_BYTE_SIZE) }
            .run {
                when (size) {
                    0 -> android.util.Log.println(priority, tag, preMsg + PREFIX)
                    1 -> android.util.Log.println(priority, tag, preMsg + PREFIX + this[0])
                    else -> {
                        if (PREFIX_MULTILINE.isNotBlank()) android.util.Log.println(priority, tag, preMsg + PREFIX_MULTILINE)
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

    private fun _DUMP(uri: Uri?): String {
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

    internal fun _DUMP(intent: Intent?): String {
        intent ?: return "null_Intent"
        //@formatter:off
        return (intent.component?.className ?: intent.toUri(0)) +
            intent.action.ifNotBlank { "\nAction    $it" } +
            intent.data  .ifNotBlank { "\nData      $it" } +
            intent.type  .ifNotBlank { "\nType      $it" } +
            intent.scheme.ifNotBlank { "\nScheme    $it" } +
            intent.flags .ifNotBlank { "\nFlags     ${Integer.toHexString(it)}" } +
            intent.extras.ifNotBlank { "\nextra\n${_DUMP(intent.extras)}" }
        //@formatter:on
    }

    @Suppress("DEPRECATION")
    internal fun _DUMP(bundle: Bundle?): String {
        val b = bundle ?: return "null_Bundle"
        return b.keySet().sorted()
            .joinToString("\n") { k ->
                val v = b.get(k)
                if (v?.javaClass?.isArray == true)
                    "$k:${v.javaClass.simpleName}=${(v as Array<*>).contentToString()}"
                else
                    "$k:${v?.javaClass?.simpleName}=$v"
            }
    }

    private inline fun <T> T?.ifNotBlank(transform: (T) -> String): String = if (this == null || (this as? CharSequence)?.isBlank() == true || (this as? Number) == 0x00) "" else transform(this)

    private fun _DUMP(th: Throwable?): String = th?.stackTraceToString() ?: "Throwable"

    @JvmStatic
    fun _DUMP_object(o: Any?): String {
        return _DUMP_object("", o, HashSet())
    }

    private fun _DUMP_object(name: String, value: Any?, duplication: MutableSet<Any>): String {
        val sb = StringBuilder()
        try {
            if (value == null)
                return "null"

            if (value.javaClass.isArray) {
                //@formatter:off
                sb.append(name).append('<').append(value.javaClass.simpleName).append('>').append(" = ")
                val componentType = value.javaClass.componentType ?: return "null"
                when {
                    Byte::class.java.isAssignableFrom(componentType) -> sb.append(if ((value as ByteArray).size < MAX_LOG_LINE_BYTE_SIZE) String((value as ByteArray?)!!) else "[" + value.size + "]")
                    Char::class.java.isAssignableFrom(componentType) -> sb.append(String((value as CharArray?)!!))
                    else -> sb.append((value as Array<*>).contentToString())
                }
                //@formatter:on
            } else if (value.javaClass.isPrimitive || value.javaClass.isEnum || value is Rect || value is RectF || value is Point || value is Number || value is Boolean || value is CharSequence) {
                sb.append(name).append('<').append(value.javaClass.simpleName).append('>').append(" = ")
                sb.append(value.toString())
            } else {
                if (duplication.contains(value)) {
                    sb.append(name).append('<').append(value.javaClass.simpleName).append('>').append(" = ")
                    sb.append("[duplication]\n")
                    return sb.toString()
                }
                duplication.add(value)
                if (value is Collection<*>) {
                    sb.append(name).append('<').append(value.javaClass.simpleName).append('>').append(" = ")
                        .append(":\n")
                    val it = value.iterator()
                    while (it.hasNext()) sb.append(_DUMP_object("  $name[item]", it.next(), duplication))
                } else {
                    val clz: Class<*> = value.javaClass
                    sb.append(name).append('<').append(value.javaClass.simpleName).append('>').append(" = ")
                        .append(":\n")
                    for (f in clz.declaredFields) {
                        f.isAccessible = true
                        sb.append(_DUMP_object("  " + name + "." + f.name, f[value], duplication))
                    }
                }
            }
            sb.append("\n")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sb.toString()
    }

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
            val info = getStack()
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

        val stack = getStack()
        val tag = getClzMethod(stack) + ".." + getLocator(stack)
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
        d(String.format("0x%08x,0x%08x", widthMode, heightMode))
        d(String.format("%10d,%10d", widthSize, heightSize))
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
}

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
private val String.width get() = toByteArray(Charset.forName("euc-kr")).size
private fun String.padEndWidth(width: Int = Log.TAG_WIDTH, padChar: Char = '.'): String =
    if (width - this.width > 0)
        padEnd(length + width - this.width, padChar)
    else
        this

internal fun String.takeLastPadStartSafeWidth(length: Int = Log.TAG_WIDTH): String {
    var text = takeLast(length)
    while (text.width != length) {
        text = if (text.width > length)
            text.drop(1)
        else
            text.padStart(length - text.width + text.length, '.')
    }
    return text
}

internal fun String.takePadEndSafeWidth(length: Int = Log.TAG_WIDTH): String {
    var text = take(length)
    while (text.width != length) {
        text = if (text.width > length)
            text.dropLast(1)
        else
            text.padEnd(length - text.width + text.length, '.')
    }
    return text
}

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

///////////////////////////////////////////////////////////////////////////
// Log Ktx
///////////////////////////////////////////////////////////////////////////
private val stack: StackTraceElement
    get() = Exception().stackTrace.run {
        filterNot {
            it.className.startsWith("android.log")
        }.filterNot {
            it.lineNumber < 0
        }.firstOrNull() ?: first()
    }

fun Lifecycle._DUMP(): Unit = addObserver(object : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        Log.ps(Log.INFO, stack, "LifecycleChanged", source.javaClass.simpleName, event)
    }
})

fun Activity._DUMP(): Unit = sbc {
    Log.i(intent)
}

fun Fragment._DUMP(): Unit = sbc {
    Log.i(arguments)
}

fun Intent?._DUMP(): Unit = sbc {
    Log.i(this)
}

fun ActivityResult._DUMP(): Unit = sbc {
    Log.i(this.resultCode)
    Log.i(this.data)
}

fun Bundle?._DUMP(): Unit = sbc {
    Log.i(this)
}

fun SavedStateHandle._DUMP(): Unit = sbc {
    keys()
        .sorted()
        .forEach {
            val k = it
            val v = get<Any>(it)
            val log = if (v?.javaClass?.isArray == true)
                "$k:${v.javaClass.simpleName}=${(v as Array<*>).contentToString()}"
            else
                "$k:${v?.javaClass?.simpleName}=$v"
            Log.i(log)
        }
}

///////////////////////////////////////////////////////////////////////////
// db
///////////////////////////////////////////////////////////////////////////

fun Cursor?._DUMP(limit: Int = Int.MAX_VALUE): Unit = sbc {
    val c = this ?: return@sbc
    Log.i("<${c.count}>")
    Log.i(c.columnNames.contentToString())

    val dat = arrayOfNulls<String>(c.columnCount)
    if (!c.isBeforeFirst) {
        for (i in 0 until c.columnCount)
            dat[i] = if (c.getType(i) == Cursor.FIELD_TYPE_BLOB) "BLOB" else c.getString(i)
        Log.i(dat.contentToString())
    } else {
        val keep = c.position
        while (c.moveToNext() && c.position <= limit) {
            for (i in 0 until c.columnCount)
                dat[i] = if (c.getType(i) == Cursor.FIELD_TYPE_BLOB) "BLOB" else c.getString(i)
            Log.i(dat.contentToString())
        }
        c.moveToPosition(keep)
    }
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

fun sbc(block: () -> Unit) {
    Log.ps(Log.VERBOSE, stack, "=".repeat(50))
    block()
    Log.w("=".repeat(50))
}

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