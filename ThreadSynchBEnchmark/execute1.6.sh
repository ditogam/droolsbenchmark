#!/bin/bash
## declare an jdk_path variable
declare -a jdk_path=("jrockit-jdk1.6.0_45-R28.2.7-4.1.0")
declare -a java_version=("1.6")
declare -a jmh_version=("1.6.3")
declare -a use_profiler=("false")
declare -a mvn_path=("apache-maven-3.2.5")

# get length of an jdk_path
jdk_pathlength=${#jdk_path[@]}

# use for loop to read all values and indexes

for ((i = 0; i < ${jdk_pathlength}; i++)); do
  echo "index: $i, jdk: ${jdk_path[$i]}, java_version: ${java_version[$i]}, jmh_version: ${jmh_version[$i]}, use_profiler: ${use_profiler[$i]}, mvn_path: ${mvn_path[$i]}"
  export JAVA_HOME=jdk/${jdk_path[$i]}
  export MAVEN_HOME=mvn/${mvn_path[$i]}
  rm -rf flamegraphs
  $MAVEN_HOME/bin/mvn --quiet -Djava.version=${java_version[$i]} -Djmh.version=${jmh_version[$i]} clean test -Duse.profiler=${use_profiler[$i]}
  VAR1=${use_profiler[$i]}
done
