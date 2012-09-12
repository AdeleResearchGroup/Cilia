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

# Verify if JORAM_HOME is well defined
if [ ! -r "$JORAM_HOME"/samples/bin/clean.sh ]; then
  echo "The JORAM_HOME environment variable is not defined correctly"
  echo "This environment variable is needed to run this program"
  exit 1
fi
RUN_DIR=$JORAM_HOME/samples/run

echo "== Cleaning the persistence directories and configuration settings =="
rm -rf $RUN_DIR
