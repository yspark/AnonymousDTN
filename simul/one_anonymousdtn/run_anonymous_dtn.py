import subprocess
import os
import datetime


Run = [0, 1, 2]

# Fixed epoch, varying percentage
#Epoch = [10*60]
#PercentageOfTrustedNode = [0.1, 0.2, 0.3, 0.4, 0.6, 0.8, 1.0]

# Varying epoch, fixed percentage
#Epoch = [5*60, 10*60, 15*60, 20*60, 25*60, 30*60]
#PercentageOfTrustedNode = [0.3]

# Varying epoch and percentage
Epoch = [5*60, 10*60, 20*60, 30*60]
PercentageOfTrustedNode = [0.1, 0.2, 0.3, 0.4, 0.6, 0.8]

# 1 time test
Run = [0]
Epoch = [10*60]
PercentageOfTrustedNode = [0.3]


filename = './results/log_' + datetime.datetime.now().strftime("%m_%d_%H_%M") + '.txt' 

pwd = os.getcwd();

for run in Run:
  for epoch in Epoch:
    for percentage in PercentageOfTrustedNode:
   
      foutput = open(filename, 'a')
  
      foutput.write('**********\n')
      foutput.write('Percentage: ' + str(percentage) + '\t' +'Epoch: ' + str(epoch) + '\n')
      foutput.write('**********\n')
      
      command = './one.sh -adtn -b 1 ' + str(percentage) + ' ' + str(epoch) + ' ' + str(run)
      print command
      os.system(command)
    
      freport = open('./reports/anonymous_dtn_MessageStatsReport.txt')
      foutput.writelines(freport.readlines())
      freport.close()
  
      foutput.close()
    #end for
  #end for
#end for
    



