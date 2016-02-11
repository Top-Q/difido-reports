#!/bin/bash

# The port to use
readonly PORT=8080

# Host name. 
readonly HOST=0.0.0.0

# Set the working directory to the parent folder of the script
cd "$(dirname "$0")"/..

# Launching the server
java -jar -Dserver.address=$HOST -Dserver.port=$PORT lib/difido-server.jar