package eu.vendeli.rethis.configuration

import eu.vendeli.rethis.types.common.ConfigType

class StandaloneConfiguration : ReThisConfiguration() {
    override val type = ConfigType.STANDALONE
}
