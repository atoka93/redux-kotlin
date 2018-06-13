# redux-kotlin
[ ![Download](https://api.bintray.com/packages/atoka93/redux/net.attilaszabo.redux%3Aredux-kotlin/images/download.svg) ](https://bintray.com/atoka93/redux/net.attilaszabo.redux%3Aredux-kotlin/_latestVersion)
[![Build Status](https://travis-ci.org/atoka93/redux-kotlin.svg?branch=master)](https://travis-ci.org/atoka93/redux-kotlin)

Object-oriented [Redux](https://redux.js.org/) implementation in Kotlin.
- `redux-kotlin` is the base module, this is a pure Kotlin module that contains the basic functionality of Redux.
- `redux-kotlin-base-implementation` contains basic implementations of a store and a asynchronous version of the store. Since the
Kotlin Coroutines are still *experimental* I used java classes for the implementation. It includes the base module.
- `redux-kotlin-android-extensions` contains useful extensions for Android. This module includes the previous module.
- `redux-kotlin-rx-extensions` contains extensions to enable RxJava2 usage. This module does not include but requires the base module.

## Example
A sample Android application showcasing an architecture combining [Redux](https://redux.js.org/) and the [Clean Architecture](https://8thlight.com/blog/uncle-bob/2012/08/13/the-clean-architecture.html) principles using these libraries can be found [here](https://github.com/atoka93/clean-redux-demo).

## Download
The libraries are available on [jcenter](https://bintray.com/bintray/jcenter?filterByPkgName=net.attilaszabo.redux) via Maven, Gradle or Ivy.
<br><br>
Gradle:
```groovy
dependencies {
    implementation 'net.attilaszabo.redux:redux-kotlin:{latest_version}'
    implementation 'net.attilaszabo.redux:redux-kotlin-implementation:{latest_version}'
    implementation 'net.attilaszabo.redux:redux-kotlin-android-extensions:{latest_version}'
    implementation 'net.attilaszabo.redux:redux-kotlin-rx-extensions:{latest_version}'
}
```

## License
Licensed under the [Apache License, Version 2.0](https://github.com/atoka93/redux-kotlin/blob/master/LICENSE).
