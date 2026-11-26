#!/bin/sh
root="${CURSOR_PROJECT_DIR:-$(pwd)}"
printf '{"pluginPaths":["%s/plugins/pstack"]}\n' "$root"
