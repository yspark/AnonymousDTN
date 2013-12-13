import subprocess
import os
import datetime
import sys

# configuration file
SettingFileName = 'anonymous_dtn.txt'


# number of trusted groups
NumTrustedGroups = 1



# simulation running iteration
Run = [0]

# valid epoch num
ValidEpochNumList = [3]

# epoch length 
Epoch = [15*60, 30*60, 45*60, 60*60]

# percentage of trusted node
PercentageOfTrustedNode = [0.05, 0.1, 0.15]

# bloom filter depth
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


#sys.exit()

# last 1 experiment
filename = './results/log_' + datetime.datetime.now().strftime("%m_%d_%H_%M") + '_' + SettingFileName + '.txt' 
foutput = open(filename, 'a')

percentage = 1.0
eppoch = 10*60
validEpochNum = 1


for depth in BloomFilterDepth:
  foutput.write('*********************\n')
  foutput.write('Percentage: ' + str(percentage) + '\tEpoch: ' + str(epoch) + '\tValidEpochNum: ' + str(validEpochNum) + '\n')
  foutput.write('*********************\n')
  
  for depth in BloomFilterDepth:
    command = './one.sh -adtn -b ' + SettingFileName + ' ' + str(NumTrustedGroups) + ' ' + str(percentage) + ' ' + str(epoch) + ' ' + str(validEpochNum) + ' ' + str(run) + ' ' + str(depth)
    print command
    os.system(command)
  #endfor

  freport = open('./reports/anonymous_dtn_MessageStatsReport.txt')
  foutput.writelines(freport.readlines())
  freport.close()
#endfor

foutput.close()



