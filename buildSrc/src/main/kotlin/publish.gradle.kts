import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform

plugins {
    id("com.vanniktech.maven.publish")
}

val libraryData = extensions.create("libraryData", PublishingExtension::class)
val releaseMode: Boolean = System.getenv("release").toBoolean()
val ver = System.getenv("libVersion") ?: "dev"

apply(plugin = "org.jetbrains.kotlin.multiplatform")

mavenPublishing {
    afterEvaluate { coordinates("eu.vendeli", libraryData.name.get(), ver) }
    publishToMavenCentral(true)
    val javaDoc = if (releaseMode) {
        signAllPublications()

        JavadocJar.Empty()
    } else JavadocJar.Empty()

    configure(KotlinMultiplatform(javaDoc, true))

    pom {
        name = libraryData.name
        description = libraryData.description
        inceptionYear = "2024"
        url = REPO_URL

        licenses {
            license {
                name = "Apache 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0"
            }
        }
        developers {
            developer {
                id = "Vendelieu"
                name = "Vendelieu"
                email = "vendelieu@gmail.com"
                url = "https://vendeli.eu"
            }
        }
        scm {
            connection = REPO_URL.replace("https://", "scm:git:")
            developerConnection = REPO_URL.replace("https://", "scm:git:ssh://")
            url = "$REPO_URL.git"
        }
        issueManagement {
            system = "Github"
            url = "$REPO_URL/issues"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GHPackages"
            url = uri(REPO_URL.replace("github.com", "maven.pkg.github.com"))
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
