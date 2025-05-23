/*
 * Copyright [2021-present] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-parameters"))
}

tasks.jar.configure {
    exclude("application.yaml")
    manifest {
        attributes(
            "Implementation-Title" to application.applicationName,
            "Implementation-Version" to archiveVersion
        )
    }
}

application {
    mainClass.set("me.ahoo.cosid.proxy.server.ProxyServer")

    applicationDefaultJvmArgs = listOf(
        "-Xms1280M",
        "-Xmx1280M",
        "-XX:MaxMetaspaceSize=256M",
        "-XX:MaxDirectMemorySize=512M",
        "-Xss1m",
        "-server",
        "-XX:+UseZGC",
        "-Xlog:gc*:file=logs/${applicationName}-gc.log:time,tags:filecount=10,filesize=32M",
        "-XX:+HeapDumpOnOutOfMemoryError",
        "-XX:HeapDumpPath=data",
        "-Dcom.sun.management.jmxremote",
        "-Dcom.sun.management.jmxremote.authenticate=false",
        "-Dcom.sun.management.jmxremote.ssl=false",
        "-Dcom.sun.management.jmxremote.port=5555",
        "-Dspring.config.location=file:./config/"
    )
}

dependencies {
    implementation(platform(project(":cosid-dependencies")))
    annotationProcessor(platform(project(":cosid-dependencies")))
    implementation(project(":cosid-proxy-api"))
    implementation(project(":cosid-spring-boot-starter"))
    implementation(project(":cosid-jackson"))

    //region cosid-spring-redis
    implementation(project(":cosid-spring-redis"))
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    //endregion

    //region cosid-jdbc
    implementation(project(":cosid-jdbc"))
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("com.mysql:mysql-connector-j")
    //endregion

    //region cosid-zookeeper
    //implementation(project(":cosid-zookeeper"))
    //endregion

    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("com.google.guava:guava")
    implementation("io.netty:netty-all")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
