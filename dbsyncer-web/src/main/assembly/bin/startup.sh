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
  echo "The conf/application.properties doesn't exist, please check it first!"
  exit 1
fi

# check process
APP="org.dbsyncer.web.Application"
PROCESS=$(pgrep -f "${APP}")
if [[ -n "$PROCESS" ]]; then
  echo "The app already started (PID: $PROCESS)."
  exit 1
fi

###########################################################################
#Parsing Java: Internal jre8 > JRE HOME > JAVA HOME > sudo
#Users usually only configure JAVA HOME; sudo by default clears environment variables
###########################################################################

#Try restoring JAVA HOME from the call user login environment when sudo clears the environment
if [[ -z "${JAVA_HOME:-}" && -z "${JRE_HOME:-}" && -n "${SUDO_USER:-}" ]]; then
  _recovered=$(su - "$SUDO_USER" -c 'printf %s "$JAVA_HOME"' 2>/dev/null | tr -d '\r')
  if [[ -n "$_recovered" && -d "$_recovered" ]]; then
    export JAVA_HOME="$_recovered"
    echo "Recovered JAVA_HOME from user $SUDO_USER: $JAVA_HOME"
  fi
  unset _recovered
fi

#Automatically detect jre8 in the same directory
if [[ -z "${JRE_HOME:-}" && -d "$DBS_HOME/jre8" ]]; then
  export JRE_HOME="$DBS_HOME/jre8"
  echo "Using embedded JRE8: $JRE_HOME"
fi

#Unified Java root directory (JRE HOME priority over JAVA HOME)
JAVA_ROOT="${JRE_HOME:-${JAVA_HOME:-}}"
if [[ -n "${JAVA_HOME:-}" && -z "${JRE_HOME:-}" ]]; then
  echo "Using JAVA_HOME: $JAVA_HOME"
fi

resolve_java_bin() {
  local root="$1"
  if [[ -n "$root" ]]; then
    if [[ -x "$root/bin/java" ]]; then
      echo "$root/bin/java"
      return
    fi
    if [[ -x "$root/jre/bin/java" ]]; then
      echo "$root/jre/bin/java"
      return
    fi
  fi
  if command -v java >/dev/null 2>&1; then
    command -v java
    return
  fi
  echo ""
}

#JRE Extension Directory: must contain sunjce provider, otherwise the TLS newspaper SunTls12RsaPremasterSecretKeyGenerator not available
resolve_jre_ext_dir() {
  local root="$1"
  local java_bin="$2"

  if [[ -n "$root" ]]; then
    if [[ -d "$root/lib/ext" ]]; then
      echo "$root/lib/ext"
      return
    fi
    if [[ -d "$root/jre/lib/ext" ]]; then
      echo "$root/jre/lib/ext"
      return
    fi
  fi

  #Back Simultaneously: extrapolated from actual java.home properties
  if [[ -n "$java_bin" ]]; then
    local java_home_prop
    java_home_prop=$("$java_bin" -XshowSettings:properties -version 2>&1 | tr -d '\r' | sed -n 's/^ *java\.home = //p' | head -1)
    if [[ -n "$java_home_prop" ]]; then
      if [[ -d "$java_home_prop/lib/ext" ]]; then
        echo "$java_home_prop/lib/ext"
        return
      fi
      if [[ -d "$java_home_prop/jre/lib/ext" ]]; then
        echo "$java_home_prop/jre/lib/ext"
        return
      fi
    fi
  fi

  echo ""
}

JAVA_BIN=$(resolve_java_bin "$JAVA_ROOT")
JAVA_EXT_DIR=$(resolve_jre_ext_dir "$JAVA_ROOT" "$JAVA_BIN")

if [[ -z "$JAVA_BIN" ]]; then
  echo "ERROR: Cannot find java. Set JAVA_HOME (JDK/JRE 8) or ensure java is on PATH." >&2
  echo "Note: sudo clears env vars; prefer 'sudo -E ./startup.sh' or export JAVA_HOME before sudo." >&2
  exit 1
fi
if [[ -z "$JAVA_EXT_DIR" ]]; then
  echo "ERROR: Cannot find JRE lib/ext (JCE required for SSL). JAVA_HOME should point to JDK/JRE 8." >&2
  echo "JAVA_HOME=${JAVA_HOME:-} JRE_HOME=${JRE_HOME:-} java=$JAVA_BIN" >&2
  exit 1
fi
echo "JAVA_BIN=$JAVA_BIN"
echo "JAVA_EXT_DIR=$JAVA_EXT_DIR"

###########################################################################
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

#1. Memory and basic configuration (at the top)
JAVA_OPTS+=("-Xms3g")
JAVA_OPTS+=("-Xmx3g")
JAVA_OPTS+=("-Xss512k")
JAVA_OPTS+=("-XX:MetaspaceSize=256m")
JAVA_OPTS+=("-XX:MaxDirectMemorySize=512m")
JAVA_OPTS+=("-XX:+DisableAttachMechanism")

#GC Configuration
JAVA_OPTS+=("-XX:+UnlockExperimentalVMOptions")
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

#3. Agent (if any)
if [ -e "$DBS_HOME/bin/$ENCRYPT_FILE" ]; then
  JAVA_OPTS+=("-agentpath:$DBS_HOME/bin/$ENCRYPT_FILE")
fi

#4. System properties
JAVA_OPTS+=("-Djava.ext.dirs=$JAVA_EXT_DIR:$DBS_HOME/lib")
JAVA_OPTS+=("-Dspring.config.location=$CONFIG_PATH")
JAVA_OPTS+=("-DLOG_HOME=$DBS_HOME/logs")
JAVA_OPTS+=("-Dsun.stdout.encoding=UTF-8")
JAVA_OPTS+=("-Dfile.encoding=UTF-8")
JAVA_OPTS+=("-Duser.timezone=Asia/Shanghai")
JAVA_OPTS+=("-Duser.dir=$DBS_HOME")

#5. Main categories
APP="org.dbsyncer.web.Application"

# execute command
echo "Starting DBSyncer Application..."

nohup "$JAVA_BIN" "${JAVA_OPTS[@]}" "$APP" > /dev/null 2>&1 &
APP_PID=$!

#Save PID and give feedback
echo "$APP_PID" > "$DBS_HOME/tmp.pid"
echo "Start successfully! PID: $APP_PID"
echo "Note: Please check $DBS_HOME/logs for detailed output."
