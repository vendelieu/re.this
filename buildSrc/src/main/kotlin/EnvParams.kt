object EnvParams {
    val releaseMode get() = System.getenv("release") != null
    val metadataOnly: Boolean get() = System.getenv("meta_only") != null
}
