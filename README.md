# Kotlin cross-platform / multi-format reflectionless serialization 

[![JetBrains incubator project](https://jb.gg/badges/incubator.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![TeamCity build](https://img.shields.io/teamcity/http/teamcity.jetbrains.com/s/KotlinTools_KotlinxSerialization_Ko.svg)](https://teamcity.jetbrains.com/viewType.html?buildTypeId=KotlinTools_KotlinxSerialization_Ko&guest=1)
[![Download](https://api.bintray.com/packages/kotlin/kotlinx/kotlinx.serialization.runtime/images/download.svg) ](https://bintray.com/kotlin/kotlinx/kotlinx.serialization.runtime/_latestVersion)

Kotlin serialization consists of a compiler plugin, which automatically produces visitor code for classes, and runtime library, which uses generated code to serialize objects without reflection.

* Supports Kotlin classes marked as `@Serializable` and standard collections. 
* Supports JSON, CBOR, and Protobuf formats out-of-the-box.
* The same code works on Kotlin/JVM, Kotlin/JS and Kotlin/Native (Native support is limited for now, see below in the [corresponding section](#native)).

## Runtime overview

This project contains the runtime library. Runtime library provides:

* Interfaces which are called by compiler-generated code (`Encoder`, `Decoder`).
* Basic skeleton implementations of these interfaces in which you should override some methods if you want to 
  implement custom data format.
* Some internal classes like built-ins and collections serializers.
* Ready-to-use serialization formats.
* Other useful classes that benefit from serialization framework (e.g. object-to-Map transformer)

You can open example projects for [JVM](example-jvm) or [JS](example-js) to get started playing with it.

## Table of contents

* [Quick example](#quick-example)
* [Current status](#current-project-status)
* [Library installing](#setup)
* [Kotlin/Native](#native)
* [Working in IntelliJ IDEA](#troubleshooting-intellij-idea)
* [Usage](docs/runtime_usage.md)
* [More examples of supported Kotlin classes](docs/examples.md)
* [Writing custom serializers](docs/custom_serializers.md)
* [Add-on formats](formats/README.md)
* [Building library and compiler plugin from source](docs/building.md)
* [Instructions for old versions under Kotlin 1.2 and migration guide](docs/old12.md)


## Quick example

```kotlin

import kotlinx.serialization.*
import kotlinx.serialization.json.JSON

@Serializable
data class Data(val a: Int, @Optional val b: String = "42")

fun main(args: Array<String>) {
    // serializing objects
    val jsonData = JSON.stringify(Data.serializer(), Data(42))
    // serializing lists
    val jsonList = JSON.stringify(Data.serializer().list, listOf(Data(42)))
    println(jsonData) // {"a": 42, "b": "42"}
    println(jsonList) // [{"a": 42, "b": "42"}]

    // parsing data back
    val obj = JSON.parse(Data.serializer(), """{"a":42}""")
    println(obj) // Data(a=42, b="42")
}
```

To learn more about JSON usage and other formats, see [usage](docs/runtime_usage.md).
More examples of various kinds of Kotlin classes that can be serialized can be found [here](docs/examples.md).

## Current project status

Starting from Kotlin 1.3-RC2, serialization plugin is bundled into the Kotlin compiler, and IDEA plugin is bundled into the Kotlin plugin.

Runtime library is under reconstruction to match the corresponding [KEEP](https://github.com/Kotlin/KEEP/blob/serialization/proposals/extensions/serialization.md), so some features described there can be not implemented yet. While library is stable and has successfully been used in various scenarios, there is no API compatibility guarantees between versions, that's why it is called experimental.
This document describes setup for Kotlin 1.3 and higher. To watch instructions regarding 1.2, follow [this document](docs/old12.md).

## Setup

Using Kotlin Serialization requires Kotlin compiler `1.3.0` or higher. Make sure that you have corresponding plugin installed in the IDE. Delegate build to Gradle: (`Settings - Build, Execution, Deployment - Build Tools - Gradle - Runner -` tick `Delegate IDE build/run actions to gradle`).
Example projects on JVM are available for [Gradle](example-jvm/build.gradle) and [Maven](example-jvm/pom.xml).

### Gradle/JVM

You have to add the serialization plugin as the other [compiler plugins](https://kotlinlang.org/docs/reference/compiler-plugins.html):

```gradle
buildscript {
    ext.kotlin_version = '1.3.0'
    repositories { jcenter() }

    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        classpath "org.jetbrains.kotlin:kotlin-serialization:$kotlin_version"
    }
}
```

Don't forget to apply the plugin:

```gradle
apply plugin: 'kotlin'
apply plugin: 'kotlinx-serialization'
```

Next, you have to add dependency on the serialization runtime library. Note that while plugin have version the same as compiler one, runtime library has different coordinates, repository and versioning.

```gradle
repositories {
    jcenter()
    // artifacts are published to this repository
    maven { url "https://kotlin.bintray.com/kotlinx" }
}

dependencies {
    compile "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
    compile "org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.9.0"
}
``` 

### Android/JVM

Library should work on Android "as is". If you're using proguard, you need
to add this to your `proguard-rules.pro`:

```proguard
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.yourcompany.yourpackage.**$$serializer { *; } # <-- change package name to your app's
-keepclassmembers class com.yourcompany.yourpackage.** { # <-- change package name to your app's
    *** Companion;
}
-keepclasseswithmembers class com.yourcompany.yourpackage.** { # <-- change package name to your app's
    kotlinx.serialization.KSerializer serializer(...);
}
```

You may also want to keep all custom serializers you've defined.

### Maven/JVM

Ensure the proper version of Kotlin and serialization version: 

```xml
<properties>
    <kotlin.version>1.3.0</kotlin.version>
    <serialization.version>0.9.0</serialization.version>
</properties>
```

Include bintray repository for library:

```xml
<repositories>
    <repository>
        <id>bintray-kotlin-kotlinx</id>
        <name>bintray</name>
        <url>https://kotlin.bintray.com/kotlinx</url>
    </repository>
</repositories>
```

Add serialization plugin to Kotlin compiler plugin:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-maven-plugin</artifactId>
            <version>${kotlin.version}</version>
            <executions>
                <execution>
                    <id>compile</id>
                    <phase>compile</phase>
                    <goals>
                        <goal>compile</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <compilerPlugins>
                    <plugin>kotlinx-serialization</plugin>
                </compilerPlugins>
            </configuration>
            <dependencies>
                <dependency>
                    <groupId>org.jetbrains.kotlin</groupId>
                    <artifactId>kotlinx-maven-serialization-plugin</artifactId>
                    <version>${kotlin.version}</version>
                </dependency>
            </dependencies>
        </plugin>
    </plugins>
</build>
```

Add dependency on serialization runtime library:

```xml
<dependency>
    <groupId>org.jetbrains.kotlinx</groupId>
    <artifactId>kotlinx-serialization-runtime</artifactId>
    <version>${serialization.version}</version>
</dependency>
```

### Multiplatform (JS and common)

Replace dependency on `kotlinx-serialization-runtime` with `kotlinx-serialization-runtime-js` or `kotlinx-serialization-runtime-common`
to use it in JavaScript and common projects, respectively. Both `kotlin-platform-***` and `kotlin-multiplatform` are supported.
JavaScript example is located at [`example-js`](example-js) folder.

### Native

You can apply the plugin to `kotlin-platform-native` or `kotlin-multiplatform` projects.
`konan` plugin is not supported and deprecated.

**Important note**: for `kotlin-multiplatform` project, apply usual `kotlinx-serailization` plugin.
For `kotlin-platform-native` module, apply `kotlinx-serialization-native` plugin,
since platform-native from K/N 0.9.3 uses infrastructure in which compiler plugins [are shaded](https://github.com/JetBrains/kotlin-native/issues/2210#issuecomment-429753168).

Use `kotlinx-serialization-runtime-native` artifact. Don't forget to `enableFeaturePreview('GRADLE_METADATA')`
in yours `settings.gradle`. You must have Gradle 4.7, because higher versions have unsupported format of metadata.

Serialization compiler plugin for Native is still in active development, and is not as feature-full as JVM/JS plugins.

What **works**: 

* `@Serializable` classes with primitive or `@Serializable` properties
* Standard collections
* `@Optional` and `@SerialName` annotations

What **does not work**:

* `@Serializable` classes with generics (except standard collections)
* Enums and arrays (`Array<T>, ByteArray, etc`)
* `@Transient` initializers and `init` blocks
* `@SerialInfo`-based annotations

Sample project can be found in [example-native](example-native) folder.


## Troubleshooting IntelliJ IDEA

Serialization support should work out of the box, if you have 1.3 Kotlin plugin installed. You still have to delegate build to Gradle (`Settings - Build, Execution, Deployment - Build Tools - Gradle - Runner -` tick `Delegate IDE build/run actions to gradle`).
In case of problems, force project re-import from Gradle.
