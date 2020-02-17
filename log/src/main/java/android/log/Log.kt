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
import android.graphics.*
import android.graphics.Bitmap.CompressFormat
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.experimental.and

/** @author eastar*/
object Log {
    const val VERBOSE = android.util.Log.VERBOSE
    const val DEBUG = android.util.Log.DEBUG
    const val INFO = android.util.Log.INFO
    const val WARN = android.util.Log.WARN
    const val ERROR = android.util.Log.ERROR
    const val ASSERT = android.util.Log.ASSERT
    var LOG = true
    var FILE_LOG: File? = null
    var OUTPUT_CHANNEL = Channel.STUDIO

    enum class Channel { STUDIO, SYSTEM }

    private const val PREFIX = "``"
    private const val PREFIX_MULTILINE = "$PREFIX▼"
    private const val LF = "\n"
    private const val MAX_LOG_LINE_BYTE_SIZE = 3600

    private var LOG_PASS_REGEX = "^android\\..+|^com\\.android\\..+|^java\\..+".toRegex()

    private fun getLocator(info: StackTraceElement): String = "(%s:%d)".format(info.fileName, info.lineNumber)

    private fun getTag(info: StackTraceElement): String = runCatching {
        (info.className.takeLastWhile { it != '.' } + "." + info.methodName).run { replace("\\$".toRegex(), "_") }
    }.getOrDefault(info.methodName)

    private fun getStack(): StackTraceElement {
        return Exception().stackTrace.filterNot {
            it.className == javaClass.name
        }.filterNot {
            it.lineNumber < 0
        }.first()
    }

    private fun getStackMethod(methodNameKey: String): StackTraceElement {
        return Exception().stackTrace.filterNot {
            it.className == javaClass.name
        }.run {
            lastOrNull {
                it.methodName == methodNameKey
            } ?: last()
        }
    }

    private fun getStackCaller(methodNameKey: String): StackTraceElement {
        return Exception().stackTrace.filterNot {
            it.className == javaClass.name
        }.run {
            getOrNull(indexOfLast { it.methodName == methodNameKey } + 1) ?: last()
        }
    }

    private fun safeCut(byteArray: ByteArray, startOffset: Int): Int {
        val byteLength = byteArray.size
        if (byteLength <= startOffset) throw ArrayIndexOutOfBoundsException("!!text_length <= start_byte_index")
        if (byteArray[startOffset] and 0xc0.toByte() == 0x80.toByte()) throw java.lang.UnsupportedOperationException("!!start_byte_index must splited index")

        var position = startOffset + MAX_LOG_LINE_BYTE_SIZE
        if (byteLength <= position) return byteLength - startOffset

        while (startOffset <= position) {
            if (byteArray[position] and 0xc0.toByte() != 0x80.toByte()) break
            position--
        }
        if (position <= startOffset) throw UnsupportedOperationException("!!byte_length too small")
        return position - startOffset
    }

    @JvmStatic
    fun p(priority: Int, vararg args: Any?) {
        if (!LOG) return
        val info = getStack()
        val tag = getTag(info)
        val locator = getLocator(info)
        val msg = _MESSAGE(*args)
        println(priority, tag, locator, msg)
    }

    @JvmStatic
    fun ps(priority: Int, info: StackTraceElement, vararg args: Any?) {
        if (!LOG) return
        val tag = getTag(info)
        val locator = getLocator(info)
        val msg = _MESSAGE(*args)
        return println(priority, tag, locator, msg)
    }

    @JvmStatic
    fun println(priority: Int, tag: String, locator: String, msg: String?) {
        if (!LOG) return
        val sa = ArrayList<String>(100)
        val st = StringTokenizer(msg, LF, false)
        while (st.hasMoreTokens()) {
            val byteText = st.nextToken().toByteArray()
            var offset = 0
            while (offset < byteText.size) {
                val count = safeCut(byteText, offset)
                sa.add(PREFIX + String(byteText, offset, count))
                offset += count
            }
        }
        if (OUTPUT_CHANNEL == Channel.STUDIO) {
            val dots = "...................................................................................."
            val sb = StringBuilder(dots)
            dots.intern()
            val lastTag = tag.substring((tag.length + locator.length - dots.length).coerceAtLeast(0))
            sb.replace(0, lastTag.length, lastTag)
            sb.replace(sb.length - locator.length, sb.length, locator)
            val adjTag = sb.toString()
            when (sa.size) {
                0 -> android.util.Log.println(priority, adjTag, PREFIX)
                1 -> android.util.Log.println(priority, adjTag, sa[0])
                else -> android.util.Log.println(priority, adjTag, PREFIX_MULTILINE).run { sa.forEach { android.util.Log.println(priority, adjTag, it) } }
            }
        }
        if (OUTPUT_CHANNEL == Channel.SYSTEM) {
            val dots = "...................................................................................."
            val sb = StringBuilder(dots)
            val lastTag = tag.substring((tag.length + locator.length - dots.length).coerceAtLeast(0))
            sb.replace(0, lastTag.length, lastTag)
            sb.replace(sb.length - locator.length, sb.length, locator)
            val adjTag = sb.toString()
            when (sa.size) {
                0 -> println(adjTag + PREFIX)
                1 -> println(adjTag + sa[0])
                else -> println(adjTag + PREFIX_MULTILINE).run { repeat(sa.size) { println(adjTag + it) } }
            }
        }
    }

    @JvmStatic
    fun a(vararg args: Any?) {
        if (!LOG) return
        p(ASSERT, *args)
    }

    @JvmStatic
    fun e(vararg args: Any?) {
        if (!LOG) return
        p(ERROR, *args)
    }

    @JvmStatic
    fun w(vararg args: Any?) {
        if (!LOG) return
        p(WARN, *args)
    }

    @JvmStatic
    fun i(vararg args: Any?) {
        if (!LOG) return
        p(INFO, *args)
    }

    @JvmStatic
    fun d(vararg args: Any?) {
        if (!LOG) return
        p(DEBUG, *args)
    }

    @JvmStatic
    fun v(vararg args: Any?) {
        if (!LOG) return
        p(VERBOSE, *args)
    }

    @JvmStatic
    fun printStackTrace() {
        if (!LOG) return
        TraceLog().printStackTrace()
    }

    @JvmStatic
    fun printStackTrace(e: Throwable) {
        if (!LOG) return
        e.printStackTrace()
    }

    @JvmStatic
    fun pn(priority: Int, depth: Int, vararg args: Any?) {
        if (!LOG) return
        val info = Exception().stackTrace[1 + depth]
        val tag = getTag(info)
        val locator = getLocator(info)
        val msg = _MESSAGE(*args)
        println(priority, tag, locator, msg)
    }

    @JvmStatic
    fun pc(priority: Int, method: String, vararg args: Any?) {
        if (!LOG) return
        val info = getStackCaller(method)
        val tag = getTag(info)
        val locator = getLocator(info)
        val msg = _MESSAGE(*args)
        println(priority, tag, locator, msg)
    }

    @JvmStatic
    fun pm(priority: Int, method: String, vararg args: Any?) {
        if (!LOG) return
        val info = getStackMethod(method)
        val tag = getTag(info)
        val locator = getLocator(info)
        val msg = _MESSAGE(*args)
        println(priority, tag, locator, msg)
    }

    @JvmStatic
    fun toast(context: Context, vararg args: Any?) {
        if (!LOG) return
        e(*args)
        Toast.makeText(context, _MESSAGE(*args), Toast.LENGTH_SHORT).show()
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
        if (parent !is ViewGroup)
            return pn(android.util.Log.ERROR, depth + 2, _DUMP(parent, 0))

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            pn(android.util.Log.ERROR, depth + 2, _DUMP(child, depth))
            if (child is ViewGroup)
                viewTree(child, depth + 1)
        }
    }

    @JvmStatic
    fun clz(clz: Class<*>) {
        if (!LOG) return
        e(clz)
        //@formatter:off
        i("getName              " , clz.name)
        i("getPackage           " , clz.`package`)
        i("getCanonicalName     " , clz.canonicalName)
        i("getDeclaredClasses   " , clz.declaredClasses.contentToString())
        i("getClasses           " , clz.classes.contentToString())
        i("getSigners           " , clz.signers?.contentToString())
        i("getEnumConstants     " , clz.enumConstants?.contentToString())
        i("getTypeParameters    " , clz.typeParameters.contentToString())
        i("getGenericInterfaces " , clz.genericInterfaces.contentToString())
        i("getInterfaces        " , clz.interfaces.contentToString())
        if (clz.isAnnotation                              ) i("isAnnotation         ", clz.isAnnotation      )
        if (clz.isAnonymousClass                          ) i("isAnonymousClass     ", clz.isAnonymousClass  )
        if (clz.isArray                                   ) i("isArray              ", clz.isArray           )
        if (clz.isEnum                                    ) i("isEnum               ", clz.isEnum            )
        if (clz.isInterface                               ) i("isInterface          ", clz.isInterface       )
        if (clz.isLocalClass                              ) i("isLocalClass         ", clz.isLocalClass      )
        if (clz.isMemberClass                             ) i("isMemberClass        ", clz.isMemberClass     )
        if (clz.isPrimitive                               ) i("isPrimitive          ", clz.isPrimitive       )
        if (clz.isSynthetic                               ) i("isSynthetic          ", clz.isSynthetic       )
         //@formatter:on
    }

    /** dump */
    private fun _MESSAGE(vararg args: Any?): String {
        if (args.isNullOrEmpty())
            return "null[]"

        return args.joinToString {
            runCatching {
                when {
                    it == null -> "null"
                    it is Class<*> -> _DUMP(it)
                    it is View -> _DUMP(it)
                    it is Intent -> _DUMP(it)
                    it is Bundle -> _DUMP(it)
                    it is Throwable -> _DUMP(it)
                    it is Method -> _DUMP(it)
                    it is JSONObject -> it.toString(2)
                    it is JSONArray -> it.toString(2)
                    it is CharSequence -> _DUMP(it.toString())
                    it.javaClass.isArray -> (it as Array<*>).contentToString()
                    else -> it.toString()
                }
            }.getOrDefault("")
        }
    }

    private fun _DUMP(text: String): String = runCatching {
        val s = text[0]
        val e = text[text.length - 1]
        when {
            s == '[' && e == ']' -> JSONArray(text).toString(2)
            s == '{' && e == '}' -> JSONObject(text).toString(2)
            s == '<' && e == '>' -> PrettyXml.format(text)
            else -> text
        }
    }.getOrDefault(text)

    private fun _DUMP(method: Method): String = method.run {
        "$modifiers ${returnType.simpleName.padEnd(20).take(20)}${declaringClass.simpleName}.$name(${parameterTypes.joinToString { it.simpleName }})"
    }

    private fun _DUMP(v: View, depth: Int = 0): String {
        val space = "                    "
        val out = StringBuilder(128)
        out.append(space)
        when (v) {
            is WebView -> out.insert(depth, "W:" + Integer.toHexString(System.identityHashCode(v)) + ":" + v.title)
            is TextView -> out.insert(depth, "T:" + Integer.toHexString(System.identityHashCode(v)) + ":" + v.text)
            else -> out.insert(depth, "N:" + Integer.toHexString(System.identityHashCode(v)) + ":" + v.javaClass.simpleName)
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
        sb.append("\r\n LastPathSegment           ").append(if (uri.lastPathSegment != null) uri.lastPathSegment else "null")
        sb.append("\r\n Query                     ").append(if (uri.query != null) uri.query else "null")
        sb.append("\r\n Fragment                  ").append(if (uri.fragment != null) uri.fragment else "null")
        return sb.toString()
    }

    private fun _DUMP(intent: Intent?): String {
        if (intent == null) return "null_Intent"
        val sb = StringBuilder()
        //@formatter:off
        sb.append(if (intent.action       != null)(if (sb.isNotEmpty())"\n" else "") + "Action     " + intent.action               .toString() else "")
        sb.append(if (intent.data         != null)(if (sb.isNotEmpty())"\n" else "") + "Data       " + intent.data                 .toString() else "")
        sb.append(if (intent.categories   != null)(if (sb.isNotEmpty())"\n" else "") + "Categories " + intent.categories           .toString() else "")
        sb.append(if (intent.type         != null)(if (sb.isNotEmpty())"\n" else "") + "Type       " + intent.type                 .toString() else "")
        sb.append(if (intent.scheme       != null)(if (sb.isNotEmpty())"\n" else "") + "Scheme     " + intent.scheme               .toString() else "")
        sb.append(if (intent.`package`    != null)(if (sb.isNotEmpty())"\n" else "") + "Package    " + intent.`package`            .toString() else "")
        sb.append(if (intent.component    != null)(if (sb.isNotEmpty())"\n" else "") + "Component  " + intent.component            .toString() else "")
        sb.append(if (intent.flags        != 0x00)(if (sb.isNotEmpty())"\n" else "") + "Flags      " + Integer.toHexString(intent.flags) else "")
         //@formatter:on
        if (intent.extras != null) sb.append((if (sb.isNotEmpty()) "\n" else "") + _DUMP(intent.extras))
        return sb.toString()
    }

    private fun _DUMP(th: Throwable?): String {
        th ?: return "Throwable"
        return if (th.cause == null)
            th.javaClass.simpleName + "," + th.message
        else
            _DUMP(th.cause)
    }

    @JvmStatic
    fun _toHexString(byteArray: ByteArray?): String = byteArray?.joinToString("") { "%02x".format(it) } ?: "!!byte[]"

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
                sb.append(name).append('<').append(value.javaClass.simpleName).append('>').append(" = ")
                //@formatter:off
                  val  componentType = value.javaClass.componentType
                when {
                    Boolean::class.javaPrimitiveType!!.isAssignableFrom(componentType!!) -> sb.append(Arrays.toString(value as BooleanArray?))
                    Byte::class.javaPrimitiveType!!.isAssignableFrom(componentType) -> sb.append(if ((value as ByteArray).size < MAX_LOG_LINE_BYTE_SIZE)String((value as ByteArray?)!!) else "[" + value.size + "]")
                    Char::class.javaPrimitiveType!!.isAssignableFrom(componentType) -> sb.append(String((value as CharArray?)!!))
                    Double::class.javaPrimitiveType!!.isAssignableFrom(componentType) -> sb.append(Arrays.toString(value as DoubleArray?))
                    Float::class.javaPrimitiveType!!.isAssignableFrom(componentType) -> sb.append(Arrays.toString(value as FloatArray?))
                    Int::class.javaPrimitiveType!!.isAssignableFrom(componentType) -> sb.append(Arrays.toString(value as IntArray?))
                    Long::class.javaPrimitiveType!!.isAssignableFrom(componentType) -> sb.append(Arrays.toString(value as LongArray?))
                    Short::class.javaPrimitiveType!!.isAssignableFrom(componentType) -> sb.append(Arrays.toString(value as ShortArray?))
                    else -> sb.append((value as Array<*>).contentToString())
                }
             //@formatter:on
            } else if (value.javaClass.isPrimitive //
//					|| (value.getClass().getMethod("toString").getDeclaringClass() != Object.class)// toString이 정의된경우만
                    || value.javaClass.isEnum //
                    || value is Rect //
                    || value is RectF //
                    || value is Point //
                    || value is Number //
                    || value is Boolean //
                    || value is CharSequence) //
            {
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
                    sb.append(name).append('<').append(value.javaClass.simpleName).append('>').append(" = ").append(":\n")
                    val it = value.iterator()
                    while (it.hasNext()) sb.append(_DUMP_object("  $name[item]", it.next(), duplication))
                } else {
                    val clz: Class<*> = value.javaClass
                    sb.append(name).append('<').append(value.javaClass.simpleName).append('>').append(" = ").append(":\n")
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
        if (!LOG)
            return

        if (uri == null) {
            e("context==null || uri == null")
            return
        }
        context.contentResolver.query(uri, null, null, null, null).use {
            cursor(it)
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

    //tic
    private var SEED_S: Long = 0L

    @JvmStatic
    fun tic_s() {
        if (!LOG) return
        val e = System.nanoTime()
        SEED_S = e
    }

    @JvmStatic
    fun tic() {
        if (!LOG) return
        val e = System.nanoTime()
        val s = SEED_S
        e(String.format(Locale.getDefault(), "%,25d", e - s))
        SEED_S = e
    }

    @JvmStatic
    fun tic(vararg args: String) {
        if (!LOG) return
        val e = System.nanoTime()
        val s = SEED_S
        e(String.format(Locale.getDefault(), "%,25d", e - s), args)
        SEED_S = e
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
                    BitmapFactory.decodeByteArray(data, 0, data.size).compress(CompressFormat.JPEG, 100, stream)
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

    private val timeText: String get() = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.ENGLISH).format(Date())

    //flog
    @JvmStatic
    fun flog(vararg args: Any?) {
        FILE_LOG ?: return
        runCatching {
            val info = getStack()
            val log: String = _MESSAGE(*args)
            val st = StringTokenizer(log, LF, false)

            val tag = "%-40s%-40d %-100s ``".format(Date().toString(), SystemClock.elapsedRealtime(), info.toString())
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
    private object PrettyXml {
        private val formatter = XmlFormatter(2, 80)
        fun format(s: String): String {
            return formatter.format(s, 0)
        }

        private fun buildWhitespace(numChars: Int): String {
            val sb = StringBuilder()
            for (i in 0 until numChars) sb.append(" ")
            return sb.toString()
        }

        private fun lineWrap(s: String?, lineLength: Int, indent: Int): String? {
            if (s == null) return null
            val sb = StringBuilder()
            var lineStartPos = 0
            var lineEndPos: Int
            var firstLine = true
            while (lineStartPos < s.length) {
                if (!firstLine) sb.append("\n") else firstLine = false
                if (lineStartPos + lineLength > s.length) lineEndPos = s.length - 1 else {
                    lineEndPos = lineStartPos + lineLength - 1
                    while (lineEndPos > lineStartPos && s[lineEndPos] != ' ' && s[lineEndPos] != '\t') lineEndPos--
                }
                sb.append(buildWhitespace(indent))
                sb.append(s.substring(lineStartPos, lineEndPos + 1))
                lineStartPos = lineEndPos + 1
            }
            return sb.toString()
        }

        private class XmlFormatter(private val indentNumChars: Int, private val lineLength: Int) {
            private var singleLine = false
            @Synchronized
            fun format(s: String, initialIndent: Int): String {
                var indent = initialIndent
                val sb = StringBuilder()
                var i = 0
                while (i < s.length) {
                    val currentChar = s[i]
                    if (currentChar == '<') {
                        val nextChar = s[i + 1]
                        if (nextChar == '/') indent -= indentNumChars
                        if (!singleLine) // Don't indent before closing element if we're creating opening and closing elements on a single line.
                            sb.append(buildWhitespace(indent))
                        if (nextChar != '?' && nextChar != '!' && nextChar != '/') indent += indentNumChars
                        singleLine = false // Reset flag.
                    }
                    sb.append(currentChar)
                    if (currentChar == '>') {
                        if (s[i - 1] == '/') {
                            indent -= indentNumChars
                            sb.append("\n")
                        } else {
                            val nextStartElementPos = s.indexOf('<', i)
                            if (nextStartElementPos > i + 1) {
                                val textBetweenElements = s.substring(i + 1, nextStartElementPos)
                                // If the space between elements is solely newlines, let them through to preserve additional newlines in source document.
                                when {
                                    textBetweenElements.replace("\n".toRegex(), "").isEmpty() -> sb.append(textBetweenElements + "\n")
                                    textBetweenElements.length <= lineLength * 0.5 -> sb.append(textBetweenElements).also { singleLine = true }
                                    else -> sb.append("\n" + lineWrap(textBetweenElements, lineLength, indent) + "\n")
                                }
                                i = nextStartElementPos - 1
                            } else {
                                sb.append("\n")
                            }
                        }
                    }
                    i++
                }
                return sb.toString()
            }

        }
    }

    class TraceLog : Throwable()
}