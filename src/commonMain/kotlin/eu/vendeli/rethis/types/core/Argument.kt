package eu.vendeli.rethis.types.core

interface PairArgument<L, R> {
    val arg: Pair<L, R>
}

interface TripleArgument<L, M, R> {
    val arg: Triple<L, M, R>
}
