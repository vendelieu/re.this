package eu.vendeli.rethis.api.processor.utils

import eu.vendeli.rethis.api.processor.types.ArrayReply
import eu.vendeli.rethis.api.processor.types.BigNumberReply
import eu.vendeli.rethis.api.processor.types.BooleanReply
import eu.vendeli.rethis.api.processor.types.BulkStringReply
import eu.vendeli.rethis.api.processor.types.DoubleReply
import eu.vendeli.rethis.api.processor.types.IntegerReply
import eu.vendeli.rethis.api.processor.types.MapReply
import eu.vendeli.rethis.api.processor.types.NullReply
import eu.vendeli.rethis.api.processor.types.OneOfReply
import eu.vendeli.rethis.api.processor.types.PushReply
import eu.vendeli.rethis.api.processor.types.Replies
import eu.vendeli.rethis.api.processor.types.ReplyShape
import eu.vendeli.rethis.api.processor.types.SetReply
import eu.vendeli.rethis.api.processor.types.SimpleErrorReply
import eu.vendeli.rethis.api.processor.types.SimpleStringReply
import eu.vendeli.rethis.api.processor.types.TupleReply
import eu.vendeli.rethis.api.processor.types.UnknownReply
import eu.vendeli.rethis.api.processor.types.VerbatimStringReply
import eu.vendeli.rethis.shared.types.RespCode

/**
 * Flatten a [Replies] payload into the legacy `Set<RespCode>` view consumed by the existing
 * codegen pipeline (CodecExtensions, ExtDecoderUtils, RCommandData.responseTypes, TypeUtils).
 *
 * Both protocol shapes are unioned so codecs that need to handle either receive the full set;
 * the artifact is the single source of truth for which RESP-level codes a command may emit.
 */
internal fun Replies.toRespCodes(): Set<RespCode> {
    val out = mutableSetOf<RespCode>()
    resp2?.collectInto(out)
    resp3?.collectInto(out)
    return out
}

private fun ReplyShape.collectInto(out: MutableSet<RespCode>) {
    when (this) {
        is SimpleStringReply -> out += RespCode.SIMPLE_STRING
        is SimpleErrorReply -> out += RespCode.SIMPLE_ERROR
        is IntegerReply -> out += RespCode.INTEGER
        is DoubleReply -> out += RespCode.DOUBLE
        is BooleanReply -> out += RespCode.BOOLEAN
        is BulkStringReply -> out += RespCode.BULK
        is VerbatimStringReply -> out += RespCode.VERBATIM_STRING
        is BigNumberReply -> out += RespCode.BIG_NUMBER
        is NullReply -> out += RespCode.NULL
        is ArrayReply -> out += RespCode.ARRAY
        is SetReply -> out += RespCode.SET
        is MapReply -> out += RespCode.MAP
        is TupleReply -> out += RespCode.ARRAY
        is PushReply -> out += RespCode.PUSH
        is OneOfReply -> variants.forEach { it.collectInto(out) }
        is UnknownReply -> Unit
    }
}
