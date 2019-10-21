import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val beamVersion: String by project
val gcsVersion: String by project
val bigqueryVersion: String by project
val csvVersion: String by project
val kotlinVersion: String by project

plugins {
    java
    application
    kotlin("jvm") version "1.3.50"
}

group = "com.ntconcepts"
version = "1.0-SNAPSHOT"

allprojects {
    buildDir = File("C:/tmp/${rootProject.name}/${project.name}")
}

repositories {
    google()
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.apache.beam:beam-sdks-java-core:$beamVersion")
    implementation("org.apache.beam:beam-runners-direct-java:$beamVersion")
    implementation("org.apache.beam:beam-runners-google-cloud-dataflow-java:$beamVersion")
    implementation("org.apache.beam:beam-sdks-java-io-google-cloud-platform:$beamVersion")
    implementation("com.google.cloud:google-cloud-storage:$gcsVersion")
    implementation("com.google.cloud:google-cloud-bigquery:$bigqueryVersion")
    implementation("org.apache.commons:commons-csv:$csvVersion")
    testCompile("junit", "junit", "4.12")
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.5.2")
    testRuntime("org.junit.jupiter", "junit-jupiter-engine", "5.5.2")
    testCompile("org.junit.jupiter", "junit-jupiter-params", "5.5.2")
    testRuntime("org.junit.vintage", "junit-vintage-engine", "5.5.2")

}
//
//test {
//    useJUnitPlatform()
//}

application {
    mainClassName = "com.ntconcepts.gcpdemo2.MainKt"
}

tasks.getByName<JavaExec>("run") {
    if (project.hasProperty("args")) {
        val a = project.properties.get("args")
        if (a is String) {
            args = a.split("\\s+".toRegex())
        }
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}


configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}