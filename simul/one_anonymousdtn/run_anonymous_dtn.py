import subprocess
import os
import datetime

# configuration file
SettingFileName = 'yspark.txt'

# number of trusted groups
NumTrustedGroups = 1



# valid epoch num
ValidEpochNumList = [6]
Run = [0, 1, 2]
Epoch = [10*60, 20*60, 30*60, 60*60]
PercentageOfTrustedNode = [0.05, 0.1, 0.2, 0.3]


# 1 time test
ValidEpochNumList = [3]
Run = [0]
Epoch = [10*60]
PercentageOfTrustedNode = [0.05]




pwd = os.getcwd();

for validEpochNum in ValidEpochNumList:

  filename = './results/log_' + datetime.datetime.now().strftime("%m_%d_%H_%M") + '_' + SettingFileName + '.txt' 

  for run in Run:
    for epoch in Epoch:
      for percentage in PercentageOfTrustedNode:
    
        foutput = open(filename, 'a')
    
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
      #end for
    #end for
  #end for
#end for     



