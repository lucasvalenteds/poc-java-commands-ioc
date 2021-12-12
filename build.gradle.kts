import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    jacoco
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.logging.log4j", "log4j-api", properties["version.log4j"].toString())
    implementation("org.apache.logging.log4j", "log4j-core", properties["version.log4j"].toString())
    implementation("org.slf4j", "slf4j-simple", properties["version.slf4j"].toString())

    implementation("javax.servlet", "javax.servlet-api", properties["version.servlet"].toString())
    implementation("org.springframework", "spring-core", properties["version.spring.framework"].toString())
    implementation("org.springframework", "spring-context", properties["version.spring.framework"].toString())
    implementation("org.springframework", "spring-webflux", properties["version.spring.framework"].toString())
    implementation("org.springframework", "spring-test", properties["version.spring.framework"].toString())

    implementation("org.springframework.data", "spring-data-jdbc", properties["version.spring.data"].toString())
    implementation("org.flywaydb", "flyway-core", properties["version.flyway"].toString())
    implementation("com.h2database", "h2", properties["version.h2"].toString())

    implementation("com.fasterxml.jackson.core", "jackson-databind", properties["version.jackson"].toString())
    implementation("com.fasterxml.jackson.datatype", "jackson-datatype-jsr310", properties["version.jackson"].toString())

    testImplementation("org.junit.jupiter", "junit-jupiter", properties["version.junit"].toString())
    testImplementation("org.mockito", "mockito-core", properties["version.mockito"].toString())
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
    }

    finalizedBy(tasks.withType<JacocoReport>())
}

tasks.withType<JacocoReport> {
    reports {
        xml.required.set(true)
    }
}
