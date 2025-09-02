package eu.vendeli.rethis.configuration

import eu.vendeli.rethis.annotations.ConfigurationDSL

/**
 * Configuration for redis connection authentication.
 *
 * @property password the password to use for authentication
 * @property username the username to use for authentication, defaults to null
 */
@ConfigurationDSL
internal class AuthConfiguration(
    var password: CharArray,
    var username: String? = null,
) {
    override fun toString(): String = "AuthConfiguration(username=$username)"
}
