#!/bin/bash
set -euxo pipefail

mvn dependency:tree > dependency_tree.txt
mvn clean install
