import subprocess
import os
import datetime
import sys

# configuration file
SettingFileName = 'yspark.txt'

# number of trusted groups
NumTrustedGroups = 1



# valid epoch num
ValidEpochNumList = [3]
Run = [0, 1, 2]
Epoch = [15*60, 30*60, 60*60]
PercentageOfTrustedNode = [0.05, 0.1, 0.15]
BloomFilterDepth = [1, 3, 5]


# 1 time test
ValidEpochNumList = [3]
Run = [0]
Epoch = [30*60]
PercentageOfTrustedNode = [0.1]
BloomFilterDepth = [3]



pwd = os.getcwd();

for validEpochNum in ValidEpochNumList:

  filename = './results/log_' + datetime.datetime.now().strftime("%m_%d_%H_%M") + '_' + SettingFileName + '.txt' 

  for run in Run:
    for epoch in Epoch:
      for percentage in PercentageOfTrustedNode:
	      for depth in BloomFilterDepth:
    
		      foutput = open(filename, 'a')
	          
		      foutput.write('*********************\n')
		      foutput.write('Percentage: ' + str(percentage) + '\tEpoch: ' + str(epoch) + '\tValidEpochNum: ' + str(validEpochNum) + '\n')
		      foutput.write('*********************\n')
		
		      command = './one.sh -adtn -b ' + SettingFileName + ' ' + str(NumTrustedGroups) + ' ' + str(percentage) + ' ' + str(epoch) + ' ' + str(validEpochNum) + ' ' + str(run) + ' ' + str(depth)
		      print command
		      os.system(command)
	            
		      freport = open('./reports/anonymous_dtn_MessageStatsReport.txt')
		      foutput.writelines(freport.readlines())
		      freport.close()
	          
		      foutput.close()
        #end for	
      #end for
    #end for
  #end for
#end for     


sys.exit()

# last 1 experiment
filename = './results/log_' + datetime.datetime.now().strftime("%m_%d_%H_%M") + '_' + SettingFileName + '.txt' 
foutput = open(filename, 'a')

percentage = 1.0
eppoch = 10*60
validEpochNum = 1

foutput.write('*********************\n')
foutput.write('Percentage: ' + str(percentage) + '\tEpoch: ' + str(epoch) + '\tValidEpochNum: ' + str(validEpochNum) + '\n')
foutput.write('*********************\n')

command = './one.sh -adtn -b ' + SettingFileName + ' ' + str(NumTrustedGroups) + ' ' + str(percentage) + ' ' + str(epoch) + ' ' + str(validEpochNum) + ' ' + str(run)
print command
os.system(command)

freport = open('./reports/anonymous_dtn_MessageStatsReport.txt')
foutput.writelines(freport.readlines())
freport.close()

foutput.close()



