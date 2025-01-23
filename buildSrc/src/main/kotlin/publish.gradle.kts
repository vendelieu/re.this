import CommonParams.REPO_URL
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.vanniktech.maven.publish")
}

val releaseMode: Boolean = System.getenv("release") != null
val ver = System.getenv("libVersion") ?: "dev"

apply(plugin = "org.jetbrains.kotlin.multiplatform")

mavenPublishing {
    coordinates("eu.vendeli", project.name, ver)
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, true)
    val javaDoc = if (releaseMode) {
        signAllPublications()

        JavadocJar.Empty()
    } else JavadocJar.Empty()

    configure(KotlinMultiplatform(javaDoc, true))

    pom {
        name = project.name
        group = project.group
        description = "Kotlin Multiplatform Redis Client: coroutine-based, DSL-powered, and easy to use."
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
            connection = "scm:git:github.com/vendelieu/re.this.git"
            developerConnection = "scm:git:ssh://github.com/vendelieu/re.this.git"
            url = "$REPO_URL.git"
        }
        issueManagement {
            system = "Github"
            url = "$REPO_URL/issues"
        }
    }
}
