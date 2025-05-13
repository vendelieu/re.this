package eu.vendeli.rethis.api.spec.common.types

import com.ionspin.kotlin.bignum.integer.BigInteger

sealed class RType {
    open val value: Any? get() = null

    data object Null : RType()
    data class Error(
        val exception: RedisError,
    ) : RType() {
        constructor(message: String) : this(RedisError(message))
    }

    class Raw(
        override val value: ByteArray,
    ) : RType()
}

sealed class RPrimitive : RType()

data class PlainString(
    override val value: String,
) : RPrimitive()

data class Int64(
    override val value: Long,
) : RPrimitive()

data class Bool(
    override val value: Boolean,
) : RPrimitive()

data class F64(
    override val value: Double,
) : RPrimitive()

data class BigNumber(
    override val value: BigInteger,
) : RPrimitive()

data class VerbatimString(
    val encoding: String,
    val data: String,
) : RPrimitive() {
    override val value = "$encoding:$data"
}

data class BulkString(
    override val value: String,
) : RPrimitive()

data class RArray(
    override val value: List<RType>,
) : RType()

data class RMap(
    override val value: Map<RPrimitive, RType?>,
) : RType()

data class RSet(
    override val value: Set<RPrimitive>,
) : RType()

data class Push(
    override val value: List<RPrimitive>,
) : RType()
