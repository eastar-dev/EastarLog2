@file:Suppress("unused", "UnusedReceiverParameter")

package easteregg

import android.app.Application
import kotlin.reflect.KClass

fun Application.easterEgg(vararg easterEgg: KClass<*>): Unit = Unit
fun Application.easterEgg(vararg easterEgg: String): Unit = Unit