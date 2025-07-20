package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.api.spec.common.request.geospatial.ByBox
import eu.vendeli.rethis.api.spec.common.request.geospatial.ByRadius
import eu.vendeli.rethis.api.spec.common.request.geospatial.FromLongitudeLatitude
import eu.vendeli.rethis.api.spec.common.response.geospatial.*
import eu.vendeli.rethis.command.geospatial.*
import io.kotest.matchers.shouldBe

class GeoCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test GEOADD command`() {
        client.geoAdd("testSet1", GeoMember(1.2, 1.3, "testValue1")) shouldBe 1L
    }

    @Test
    suspend fun `test GEODIST command`() {
        client.geoAdd("testSet2", GeoMember(1.0, 1.0, "testValue2"))
        client.geoAdd("testSet2", GeoMember(2.0, 2.0, "testValue3"))

        client.geoDist("testSet2", "testValue2", "testValue3") shouldBe 157270.0561
    }

    @Test
    suspend fun `test GEOHASH command`() {
        client.geoAdd("testSet3", GeoMember(1.0, 1.0, "testValue4"))
        client.geoHash("testSet3", "testValue4") shouldBe listOf("s00twy01mt0")
    }

    @Test
    suspend fun `test GEOPOS command`() {
        client.geoAdd("testSet4", GeoMember(1.0, 1.0, "testValue5"))
        client.geoPos("testSet4", "testValue5") shouldBe listOf(
            listOf(
                GeoPosition(
                    0.9999999403953552,
                    0.9999994591429768,
                ),
            ),
        )
    }

    @Test
    suspend fun `test GEOSEARCH command`() {
        client.geoAdd("testSet5", GeoMember(13.361389, 38.115556, "testValue6"))
        client.geoAdd("testSet5", GeoMember(12.758489, 38.788135, "testValue7"))
        client.geoSearch(
            "testSet5",
            FromLongitudeLatitude(15.0, 37.0),
            ByRadius(400.0, GeoUnit.KILOMETERS),
            withCoord = true,
            withDist = true,
            withHash = true,
            order = GeoSort.ASC,
        ) shouldBe listOf(
            GeoSearchResult(
                member = "testValue6",
                distance = 190.4424,
                coordinates = GeoPosition(13.361389338970184, 38.1155563954963),
                hash = 3479099956230698,
            ),
            GeoSearchResult(
                member = "testValue7",
                distance = 279.7405,
                coordinates = GeoPosition(12.75848776102066, 38.78813451624225),
                hash = 3479273021651468,
            ),
        )
    }

    @Test
    suspend fun `test GEOSEARCHSTORE command`() {
        client.geoAdd("testSet6", GeoMember(13.361389, 38.115556, "testValue8"))
        client.geoAdd("testSet6", GeoMember(12.758489, 38.788135, "testValue9"))
        client.geoSearchStore(
            "testKey7",
            "testSet6",
            FromLongitudeLatitude(15.0, 37.0),
            ByBox(400.0, 400.0, GeoUnit.KILOMETERS),
            order = GeoSort.ASC,
            count = 3,
            any = true,
            storedist = true,
        ) shouldBe 2L
    }
}
