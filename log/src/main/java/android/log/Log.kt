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

@file:Suppress("FunctionName", "unused", "MemberVisibilityCanBePrivate")

package android.log

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.StringWriter
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Date
import java.util.Locale
import java.util.StringTokenizer
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

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
    var TAG_WIDTH = 35

    @JvmField
    var LOCATOR_WIDTH = 40

    @JvmField
    var MAX_LOG_LINE_BYTE_SIZE = 3600

    @JvmField
    var defaultLogFilterClassNameRegex: Regex = "^android\\..+|^com\\.android\\..+|^java\\..+".toRegex()

    @JvmField
    var logFilterClassNameRegex: Regex = "".toRegex()

    @JvmField
    var logFilterPredicate: (StackTraceElement) -> Boolean = { false }

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

    @JvmStatic
    private fun getStackFilter(filterClassNameRegex: String? = null): StackTraceElement {
        return Exception().stackTrace.filterNot {
            it.className == javaClass.name
        }.run {
            asSequence().filterNot {
                it.className.matches(defaultLogFilterClassNameRegex)
            }.filterNot { stackTraceElement ->
                filterClassNameRegex?.let { stackTraceElement.className.matches(filterClassNameRegex.toRegex()) } ?: false
            }.filterNot {
                it.lineNumber < 0
            }.firstOrNull() ?: first()
        }
    }

    private fun getStack(): StackTraceElement {
        return Exception().stackTrace.filterNot {
            it.className == javaClass.name
        }.run {
            asSequence().filterNot {
                it.className.matches(defaultLogFilterClassNameRegex)
            }.filterNot {
                it.className.matches(logFilterClassNameRegex)
            }.filterNot(logFilterPredicate).filterNot {
                it.lineNumber < 0
            }.firstOrNull() ?: first()
        }
    }

    private fun getStackMethod(methodNameKey: String): StackTraceElement {
        return Exception().stackTrace.filterNot {
            it.className == javaClass.name
        }.run {
            filterNot(logFilterPredicate)
                .lastOrNull { it.methodName == methodNameKey } ?: last()
        }
    }

    private fun getStackCaller(methodNameKey: String): StackTraceElement {
        return Exception().stackTrace.filterNot {
            it.className == javaClass.name
        }.run {
            filterNot(logFilterPredicate)
                .getOrNull(indexOfLast { it.methodName == methodNameKey } + 1) ?: last()
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
    fun pl(priority: Int, tag: String, locator: String, vararg args: Any?): Int {
        if (!LOG) return 0
        val msg = getMessage(*args)
        return printlnInternal(priority, tag.takePadEndSafeWidth(TAG_WIDTH), msg, locator.takePadEndSafeWidth(LOCATOR_WIDTH))
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
        val tag = getMethodNameWidth(stack)
        val locator = getLocatorWidth(stack)
        return ptl(priority, tag, locator, *args)
    }

    @JvmStatic
    fun ptl(priority: Int, tag: String, locator: String, vararg args: Any?): Int {
        if (!LOG) return 0
        val msg = getMessage(*args)
        return printlnInternal(priority, tag, msg, locator)
    }


    private fun printlnInternal(priority: Int, tag: String?, msg: String?, locator: String = "".padEnd(LOCATOR_WIDTH, '.')): Int {
        flog(msg)

        msg ?: return android.util.Log.println(priority, tag, locator + PREFIX)

        return msg.split(LF)
            .flatMap { it.splitSafe(MAX_LOG_LINE_BYTE_SIZE) }
            .run {
                when (size) {
                    0 -> android.util.Log.println(priority, tag, locator + PREFIX)
                    1 -> android.util.Log.println(priority, tag, locator + PREFIX + this[0])
                    else -> {
                        if (PREFIX_MULTILINE.isNotBlank()) android.util.Log.println(priority, tag, locator + PREFIX_MULTILINE)
                        sumOf { android.util.Log.println(priority, tag, PREFIX + it) }
                    }
                }
            }
    }


    @JvmStatic
    fun toast(context: Context, vararg args: Any?) {
        if (!LOG) return
        p(ERROR, *args)
        Toast.makeText(context, getMessage(*args), Toast.LENGTH_SHORT).show()
    }

    private var timeout: Long = 0

    @JvmStatic
    fun debounce(vararg args: Any?) {
        if (!LOG) return
        if (timeout < System.nanoTime() - TimeUnit.SECONDS.toNanos(1))
            return
        timeout = System.nanoTime()
        p(ERROR, *args)
    }

    @JvmStatic
    fun viewTree(parent: View, depth: Int = 0) {
        if (!LOG) return
        if (parent !is ViewGroup) {
            pn(ERROR, depth + 2, _DUMP(parent, 0))
            return
        }

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            pn(ERROR, depth + 2, _DUMP(child, depth))
            if (child is ViewGroup)
                viewTree(child, depth + 1)
        }
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
        "$modifiers ${
            returnType.simpleName.padEnd(20).take(20)
        }${declaringClass.simpleName}.$name(${parameterTypes.joinToString { it.simpleName }})"
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

    private fun _DUMP(bundle: Bundle?): String {
        if (bundle == null) return "null_Bundle"
        val sb = StringBuilder()
        bundle.keySet().forEach {
            val o = bundle[it]
            when {
                o == null -> sb.append("Object $it;//null")
                o.javaClass.isArray -> sb.append(o.javaClass.simpleName + " " + it + ";//" + (o as Array<*>).contentToString())
                else -> sb.append(o.javaClass.simpleName + " " + it + ";//" + o.toString())
            }
            sb.append("\n")
        }
        return sb.toString()
    }

    private fun _DUMP(cls: Class<*>?): String {
        return cls?.simpleName ?: "null_Class<?>"
    }

    private fun _DUMP(uri: Uri?): String {
        if (uri == null) return "null_Uri"
        //		return uri.toString();
        val sb = StringBuilder()
        sb.append("\r\n Uri                       ").append(uri.toString())
        sb.append("\r\n Scheme                    ").append(if (uri.scheme != null) uri.scheme else "null")
        sb.append("\r\n Host                      ").append(if (uri.host != null) uri.host else "null")
        sb.append("\r\n Path                      ").append(if (uri.path != null) uri.path else "null")
        sb.append("\r\n LastPathSegment           ")
            .append(if (uri.lastPathSegment != null) uri.lastPathSegment else "null")
        sb.append("\r\n Query                     ").append(if (uri.query != null) uri.query else "null")
        sb.append("\r\n Fragment                  ").append(if (uri.fragment != null) uri.fragment else "null")
        return sb.toString()
    }

    private fun _DUMP(intent: Intent?): String {
        if (intent == null) return "null_Intent"
        val sb = StringBuilder()
        //@formatter:off
        sb.append(if (intent.action     != null) (if (sb.isNotEmpty()) "\n" else "") + "Action     " + intent.action    .toString() else "")
        sb.append(if (intent.data       != null) (if (sb.isNotEmpty()) "\n" else "") + "Data       " + intent.data      .toString() else "")
        sb.append(if (intent.categories != null) (if (sb.isNotEmpty()) "\n" else "") + "Categories " + intent.categories.toString() else "")
        sb.append(if (intent.type       != null) (if (sb.isNotEmpty()) "\n" else "") + "Type       " + intent.type      .toString() else "")
        sb.append(if (intent.scheme     != null) (if (sb.isNotEmpty()) "\n" else "") + "Scheme     " + intent.scheme    .toString() else "")
        sb.append(if (intent.`package`  != null) (if (sb.isNotEmpty()) "\n" else "") + "Package    " + intent.`package` .toString() else "")
        sb.append(if (intent.component  != null) (if (sb.isNotEmpty()) "\n" else "") + "Component  " + intent.component .toString() else "")
        sb.append(if (intent.flags      != 0x00) (if (sb.isNotEmpty()) "\n" else "") + "Flags      " + Integer.toHexString(intent.flags) else "")
        //@formatter:on
        if (intent.extras != null) sb.append((if (sb.isNotEmpty()) "\n" else "") + _DUMP(intent.extras))
        return sb.toString()
    }

    private fun _DUMP(th: Throwable?): String = th?.stackTraceToString() ?: "Throwable"

    @JvmStatic
    fun _toHexString(byteArray: ByteArray?): String =
        byteArray?.joinToString("") { "%02x".format(it) } ?: "!!byte[]"

    @JvmStatic
    fun _toByteArray(hexString: String): ByteArray = hexString.zipWithNext { a, b -> "$a$b" }
        .filterIndexed { index, _ -> index % 2 == 0 }
        .map { it.toInt(16).toByte() }
        .toByteArray()

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
                val componentType = value.javaClass.componentType!!
                when {
                    Boolean ::class.javaPrimitiveType!!.isAssignableFrom(componentType) -> sb.append(Arrays.toString(value as BooleanArray?))
                    Byte    ::class.javaPrimitiveType!!.isAssignableFrom(componentType) -> sb.append(if ((value as ByteArray).size < MAX_LOG_LINE_BYTE_SIZE) String((value as ByteArray?)!!) else "[" + value.size + "]")
                    Char    ::class.javaPrimitiveType!!.isAssignableFrom(componentType) -> sb.append(String((value as CharArray?)!!))
                    Double  ::class.javaPrimitiveType!!.isAssignableFrom(componentType) -> sb.append(Arrays.toString(value as DoubleArray?))
                    Float   ::class.javaPrimitiveType!!.isAssignableFrom(componentType) -> sb.append(Arrays.toString(value as FloatArray?))
                    Int     ::class.javaPrimitiveType!!.isAssignableFrom(componentType) -> sb.append(Arrays.toString(value as IntArray?))
                    Long    ::class.javaPrimitiveType!!.isAssignableFrom(componentType) -> sb.append(Arrays.toString(value as LongArray?))
                    Short   ::class.javaPrimitiveType!!.isAssignableFrom(componentType) -> sb.append(Arrays.toString(value as ShortArray?))
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
        context.contentResolver.query(uri, null, null, null, null).use { cursor(it) }
    }

    private var SEED_S = 0L

    @JvmStatic
    fun tic(vararg args: Any? = arrayOf("")) {
        if (!LOG) return
        synchronized(this) {
            val e = System.currentTimeMillis()
            val s = SEED_S
            val interval = if (SEED_S == 0L) 0L else e - s
            SEED_S = e
            e(String.format(Locale.getDefault(), "%,15d", interval), getMessage(*args))
        }
    }

    private fun cursor(c: Cursor?) {
        c ?: return
        e("<${c.count}>")
        e(c.columnNames)

        val dat = arrayOfNulls<String>(c.columnCount)
        if (!c.isBeforeFirst) {
            for (i in 0 until c.columnCount)
                dat[i] = if (c.getType(i) == Cursor.FIELD_TYPE_BLOB) "BLOB" else c.getString(i)
            e(dat.contentToString())
        } else {
            val keep = c.position
            while (c.moveToNext()) {
                for (i in 0 until c.columnCount)
                    dat[i] = if (c.getType(i) == Cursor.FIELD_TYPE_BLOB) "BLOB" else c.getString(i)
                e(dat.contentToString())
            }
            c.moveToPosition(keep)
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //image save
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @JvmStatic
    fun compress(name: String, data: ByteArray) {
        FILE_LOG ?: return
        runCatching {
            FILE_LOG!!.parentFile?.also {
                it.mkdirs()
                it.canWrite()
                val f = File(it, timeText + "_" + name + ".jpg")
                FileOutputStream(f).use { stream ->
                    BitmapFactory.decodeByteArray(data, 0, data.size)
                        .compress(CompressFormat.JPEG, 100, stream)
                }
            }
        }
    }

    @JvmStatic
    fun compress(name: String, bmp: Bitmap) {
        FILE_LOG ?: return
        runCatching {
            FILE_LOG!!.parentFile?.also {
                it.mkdirs()
                it.canWrite()
                val f = File(it, timeText + "_" + name + ".jpg")
                FileOutputStream(f).use { stream ->
                    bmp.compress(CompressFormat.JPEG, 100, stream)
                }
            }
        }
    }

    private val timeText: String
        get() = SimpleDateFormat(
            "yyyyMMdd_HHmmss_SSS",
            Locale.ENGLISH
        ).format(Date())

    //flog
    @JvmStatic
    fun flog(vararg args: Any?) {
        FILE_LOG ?: return
        runCatching {
            val info = getStack()
            val log: String = getMessage(*args)
            val st = StringTokenizer(log, LF, false)

            val tag =
                "%-40s%-40d %-100s ``".format(Date().toString(), SystemClock.elapsedRealtime(), info.toString())
            if (st.hasMoreTokens()) {
                val token = st.nextToken()
                FILE_LOG!!.appendText(tag + token + LF)
            }

            val space = "%-40s%-40s %-100s ``".format("", "", "")
            while (st.hasMoreTokens()) {
                val token = st.nextToken()
                FILE_LOG!!.appendText(space + token + LF)
            }
        }
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

    class TraceLog : Throwable()

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

    /////////////////////////////////////////////////////////////////////////////
    //over lap func
    @JvmStatic
    fun println(priority: Int, tag: String?, msg: String?): Int {
        if (!LOG) return 0
        return p(priority, tag, msg)
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

    @JvmStatic
    fun printStackTrace() {
        if (!LOG) return
        TraceLog().printStackTrace()
    }

    @JvmStatic
    fun printStackTrace(th: Throwable) {
        if (!LOG) return
        th.printStackTrace()
    }

    @JvmStatic
    fun getStackTraceString(th: Throwable): String {
        if (!LOG) return ""
        return android.util.Log.getStackTraceString(th)
    }

}
