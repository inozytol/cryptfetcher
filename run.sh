#!/usr/bin/env bash

# configure file contains paths to libraries and additional
# scripts setting up enviroment
# defined variables
# LIB_JAR_PATH - path to inozytol/fileFetcher.jar and inozytol/Cryptest.jar
# LOG4J_JAR_API_PATH
# LOG$J_JAR_CORE_PATH
# LIBS
. configure.sh


rm -fr target
mkdir target

javac -d target -cp $LIBS:.:target src/main/inozytol/cryptFetcher/App.java

if [[ $? -ne 0 ]]
then
    exit 1
fi

java -cp $LIBS:.:target inozytol.cryptFetcher.App

