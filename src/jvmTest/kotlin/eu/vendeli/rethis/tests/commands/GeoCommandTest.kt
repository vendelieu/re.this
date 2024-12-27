package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.types.common.*
import eu.vendeli.rethis.types.options.BYBOX
import eu.vendeli.rethis.types.options.BYRADIUS
import eu.vendeli.rethis.types.options.FROMLONLAT
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GeoCommandTest : ReThisTestCtx() {
    @Test
    fun `test GEOADD command`(): Unit = runTest {
        client.geoAdd("testSet1", GeoMember(1.2, 1.3, "testValue1")) shouldBe 1L
    }

    @Test
    fun `test GEODIST command`(): Unit = runTest {
        client.geoAdd("testSet2", GeoMember(1.0, 1.0, "testValue2"))
        client.geoAdd("testSet2", GeoMember(2.0, 2.0, "testValue3"))

        client.geoDist("testSet2", "testValue2", "testValue3") shouldBe 157270.0561
    }

    @Test
    fun `test GEOHASH command`(): Unit = runTest {
        client.geoAdd("testSet3", GeoMember(1.0, 1.0, "testValue4"))
        client.geoHash("testSet3", "testValue4") shouldBe listOf("s00twy01mt0")
    }

    @Test
    fun `test GEOPOS command`(): Unit = runTest {
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
    fun `test GEOSEARCH command`(): Unit = runTest {
        client.geoAdd("testSet5", GeoMember(13.361389, 38.115556, "testValue6"))
        client.geoAdd("testSet5", GeoMember(12.758489, 38.788135, "testValue7"))
        client.geoSearch(
            "testSet5",
            FROMLONLAT(15.0, 37.0),
            BYRADIUS(400.0, GeoUnit.KILOMETERS),
            withCoord = true,
            withDist = true,
            withHash = true,
            sort = GeoSort.ASC,
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
    fun `test GEOSEARCHSTORE command`(): Unit = runTest {
        client.geoAdd("testSet6", GeoMember(13.361389, 38.115556, "testValue8"))
        client.geoAdd("testSet6", GeoMember(12.758489, 38.788135, "testValue9"))
        client.geoSearchStore(
            "testKey7",
            "testSet6",
            FROMLONLAT(15.0, 37.0),
            BYBOX(400.0, 400.0, GeoUnit.KILOMETERS),
            sort = GeoSort.ASC,
            count = 3,
            any = true,
            storeDist = true,
        ) shouldBe 2L
    }
}
