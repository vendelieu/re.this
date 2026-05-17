package eu.vendeli.rethis.shared.request.server

import eu.vendeli.rethis.shared.annotations.RedisOption

@RedisOption.Token("FILTERBY")
sealed class CommandListFilter {
    @RedisOption.Token("MODULE")
    class Module(@RedisOption.Name("module-name") val name: String) : CommandListFilter()

    @RedisOption.Token("ACLCAT")
    class AclCat(@RedisOption.Name("category") val category: String) : CommandListFilter()

    @RedisOption.Token("PATTERN")
    class Pattern(@RedisOption.Name("pattern") val pattern: String) : CommandListFilter()
}
