object EnvParams {
    val releaseMode get() = System.getenv("release") != null
    val metadataOnly: Boolean get() = System.getProperty("metadata_only") != null
}
