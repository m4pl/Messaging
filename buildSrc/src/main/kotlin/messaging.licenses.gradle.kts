import messaging.licenses.GenerateLicensesTask

tasks.register<GenerateLicensesTask>("generateLicenses") {
    group = "documentation"
    description = "Generates assets/licenses.html from the release runtime classpath."

    output.set(rootProject.file("assets/licenses.html"))

    copyrightOverrides.put(
        "com.github.bumptech.glide:*",
        "Copyright 2014 Google, Inc. All rights reserved.",
    )
}
