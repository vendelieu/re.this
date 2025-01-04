object CommonParams {
    val releaseMode get() = System.getenv("release") != null
    val metadataOnly: Boolean get() = System.getenv("meta_only") != null

    const val REPO_URL = "https://github.com/vendelieu/re.this"
}
