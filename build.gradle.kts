plugins {
    id("java")
    id ("org.springframework.boot") version "3.3.0"
    id ("io.spring.dependency-management") version "1.1.5"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation ("org.springframework.boot:spring-boot-starter")
    implementation ("org.springframework.boot:spring-boot-starter-web")
    implementation ("org.telegram:telegrambots:6.9.7.1")
    implementation ("org.projectlombok:lombok")
    implementation ("org.slf4j:slf4j-api")
    implementation ("org.springframework.boot:spring-boot-starter-validation")
    annotationProcessor ("org.projectlombok:lombok")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}