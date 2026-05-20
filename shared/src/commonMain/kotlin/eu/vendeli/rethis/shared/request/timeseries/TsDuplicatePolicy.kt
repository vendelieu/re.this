package eu.vendeli.rethis.shared.request.timeseries

import eu.vendeli.rethis.shared.annotations.RedisOption

@RedisOption.Token("ON_DUPLICATE")
sealed class TsOnDuplicate {
    @RedisOption.Token("BLOCK")
    data object Block : TsOnDuplicate()

    @RedisOption.Token("FIRST")
    data object First : TsOnDuplicate()

    @RedisOption.Token("LAST")
    data object Last : TsOnDuplicate()

    @RedisOption.Token("MIN")
    data object Min : TsOnDuplicate()

    @RedisOption.Token("MAX")
    data object Max : TsOnDuplicate()

    @RedisOption.Token("SUM")
    data object Sum : TsOnDuplicate()
}

@RedisOption.Token("DUPLICATE_POLICY")
sealed class TsDuplicatePolicy {
    @RedisOption.Token("BLOCK")
    data object Block : TsDuplicatePolicy()

    @RedisOption.Token("FIRST")
    data object First : TsDuplicatePolicy()

    @RedisOption.Token("LAST")
    data object Last : TsDuplicatePolicy()

    @RedisOption.Token("MIN")
    data object Min : TsDuplicatePolicy()

    @RedisOption.Token("MAX")
    data object Max : TsDuplicatePolicy()

    @RedisOption.Token("SUM")
    data object Sum : TsDuplicatePolicy()
}
