import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.vanniktech.maven.publish")
}

val releaseMode: Boolean = System.getenv("release") != null

apply(plugin = "org.jetbrains.kotlin.multiplatform")

mavenPublishing {
    coordinates("eu.vendeli", project.name, project.version.toString())
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, true)
    val javaDoc = if (releaseMode) {
        signAllPublications()

        JavadocJar.Dokka("dokkaHtml")
    } else JavadocJar.Empty()

    configure(KotlinMultiplatform(javaDoc, true))

    pom {
        name = project.name
        description = project.description
        inceptionYear = "2024"
        url = "https://github.com/vendelieu/re.this"

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
            url = "https://github.com/vendelieu/re.this.git"
        }
        issueManagement {
            system = "Github"
            url = "https://github.com/vendelieu/re.this/issues"
        }
    }
}
