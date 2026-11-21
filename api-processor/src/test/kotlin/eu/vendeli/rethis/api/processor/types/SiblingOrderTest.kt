package eu.vendeli.rethis.api.processor.types

import kotlin.test.Test
import kotlin.test.assertEquals

class SiblingOrderTest {
    private data class Sibling(val name: String, val bound: RangeBound)

    private fun bound(vararg path: Int) = RangeBound(path.toList())

    private fun order(vararg siblings: Sibling) = siblings.toList()
        .orderedByBounds { it.bound to it.bound }
        .map { it.name }

    @Test
    fun `spec-less sibling declared after a spec-bound one stays after it`() {
        val result = order(Sibling("bound", bound(1)), Sibling("specless", bound()))

        assertEquals(listOf("bound", "specless"), result)
    }

    @Test
    fun `spec-less sibling declared between spec-bound ones stays between them`() {
        val result = order(
            Sibling("a", bound(1)),
            Sibling("specless", bound()),
            Sibling("b", bound(3)),
        )

        assertEquals(listOf("a", "specless", "b"), result)
    }

    @Test
    fun `spec-less sibling declared first stays first`() {
        val result = order(Sibling("specless", bound()), Sibling("bound", bound(1)))

        assertEquals(listOf("specless", "bound"), result)
    }

    @Test
    fun `all spec-less siblings keep declaration order`() {
        val result = order(Sibling("x", bound()), Sibling("y", bound()), Sibling("z", bound()))

        assertEquals(listOf("x", "y", "z"), result)
    }

    @Test
    fun `ignored leaf sorts last and does not move later spec-less siblings`() {
        val result = order(
            Sibling("ignored", bound(Int.MAX_VALUE)),
            Sibling("bound", bound(2)),
            Sibling("specless", bound()),
        )

        assertEquals(listOf("bound", "specless", "ignored"), result)
    }

    @Test
    fun `spec-bound siblings are ordered by bounds`() {
        val result = order(Sibling("b", bound(2)), Sibling("a", bound(1)))

        assertEquals(listOf("a", "b"), result)
    }

    private data class Composite(val name: String, val first: RangeBound, val second: RangeBound)

    private fun orderComposites(vararg siblings: Composite) = siblings.toList()
        .orderedByBounds { it.first to it.second }
        .map { it.name }

    @Test
    fun `composite with spec-less first bound declared after a bound sibling stays after it`() {
        val result = orderComposites(
            Composite("a", bound(1), bound(1)),
            Composite("composite", bound(), bound(5)),
            Composite("b", bound(7), bound(7)),
        )

        assertEquals(listOf("a", "composite", "b"), result)
    }

    @Test
    fun `composite with spec-less first bound declared first stays first`() {
        val result = orderComposites(
            Composite("composite", bound(), bound(5)),
            Composite("a", bound(1), bound(1)),
        )

        assertEquals(listOf("composite", "a"), result)
    }

    @Test
    fun `composite with bound first is ordered by its own bounds`() {
        val result = orderComposites(
            Composite("b", bound(7), bound(7)),
            Composite("composite", bound(3), bound(5)),
            Composite("a", bound(1), bound(1)),
        )

        assertEquals(listOf("a", "composite", "b"), result)
    }

    @Test
    fun `composite ending with an ignored bound does not move later spec-less siblings`() {
        val result = orderComposites(
            Composite("a", bound(1), bound(1)),
            Composite("composite", bound(), bound(Int.MAX_VALUE)),
            Composite("specless", bound(), bound()),
        )

        assertEquals(listOf("a", "specless", "composite"), result)
    }
}
