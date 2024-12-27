import CommonParams.REPO_URL
import java.time.LocalDate

plugins {
    id("org.jetbrains.dokka")
}
dokka {
    moduleName = "re.this"
    dokkaSourceSets.configureEach {
        sourceRoots.setFrom(project.projectDir.resolve("src/$name/kotlin"))
        sourceLink {
            localDirectory.set(file("src/$name/kotlin"))
            remoteUrl("$REPO_URL/blob/master/src/$name/kotlin")
            remoteLineSuffix.set("#L")
        }
    }
    pluginsConfiguration.html {
        customAssets.from(rootDir.resolve("assets/logo-icon.svg"))
        footerMessage.set("© ${LocalDate.now().year} Vendelieu")
        homepageLink.set(REPO_URL)
    }
}
