import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

fun readCredentials(): Pair<String, String> {
    // Initialize Napier for logging network requests
    Napier.base(DebugAntilog())

    // Read credentials from stdin
    print("Enter username: ")
    val username = readlnOrNull() ?: ""

    print("Enter password: ")
    val password = readlnOrNull() ?: ""

    return Pair(username, password)
}