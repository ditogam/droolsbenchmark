#!/bin/bash
  export JAVA_HOME=/opt/jdk/redhat/jdk-11.0.12
  export MAVEN_HOME=/opt/jdk/mvn/apache-maven-3.3.1
  $MAVEN_HOME/bin/mvn --quiet  clean test