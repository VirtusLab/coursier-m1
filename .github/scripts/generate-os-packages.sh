#!/usr/bin/env bash
 # copied from https://github.com/VirtusLab/scala-cli/blob/8e3d34a336153c99918dbd43a2c03f8cffbe6afe/.github/scripts/generate-os-packages.sh
 set -eu

 ARCHITECTURE=$(uname -m)
 ARTIFACTS_DIR="artifacts/"
 mkdir -p "$ARTIFACTS_DIR"

 if [[ "$OSTYPE" == "msys" ]]; then
   mill="./mill.bat"
 else
   mill="./mill"
 fi


 launcher() {
   local launcherMillCommand="cs-m1.nativeImage"
   local launcherName

   if [[ "$OSTYPE" == "msys" ]]; then
     launcherName="cs.exe"
   else
     launcherName="cs"
   fi

   "$mill" -i copyTo "$launcherMillCommand" "$launcherName" 1>&2
   echo "$launcherName"
 }

 generate_sdk() {
   local sdkDirectory
   local binName

   if [[ "$OSTYPE" == "linux-gnu"* ]]; then
     if  [[ "$ARCHITECTURE" == "aarch64" ]]; then
       sdkDirectory="cs-aarch64-pc-linux-static-sdk"
     else
       echo "cs is not supported on $ARCHITECTURE"
       exit 2
     fi
     binName="cs"
   elif [[ "$OSTYPE" == "darwin"* ]]; then
     if [[ "$ARCHITECTURE" == "arm64" ]]; then
       sdkDirectory="cs-aarch64-apple-darwin-sdk"
     else
       echo "cs is not supported on $ARCHITECTURE"
       exit 2
     fi
     binName="cs"
   else
     echo "Unrecognized operating system: $OSTYPE" 1>&2
     exit 1
   fi

   mkdir -p "$sdkDirectory"/bin
   cp "$(launcher)" "$sdkDirectory"/bin/"$binName"

   zip -r "$sdkDirectory".zip "$sdkDirectory"

   mv "$sdkDirectory".zip "$ARTIFACTS_DIR/"/"$sdkDirectory".zip
 }

 if [[ "$OSTYPE" == "linux-gnu"* ]]; then
   generate_sdk
 elif [[ "$OSTYPE" == "darwin"* ]]; then
   generate_sdk
 else
   echo "Unrecognized operating system: $OSTYPE" 1>&2
   exit 1
 fi
