#!/bin/bash
# Grabs and kill a process from the pidlist that has the word difido-server

pid=`ps aux | grep difido-server | awk '{print $2}'`
kill -9 $pid
