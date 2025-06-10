package eu.vendeli.rethis.api.processor.utils

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import io.ktor.util.reflect.*
import kotlinx.io.Buffer

internal fun addMetaParameters() {
    val staticParts = buildStaticCommandParts(
        *context.currentCommand.command.name.split(' ').toTypedArray(),
        parameters = context.currentCommand.encodeFunction.parameters,
    )

    context.typeSpec
        .addProperty(
            PropertySpec.builder("TYPE_INFO", TypeInfo::class, KModifier.PRIVATE)
                .initializer("typeInfo<%T>()", context.currentCommand.type)
                .build(),
        )
        .addProperty(
            PropertySpec.builder("BLOCKING_STATUS", BOOLEAN, KModifier.PRIVATE, KModifier.CONST)
                .initializer("%L", context.currentCommand.command.isBlocking)
                .build(),
        )
        .addProperty(
            PropertySpec.builder("COMMAND_HEADER", Buffer::class, KModifier.PRIVATE)
                .initializer(buildStaticHeaderInitializer(staticParts))
                .build(),
        )
}
