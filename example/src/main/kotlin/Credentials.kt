fun readCredentials(): Pair<String, String> {
    // Read credentials from stdin
    print("Enter username: ")
    val username = readLine() ?: ""

    print("Enter password: ")
    val password = readLine() ?: ""

    return Pair(username, password)
}