#!/bin/bash

#Get Directory Path
SCRIPT_DIR=$(cd "$(dirname "$0")"; pwd)
DBS_HOME=$(cd "$SCRIPT_DIR/.."; pwd)
echo "DBS_HOME=$DBS_HOME"

#Ensure conf directory exists
if [ ! -d "$DBS_HOME/conf" ]; then
  mkdir -p "$DBS_HOME/conf"
fi

#Ensures that logs directory exists
if [ ! -d "$DBS_HOME/logs" ]; then
  mkdir -p "$DBS_HOME/logs"
fi

# application.properties
CONFIG_PATH="$DBS_HOME/conf/application.properties"
if [ ! -f "$CONFIG_PATH" ]; then
  echo "The conf/application.properties doesn't exist, please check it first!" >&2
  exit 1
fi

###########################################################################
#Comparable JRE extensions directory: must contain sunjce provider, etc.
#SunTls12RsaPremasterSecretKeyGenerator not available (java.ext.dirs)
resolve_jre_ext_dir() {
  local java_home_prop
  if ! command -v java >/dev/null 2>&1; then
    echo ""
    return
  fi
  java_home_prop=$(java -XshowSettings:properties -version 2>&1 | tr -d '\r' | sed -n 's/^ *java\.home = //p' | head -1)
  if [[ -n "$java_home_prop" && -d "$java_home_prop/lib/ext" ]]; then
    echo "$java_home_prop/lib/ext"
    return
  fi
  if [[ -n "${JAVA_HOME:-}" ]]; then
    if [[ -d "$JAVA_HOME/jre/lib/ext" ]]; then
      echo "$JAVA_HOME/jre/lib/ext"
      return
    fi
    if [[ -d "$JAVA_HOME/lib/ext" ]]; then
      echo "$JAVA_HOME/lib/ext"
      return
    fi
  fi
  echo ""
}

JAVA_EXT_DIR=$(resolve_jre_ext_dir)
if [[ -z "$JAVA_EXT_DIR" ]]; then
  echo "ERROR: Cannot find JRE lib/ext (JCE required for SSL). Set JAVA_HOME or fix PATH to java." >&2
  exit 1
fi
echo "JAVA_EXT_DIR=$JAVA_EXT_DIR"

# set up environment for Java
#JAVA_HOME=/opt/jdk1.8.0_202
#Build JVM arguments
JAVA_OPTS=()

ENCRYPT_FILE=''
if [[ $(uname -m) == "aarch64"* ]]; then
   ENCRYPT_FILE='libDBSyncer_aarch64.so'
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
   ENCRYPT_FILE='libDBSyncer.so'
elif [[ "$OSTYPE" == "darwin"* ]]; then
   ENCRYPT_FILE='libDBSyncer.dylib'
else
  echo "Unsupported OS." >&2
  exit 1
fi

if [ -e "$DBS_HOME/bin/$ENCRYPT_FILE" ]; then
  JAVA_OPTS+=("-agentpath:$DBS_HOME/bin/$ENCRYPT_FILE")
fi

#1. Memory and basic configuration (at the top)
#Pockets: fixed 2g to avoid proliferation; if a container is limited to 5g and it wants to be stored outside/in the system, it can be covered by JAVA OPTS= "-Xmx3500m" etc.
JAVA_OPTS+=("-Xms2g")
JAVA_OPTS+=("-Xmx2g")
JAVA_OPTS+=("-Xss512k")
JAVA_OPTS+=("-XX:MetaspaceSize=256m")
JAVA_OPTS+=("-XX:MaxMetaspaceSize=384m")
#Direct memory: limit NIO etc. external occupancy
JAVA_OPTS+=("-XX:MaxDirectMemorySize=256m")
JAVA_OPTS+=("-XX:+DisableAttachMechanism")
JAVA_OPTS+=("-XX:NativeMemoryTracking=summary")
#Allows adding JVM parameters to environmental variables (e. g 113. JAVA OPTS EXT="-Xmx3500m-XX: MaxDirectMamorySize=512m")
if [[ -n "${JAVA_OPTS_EXT:-}" ]]; then
  for opt in $JAVA_OPTS_EXT; do JAVA_OPTS+=("$opt"); done
fi

#GC Configuration
JAVA_OPTS+=("-XX:+UseG1GC")
JAVA_OPTS+=("-XX:G1HeapRegionSize=16m")
JAVA_OPTS+=("-XX:MaxGCPauseMillis=200")
JAVA_OPTS+=("-XX:+HeapDumpOnOutOfMemoryError")
JAVA_OPTS+=("-XX:HeapDumpPath=$DBS_HOME/logs/heapdump.hprof")
JAVA_OPTS+=("-XX:ErrorFile=$DBS_HOME/logs/hs_err_pid_%p.log")
#GC Log is closed and the following three lines can be deleted if required
# JAVA_OPTS+=("-verbose:gc")
# JAVA_OPTS+=("-XX:+PrintGCDetails")
# JAVA_OPTS+=("-XX:+PrintGCDateStamps")

#4. System Properties offends
JAVA_OPTS+=("-Djava.ext.dirs=$JAVA_EXT_DIR:$DBS_HOME/lib")
JAVA_OPTS+=("-Dspring.config.location=$CONFIG_PATH")
JAVA_OPTS+=("-DLOG_HOME=$DBS_HOME/logs")
JAVA_OPTS+=("-Dsun.stdout.encoding=UTF-8")
JAVA_OPTS+=("-Dfile.encoding=UTF-8")
JAVA_OPTS+=("-Duser.dir=$DBS_HOME")
#Alternatively, there's no-XX--

#5. Main category (at last)
APP="org.dbsyncer.web.Application"

###########################################################################
# execute command
echo "Starting DBSyncer Application..."

nohup java "${JAVA_OPTS[@]}" "$APP"