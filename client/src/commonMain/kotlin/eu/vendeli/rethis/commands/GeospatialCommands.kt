package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.BulkString
import eu.vendeli.rethis.api.spec.common.types.Int64
import eu.vendeli.rethis.api.spec.common.types.RArray
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.types.common.*
import eu.vendeli.rethis.types.options.CenterPoint
import eu.vendeli.rethis.types.options.GeoAddOption
import eu.vendeli.rethis.types.options.Shape
import eu.vendeli.rethis.types.response.*
import eu.vendeli.rethis.utils.cast
import eu.vendeli.rethis.utils.execute
import eu.vendeli.rethis.utils.safeCast
import eu.vendeli.rethis.utils.unwrap
import eu.vendeli.rethis.utils.unwrapList
import eu.vendeli.rethis.utils.writeArgument
import kotlinx.coroutines.delay

suspend fun ReThis.geoAdd(
    key: String,
    vararg member: GeoMember,
    upsertMode: GeoAddOption.UpsertMode? = null,
    ch: Boolean = false,
): Long = execute<Long>(
    mutableListOf(
        "GEOADD".toArgument(),
        key.toArgument(),
    ).apply {
        writeArgument(upsertMode)
        if (ch) writeArgument("CH")
        member.forEach { writeArgument(it) }
    },
) ?: 0

suspend fun ReThis.geoDist(key: String, member1: String, member2: String, unit: GeoUnit? = null): Double? =
    execute(
        mutableListOf(
            "GEODIST".toArgument(),
            key.toArgument(),
            member1.toArgument(),
            member2.toArgument(),
        ).writeArgument(unit),
    ).unwrap<String?>()?.toDouble()

suspend fun ReThis.geoHash(key: String, vararg members: String): List<String> = execute<String>(
    listOf(
        "GEOHASH".toArgument(),
        key.toArgument(),
        *members.toArgument(),
    ),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.geoPos(key: String, vararg members: String): List<List<GeoPosition>?> = execute(
    listOf(
        "GEOPOS".toArgument(),
        key.toArgument(),
        *members.toArgument(),
    ),
).unwrapList<RType>().map { entry ->
    entry.safeCast<RArray>()?.value?.chunked(2) {
        GeoPosition(it.first().unwrap()!!, it.last().unwrap()!!)
    }
}

suspend fun ReThis.geoSearch(
    key: String,
    center: CenterPoint,
    shape: Shape,
    withCoord: Boolean = false,
    withDist: Boolean = false,
    withHash: Boolean = false,
    count: Long? = null,
    any: Boolean = false,
    sort: GeoSort? = null,
): List<GeoSearchResult>? = execute(
    mutableListOf(
        "GEOSEARCH".toArgument(),
        key.toArgument(),
    ).apply {
        writeArgument(center)
        writeArgument(shape)
        writeArgument(sort)
        count?.let { writeArgument("COUNT" to it) }
        if (any) writeArgument("ANY")
        if (withCoord) writeArgument("WITHCOORD")
        if (withDist) writeArgument("WITHDIST")
        if (withHash) writeArgument("WITHHASH")
    },
).unwrapList<RType>().takeIf { it.firstOrNull() is RArray }?.map {
    val el = it.cast<RArray>()
    var member = ""
    var distance: Double? = null
    var hash: Long? = null
    var coord: GeoPosition? = null

    for ((idx, i) in el.value.withIndex()) {
        when {
            idx == 0 -> {
                member = i.unwrap<String>()!!
            }

            withDist && idx == 1 && i is BulkString -> {
                distance = i.value.toDouble()
            }

            withHash && idx == 2 && i is Int64 -> {
                hash = i.value
            }

            i is RArray -> {
                val c = i.unwrapList<Double>()
                coord = GeoPosition(
                    c.first(),
                    c.last(),
                )
            }
        }
        delay(0)
    }

    GeoSearchResult(
        member,
        distance,
        coord,
        hash,
    )
}

suspend fun ReThis.geoSearchStore(
    destination: String,
    source: String,
    center: CenterPoint,
    shape: Shape,
    sort: GeoSort? = null,
    count: Long? = null,
    any: Boolean = false,
    storeDist: Boolean = false,
): Long = execute<Long>(
    mutableListOf(
        "GEOSEARCHSTORE".toArgument(),
        destination.toArgument(),
        source.toArgument(),
    ).apply {
        writeArgument(center)
        writeArgument(shape)
        writeArgument(sort)
        count?.let { writeArgument("COUNT" to it) }
        if (any) writeArgument("ANY")
        if (storeDist) writeArgument("STOREDIST")
    },
) ?: 0
