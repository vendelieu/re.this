package eu.vendeli.rethis

import io.kotest.core.annotation.Condition
import io.kotest.core.spec.Spec
import kotlin.reflect.KClass

class IOSensitiveTest : Condition {
    override fun evaluate(kclass: KClass<out Spec>): Boolean = !TestEnv.has("GITHUB_ACTOR")
}
