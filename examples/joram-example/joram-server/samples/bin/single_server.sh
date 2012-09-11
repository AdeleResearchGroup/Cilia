#!/bin/sh

# CYGWIN specific support.
cygwin=false
case "`uname`" in
CYGWIN*) cygwin=true;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$JORAM_HOME" ] && JORAM_HOME=`cygpath --unix "$JORAM_HOME"`
  [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

echo $JORAM_HOME

# Verify if JORAM_HOME is well defined
if [ ! -r "$JORAM_HOME"/samples/bin/clean.sh ]; then
  echo "The JORAM_HOME environment variable is not defined correctly"
  echo "This environment variable is needed to run this program"
  exit 1
fi

# Verify if JAVA_HOME is well defined
if [ ! -r "$JAVA_HOME"/bin/java ]; then
  echo "The JAVA_HOME environment variable is not defined correctly"
  echo "This environment variable is needed to run this program"
  exit 1
fi

CONFIG_DIR=$JORAM_HOME/samples/config
JORAM_BIN=$JORAM_HOME/ship/bin
RUN_DIR=$JORAM_HOME/samples/run
SERVER_RUN_DIR=$RUN_DIR/server0

# Building the Classpath
CLASSPATH=$CLASSPATH:$JORAM_BIN/felix.jar

mkdir $RUN_DIR
mkdir $SERVER_RUN_DIR
cp $CONFIG_DIR/a3config.dtd $SERVER_RUN_DIR/a3config.dtd
cp $CONFIG_DIR/a3debug.cfg $SERVER_RUN_DIR/a3debug.cfg
cp $CONFIG_DIR/centralized_a3servers.xml $SERVER_RUN_DIR/a3servers.xml
cp $CONFIG_DIR/config.properties $SERVER_RUN_DIR/config.properties

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
  JORAM_HOME=`cygpath --path --windows "$JORAM_HOME"`
  CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

echo "== Launching a non persistent server#0 =="
cd $SERVER_RUN_DIR; exec "${JAVA_HOME}"/bin/java -Dfelix.config.properties=file:config.properties -Dfr.dyade.aaa.agent.AgentServer.id=0 -Dcom.sun.management.jmxremote -classpath $CLASSPATH org.apache.felix.main.Main