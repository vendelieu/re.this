package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.common.*
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.types.options.BYBOX
import eu.vendeli.rethis.types.options.CenterPoint
import eu.vendeli.rethis.types.options.GeoAddOption
import eu.vendeli.rethis.types.options.Shape
import eu.vendeli.rethis.utils.cast
import eu.vendeli.rethis.utils.safeCast
import kotlinx.coroutines.delay

suspend fun ReThis.geoAdd(
    key: String,
    vararg member: GeoMember,
    upsertMode: GeoAddOption.UpsertMode? = null,
    ch: Boolean = false,
): Long = execute(
    listOfNotNull(
        "GEOADD",
        key,
        upsertMode,
        ch.takeIf { it }?.let { "CH" },
        *member,
    ),
).unwrap() ?: 0

suspend fun ReThis.geoDist(key: String, member1: String, member2: String, unit: GeoUnit? = null): Double? =
    execute(
        listOfNotNull(
            "GEODIST",
            key,
            member1,
            member2,
            unit?.toString(),
        ),
    ).unwrap<String?>()?.toDouble()

suspend fun ReThis.geoHash(key: String, vararg members: String): List<String> = execute(
    listOf(
        "GEOHASH",
        key,
        *members,
    ),
).unwrapList()

suspend fun ReThis.geoPos(key: String, vararg members: String): List<List<GeoPosition>?> = execute(
    listOf(
        "GEOPOS",
        key,
        *members,
    ),
).unwrapList<RType>().map { entry ->
    entry.safeCast<RArray>()?.value?.chunked(2) {
        GeoPosition(it.first().unwrap<Double>()!!, it.last().unwrap()!!)
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
    listOfNotNull(
        "GEOSEARCH",
        key,
        center,
        shape.let { if (it is BYBOX) "BYBOX" to it else it },
        sort?.name,
        count?.let { "COUNT" to it },
        any.takeIf { it }?.let { "ANY" },
        withCoord.takeIf { it }?.let { "WITHCOORD" },
        withDist.takeIf { it }?.let { "WITHDIST" },
        withHash.takeIf { it }?.let { "WITHHASH" },
    ),
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
): Long = execute(
    listOfNotNull(
        "GEOSEARCHSTORE",
        destination,
        source,
        center,
        shape.let { if (it is BYBOX) "BYBOX" to it else it },
        sort?.name,
        count?.let { "COUNT" to it },
        any.takeIf { it }?.let { "ANY" },
        storeDist.takeIf { it }?.let { "STOREDIST" },
    ),
).unwrap() ?: 0
