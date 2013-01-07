OSGi Delegation Support
==========================

The purpose of this library is to work around some downsides of using DynamicImport-Package by providing an alternate way to dynamically retrieve classes from other bundles at runtime.

[![Build Status](https://travis-ci.org/openengsb-labs/labs-delegation.png?branch=master)](https://travis-ci.org/openengsb-labs/labs-delegation)

How to build
==========================
* Install JDK 6 or higher

  You can install [Oracle JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) or
  [OpenJDK](http://openjdk.java.net/install/index.html) depending on the OS you use.
  Other JVM implementations should also work, but are untested.

* Install [Maven 3 or higher](http://maven.apache.org/download.html)

  Be sure to follow the provided [installation instructions](http://maven.apache.org/download.html#Installation)

* Run **mvn install** from the project's root directory
