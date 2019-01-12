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


java -cp $LIBS:.:target inozytol.cryptFetcher.App > error1
echo "Well, you should give two arguments to this app: one - file to store; second - storage path" > error1good
diff error1 error1good
#rm error1
#rm error1good
if [[ $? -ne 0 ]]
then
    echo "ERROR Something wrong with running script without arguments"
else
    echo "OK Good info when running script without arguments"
fi

printf "\n"

java -cp $LIBS:.:target inozytol.cryptFetcher.App lol ./ > error2

printf "\n"

printf "lol\nlol" | java -cp $LIBS:.:target inozytol.cryptFetcher.App README.md ./target
