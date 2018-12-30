#!/usr/bin/env bash

# configure file contains paths to libraries and additional
# scripts setting up enviroment
# defined variables
# LIB_JAR_PATH
# LIBS
. configure.sh


rm -fr target
mkdir target

javac -d target -cp $LIBS:.:target src/main/inozytol/cryptFetcher/App.java

java -cp $LIBS:.:target inozytol.cryptFetcher.App
