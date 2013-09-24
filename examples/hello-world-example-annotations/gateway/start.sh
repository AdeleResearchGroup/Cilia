#!/usr/bin/env sh
cd "/home/torito/workspace/Cilia/examples/hello-world-example-annotations/gateway/target/annotated-hello-world-distribution"
exec java $@ -jar bin/felix.jar