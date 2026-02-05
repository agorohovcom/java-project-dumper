plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.agorohov.java-project-dumper"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.5.0.202512021534-r")
    implementation("ch.qos.logback:logback-classic:1.5.27")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveBaseName.set("java-project-dumper")
    archiveFileName.set("java-project-dumper.jar")
    archiveClassifier.set("")
    archiveVersion.set(project.version.toString())

    manifest {
        attributes["Main-Class"] = "com.agorohov.java_project_dumper.Main"
    }
}

//tasks.jar {
//    enabled = false  // отключаем обычный тонкий JAR
//}

tasks.build {
    dependsOn(tasks.shadowJar)
}