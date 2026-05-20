package eu.vendeli.rethis.shared.request.timeseries

import eu.vendeli.rethis.shared.annotations.RedisOption

@RedisOption.Token("AGGREGATION")
sealed class TsAggregator {
    @RedisOption.Token("AVG")
    data object Avg : TsAggregator()

    @RedisOption.Token("FIRST")
    data object First : TsAggregator()

    @RedisOption.Token("LAST")
    data object Last : TsAggregator()

    @RedisOption.Token("MIN")
    data object Min : TsAggregator()

    @RedisOption.Token("MAX")
    data object Max : TsAggregator()

    @RedisOption.Token("SUM")
    data object Sum : TsAggregator()

    @RedisOption.Token("RANGE")
    data object Range : TsAggregator()

    @RedisOption.Token("COUNT")
    data object Count : TsAggregator()

    @RedisOption.Token("STD.P")
    data object StdP : TsAggregator()

    @RedisOption.Token("STD.S")
    data object StdS : TsAggregator()

    @RedisOption.Token("VAR.P")
    data object VarP : TsAggregator()

    @RedisOption.Token("VAR.S")
    data object VarS : TsAggregator()

    @RedisOption.Token("TWA")
    data object Twa : TsAggregator()

    @RedisOption.Token("COUNTNAN")
    data object CountNan : TsAggregator()

    @RedisOption.Token("COUNTALL")
    data object CountAll : TsAggregator()
}
