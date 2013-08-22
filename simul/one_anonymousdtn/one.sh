#! /bin/sh
java -Xmx512M -cp .:lib/ECLA.jar:lib/DTNConsoleConnection.jar core.DTNSim $*
cat ./reports/default_scenario_MessageStatsReport.txt
