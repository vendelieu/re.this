package eu.vendeli.rethis.types.options

sealed class UpdateStrategyOption {
    sealed class ExistenceRule : UpdateStrategyOption()
    data object NX : ExistenceRule()
    data object XX : ExistenceRule()

    sealed class ComparisonRule : UpdateStrategyOption()
    data object GT : ComparisonRule()
    data object LT : ComparisonRule()
}
