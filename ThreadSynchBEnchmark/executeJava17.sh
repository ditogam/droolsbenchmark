#!/bin/bash
## declare an jdk_path variable
declare -a jdk_path=("jdk-17")
declare -a java_version=("17")
declare -a jmh_version=("1.32")
declare -a use_profiler=("true")
declare -a mvn_path=( "apache-maven-3.3.1")

# get length of an jdk_path
jdk_pathlength=${#jdk_path[@]}

# use for loop to read all values and indexes
rm -rf flamegraphs_out
mkdir flamegraphs_out
rm -rf results
mkdir results
for ((i = 0; i < ${jdk_pathlength}; i++)); do
  echo "index: $i, jdk: ${jdk_path[$i]}, java_version: ${java_version[$i]}, jmh_version: ${jmh_version[$i]}, use_profiler: ${use_profiler[$i]}, mvn_path: ${mvn_path[$i]}"
  export JAVA_HOME=jdk/${jdk_path[$i]}
  export MAVEN_HOME=mvn/${mvn_path[$i]}
  rm -rf flamegraphs
  $MAVEN_HOME/bin/mvn --quiet -Djava.version=${java_version[$i]} -Djmh.version=${jmh_version[$i]} clean test -Duse.profiler=${use_profiler[$i]}
  VAR1=${use_profiler[$i]}
  VAR2="true"
  if [ "$VAR1" = "$VAR2" ]; then
    FILE_NAME=flamegraph.${java_version[$i]}.tar.gz
    tar -czvf $FILE_NAME flamegraphs
    mv $FILE_NAME flamegraphs_out
  fi
done
