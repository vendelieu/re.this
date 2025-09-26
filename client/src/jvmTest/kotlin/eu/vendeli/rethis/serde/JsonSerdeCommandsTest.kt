package eu.vendeli.rethis.serde

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.json.jsonArrPop
import eu.vendeli.rethis.command.json.jsonSet
import eu.vendeli.rethis.command.serde.jsonGet
import eu.vendeli.rethis.command.serde.jsonMGet
import eu.vendeli.rethis.command.serde.jsonSet
import eu.vendeli.rethis.shared.request.json.JsonGetOption
import eu.vendeli.rethis.shared.types.BulkString
import eu.vendeli.rethis.shared.types.RArray
import eu.vendeli.rethis.shared.utils.unwrap
import eu.vendeli.rethis.utils.serdeModule
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.serialization.Serializable

class JsonSerdeCommandsTest : ReThisTestCtx(true) {
    @Serializable
    data class User(
        val id: Int,
        val name: String,
    )

    @Serializable
    data class MixedData(
        val str: String,
        val user: User,
        val number: Double,
    )

    @Test
    suspend fun `test json set with default path`() {
        val key = "defaultPathKey"
        val user = User(794, "Sam")

        client.jsonSet(key, value = user)

        client.jsonGet<User>(key) shouldBe user
    }

    @Test
    suspend fun `test heterogeneous structures using wrapper objects`() {
        val key = "mixedKey"
        val data = MixedData(
            str = "test",
            user = User(3, "Charlie"),
            number = 3.14,
        )

        client.jsonSet("a", "v")
        client.jsonSet(key, data)

        // Access individual fields with proper types
        client.jsonGet<String>(key, JsonGetOption.Paths("$.str")) shouldBe "[\"test\"]"
        client.jsonGet<List<User>>(key, JsonGetOption.Paths("$.user")) shouldBe listOf(User(3, "Charlie"))
        client.jsonGet<List<Double>>(key, JsonGetOption.Paths("$.number")) shouldBe listOf(3.14)
    }

    @Test
    suspend fun `test nested arrays with typed elements`() {
        @Serializable
        data class NestedUserList(
            val users: List<User>,
        )

        val key = "nestedArrayKey"
        val data = NestedUserList(
            users = listOf(
                User(4, "David"),
                User(5, "Eve"),
            ),
        )

        client.jsonSet(key, data)

        // Pop from nested array
        client.jsonArrPop(key, "$.users")
            .shouldBeTypeOf<RArray>()
            .value
            .first()
            .shouldBeTypeOf<BulkString>().let {
                client.serdeModule().deserialize(User.serializer(), it.unwrap<String>()!!)
            } shouldBe User(5, "Eve")
        client.jsonGet<NestedUserList>(key) shouldBe NestedUserList(
            users = listOf(User(4, "David")),
        )
    }

    @Test
    suspend fun `test jsonMGet with typed paths`() {
        val key = "dataKey"
        val data = mapOf(
            "adam" to User(1, "Adam"),
            "eve" to User(2, "Eve"),
        )

        val key2 = "dataKey2"
        val data2 = mapOf(
            "joanna" to User(3, "Joanna"),
            "jey" to User(4, "Jey"),
        )

        client.jsonSet(key, data)
        client.jsonSet(key2, data2)

        // Query multiple paths with proper types
        val results = client.jsonMGet<List<Int>>(
            path = "$.*.id",
            key,
            key2,
        )

        // Verify results with type casting
        val result = results.shouldNotBeNull().shouldHaveSize(2)

        result.first().shouldNotBeNull().shouldHaveSize(2).run {
            first().shouldNotBeNull() shouldBe 1
            last().shouldNotBeNull() shouldBe 2
        }

        result.last().shouldNotBeNull().shouldHaveSize(2).run {
            first().shouldNotBeNull() shouldBe 3
            last().shouldNotBeNull() shouldBe 4
        }
    }
}
