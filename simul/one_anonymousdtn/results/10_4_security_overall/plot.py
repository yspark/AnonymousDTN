import matplotlib.pyplot as plt
import sys
import re
from numpy import *
from pylab import *

#freport = open('./secure_epoch6.txt')
freport = open('./normal_epoch6.txt')
#freport = open('./8_26/8_26_FixedEpoch_VaryingPercentage.txt')
#freport = open('./8_26/8_26_VaryingEpoch_FixedPercentage.txt')


# All, Varying epoch and percentage
n_test = 3.0
Epoch = [10*60, 20*60, 30*60, 60*60]
Percentage = [0.0, 0.05, 0.1, 0.15, 0.2, 0.25]

# Fixed epoch, varying percentage
#n_test = 1.0
#Epoch = [10*60]
#Percentage = [0.1, 0.2, 0.3, 0.4, 0.6, 0.8, 1.0]

# Varying epoch, fixed percentage
#n_test = 1.0
#Epoch = [5*60, 10*60, 15*60, 20*60, 25*60, 30*60]
#Percentage = [0.3]


n_epoch = len(Epoch)
n_percentage = len(Percentage)
array_len = n_epoch * n_percentage


markers = ['o', 's', 'v', 'x']
labels = ['Epoch=10mins', 'Epoch=20mins', 'Epoch=30mins', 'Epoch=60mins']



#####################################################################
# Unused data
#####################################################################
num_messages = zeros((n_epoch, n_percentage), dtype=float)



#####################################################################
# Delivery rate (within a anonymity group)
#####################################################################
delivery_rate = zeros((n_epoch, n_percentage), dtype=float)

def plot_delivery_rate():
   
  xtickValues = arange(len(Percentage))  
  ytickValues = arange(0.5, 1.01, 0.1)

  xValues = Percentage
  xValues[0] = "0.0\n(Pure epidemic)"

  # plot x:percentage, y:delivery_rate
  for i in range(0, n_epoch):
    yValues = delivery_rate[i,:]

    plt.plot(xtickValues, yValues, label=labels[i], marker=markers[i])
  #endfor

  
  plt.xticks(xtickValues, xValues)   
  plt.yticks(ytickValues, ytickValues)


  plt.xlabel('Percentage of trusted hosts')
  plt.ylabel('Delivery rate')
 
  
  legend = plt.legend(loc='lower right')
  
  #plt.show()
  savefig('delivery_rate.pdf')
  plt.close()
#end def




#####################################################################
# Delivery rate (detail)
#####################################################################
created_t_to_t = zeros((n_epoch, n_percentage), dtype=float)
created_t_to_ut = zeros((n_epoch, n_percentage), dtype=float)
created_ut_to_t = zeros((n_epoch, n_percentage), dtype=float)
created_ut_to_ut = zeros((n_epoch, n_percentage), dtype=float)

delivered_t_to_t = zeros((n_epoch, n_percentage), dtype=float)
delivered_t_to_ut = zeros((n_epoch, n_percentage), dtype=float)
delivered_ut_to_t = zeros((n_epoch, n_percentage), dtype=float)
delivered_ut_to_ut = zeros((n_epoch, n_percentage), dtype=float)

delivery_rate_t_to_t = zeros((n_epoch, n_percentage), dtype=float)
delivery_rate_t_to_ut = zeros((n_epoch, n_percentage), dtype=float)
delivery_rate_ut_to_t = zeros((n_epoch, n_percentage), dtype=float)
delivery_rate_ut_to_ut = zeros((n_epoch, n_percentage), dtype=float)


def plot_delivery_rate_detail():
  delivery_rate_t_to_t =  delivered_t_to_t / created_t_to_t 
  delivery_rate_t_to_ut =  delivered_t_to_ut / created_t_to_ut
  delivery_rate_ut_to_t =  delivered_ut_to_t / created_ut_to_t
  delivery_rate_ut_to_ut =  delivered_ut_to_ut / created_ut_to_ut     
   
   
  xtickValues = arange(len(Percentage))  
  ytickValues = arange(0.0, 1.01, 0.1)

  xValues = Percentage[0:len(Percentage)]
  xValues[0] = "0.0\n(Pure epidemic)"

  for k in range(0, 4):
    if k == 0:
      yValueMatrix = delivery_rate_t_to_t
    elif k == 1:
      yValueMatrix = delivery_rate_t_to_ut
    elif k == 2:
      yValueMatrix = delivery_rate_ut_to_t
    elif k == 3:
      yValueMatrix = delivery_rate_ut_to_ut


    for i in range(0, n_epoch):
      yValues = yValueMatrix[i,:]

      plt.plot(xtickValues, yValues, label=labels[i], marker=markers[i])
    #endfor

    
    plt.xticks(xtickValues, xValues)   
    plt.yticks(ytickValues, ytickValues)


    plt.xlabel('Percentage of trusted hosts')
    plt.ylabel('Delivery rate')
   
    
    legend = plt.legend(loc='lower right')
    
    #plt.show()
    
    
    if k == 0:
      savefig('delivery_rate_t_to_t.pdf')
    elif k == 1:
      savefig('delivery_rate_t_to_ut.pdf')
    elif k == 2:
      savefig('delivery_rate_ut_to_t.pdf')
    elif k == 3:
      savefig('delivery_rate_ut_to_ut.pdf')
    
    plt.close()
  #endfor
#end def





#####################################################################
# Delivery latency
#####################################################################
latency = zeros((n_epoch, n_percentage), dtype=float)

def plot_latency():
  # plot x:percentage, y:latency

  xtickValues = arange(len(Percentage))  
  ytickValues = arange(5000, 9001, 1000)
  
  xValues = Percentage
  xValues[0] = "0.0\n(Pure epidemic)"
  
  for i in range(0, n_epoch):
    yValues = latency[i,:]
  
    plt.plot(xtickValues, yValues, label=labels[i], marker=markers[i])
  #endfor
  
  plt.xticks(xtickValues, xValues)
  plt.yticks(ytickValues, ytickValues)


  plt.xlabel('Percentage of trusted hosts')
  plt.ylabel('Delivery Latency (sec)')


  legend = plt.legend(loc='upper right')
  
  #plt.show()
  savefig('delivery_latency.pdf')
  plt.close()
#end def



#####################################################################
# Delivery hop count
#####################################################################
hopcount = zeros((n_epoch, n_percentage), dtype=float)

def plot_hopcount():
  # plot x:percentage, y:hopcount
  
  xtickValues = arange(len(Percentage))  
  ytickValues = arange(2, 6.1, 1)

  xValues = Percentage
  xValues[0] = "0.0\n(Pure epidemic)"

  for i in range(0, n_epoch):

    yValues = hopcount[i,:]
  
    plt.plot(xtickValues, yValues, label=labels[i], marker=markers[i])
  #endfor
  


  plt.xticks(xtickValues, xValues)
  plt.yticks(ytickValues, ytickValues)


  plt.xlabel('Percentage of trusted hosts')
  plt.ylabel('Hopcounts')

  legend = plt.legend(loc='upper right')
  
  #plt.show()
  savefig('hopcount.pdf')
  plt.close()
#end def



#####################################################################
# Total number of packet relays
#####################################################################
total_relays = zeros((n_epoch, n_percentage), dtype=float)

def plot_relay_count():
  # plot x:percentage, y:average relay per message
  
  xtickValues = arange(len(Percentage))  
  ytickValues = arange(100000, 120001, 5000)

  xValues = Percentage
  xValues[0] = "0.0\n(Pure epidemic)"
    
  for i in range(0, n_epoch):
    yValues = total_relays[i,:]

    plt.plot(xtickValues, yValues, label=labels[i], marker=markers[i])
  #endfor


  plt.xticks(xtickValues, xValues) 
  plt.yticks(ytickValues, ytickValues)


  plt.xlabel('Percentage of trusted hosts')
  plt.ylabel('Overall packet relay count')
  
  legend = plt.legend(loc='lower right')
  
  #plt.show()
  savefig('relay_count.pdf')
  plt.close()

#end def



#####################################################################
# Number of delivered packet relays
#####################################################################
relayed_delivery = zeros((n_epoch, n_percentage), dtype=float)

def plot_relays_delivery():


  # plot x:percentage, y:# deliveries with untrusted nodes

  xtickValues = arange(len(Percentage))  
  ytickValues = arange(1500, 2101, 100)

  xValues = Percentage
  xValues[0] = "0.0\n(Pure epidemic)"
  
  for i in range(0, n_epoch):
    yValues = relayed_delivery[i] 
    
    plt.plot(xtickValues, yValues, label=labels[i], marker=markers[i])
  #endfor

  plt.xticks(xtickValues, xValues)
  plt.yticks(ytickValues, ytickValues)


  plt.xlabel('Percentage of trusted hosts')
  plt.ylabel('Delivered packet relay count')
  
  legend = plt.legend(loc='upper right')
  
  #plt.show()
  savefig('relay_delivery_count.pdf')
  plt.close()

#end def


'''
#####################################################################
# Average relays per message
#####################################################################
num_messages = zeros((n_epoch, n_percentage), dtype=float)
total_relays = zeros((n_epoch, n_percentage), dtype=float)

def plot_avg_relay_per_message():
  # plot x:percentage, y:average relay per message
  
  xtickValues = arange(0.1, 1.1, 0.1)
  ytickValues = arange(4.0, 22.1, 2.0)


  for i in range(0, n_epoch):
    xValues = Percentage
    yValues = total_relays[i,:] / num_messages[i,:]

    plt.plot(xValues, yValues, label=labels[i], marker=markers[i])
  #endfor


  plt.xticks(xtickValues, xtickValues) 
  plt.yticks(ytickValues, ytickValues)


  plt.xlabel('Percentage of trusted hosts')
  plt.ylabel('Average relay count per message')
  
  legend = plt.legend(loc='lower right')
  
  #plt.show()
  savefig('relay_per_message.pdf')
  plt.close()

#end def
'''


#####################################################################
# Percentage of deliveries with untrusted nodes
#####################################################################
delivered = zeros((n_epoch, n_percentage), dtype=float)
delivered_with_ut_hops = zeros((n_epoch, n_percentage), dtype=float)

def plot_delivery_with_ut_count():
  # plot x:percentage, y:# deliveries with untrusted nodes

  xtickValues = arange(len(Percentage))  
  ytickValues = arange(0.0, 1.01, 0.1)
  

  xValues = Percentage
  xValues[0] = "0.0\n(Pure epidemic)"

  for i in range(0, n_epoch):
    yValues = delivered_with_ut_hops[i,:] / delivered[i,:]
    
    plt.plot(xtickValues, yValues, label=labels[i], marker=markers[i])

  #endfor


  plt.xticks(xtickValues, xValues)
  plt.yticks(ytickValues, ytickValues)


  plt.xlabel('Percentage of trusted hosts')
  plt.ylabel('Percentage of deliveries with untrusted nodes')
  
  legend = plt.legend(loc='lower left')
  
  #plt.show()
  savefig('delivery_with_ut.pdf')
  plt.close()

#end def

'''
def plot_delivered_count_over_epoch():
  # plot x:epoch, y:delivered_count
  
  width = 1.5
  
  xValues = array(Epoch) / 60
  
  yValues1 = delivered - delivered_with_ut_hops
  yValues2 = delivered_with_ut_hops


  p1 = plt.bar(xValues, yValues1, width, color='r', label='Trusted hops only')
  p2 = plt.bar(xValues, yValues2, width, bottom=yValues1, color='b', label='Trusted+untrusted hops')

  plt.xticks(xValues+width/2.0, xValues)
  
  legend = plt.legend(loc='upper center', bbox_to_anchor=(0.5, 1.13))
  
  plt.xlabel('Epoch (mins)')
  plt.ylabel('# delivered packets')
  
  #plt.show()
  savefig('delivered_co.pdf')
  plt.close()

#end def

def plot_delivered_count_over_percentage():
  # plot x:percentage, y:delivered_count
  
  width = 0.05
  
  xValues = array(Percentage)
  
  yValues1 = delivered[0] - delivered_with_ut_hops[0]
  yValues2 = delivered_with_ut_hops[0]

  print xValues
  print yValues1
  print yValues2
  
  
  p1 = plt.bar(xValues, yValues1, width, color='r', label='Trusted hops only')
  p2 = plt.bar(xValues, yValues2, width, bottom=yValues1, color='b', label='Trusted+untrusted hops')

  plt.xticks(xValues+width/2.0, xValues)
  
  legend = plt.legend(loc='upper center', bbox_to_anchor=(0.5, 1.13))
  #plt.legend((p1[0], p2[0]), ('Delivered', 'Delivered with untrusted hops'))
  plt.xlabel('Percentage of trusted nodes')
  plt.ylabel('# delivered packets')
 

  plt.show()
#end def
'''





#####################################################################
# Overall Relay classification
#####################################################################
relayed_t_to_t = zeros((n_epoch, n_percentage), dtype=float)
relayed_t_to_ut = zeros((n_epoch, n_percentage), dtype=float)
relayed_ut_to_t = zeros((n_epoch, n_percentage), dtype=float)
relayed_ut_to_ut = zeros((n_epoch, n_percentage), dtype=float)


def plot_relay_classification_over_epoch():
  # plot x:epoch, y:delivered_count
  
 
  xValues = array(Epoch) / 60

  yValues1 = relayed_t_to_t[:,2]
  yValues2 = relayed_t_to_ut[:,2]
  yValues3 = relayed_ut_to_t[:,2]
  yValues4 = relayed_ut_to_ut[:,2]


  width = 0.4

  xtickValues = arange(len(Epoch))  
  ytickValues = arange(0.0, 120001, 10000)



  p1 = plt.bar(xtickValues, yValues1, width, color='r', label='Trusted->trusted')
  p2 = plt.bar(xtickValues, yValues2, width, bottom=yValues1, color='b', label='Trusted->untrusted')
  p3 = plt.bar(xtickValues, yValues3, width, bottom=yValues1+yValues2, color='gray', label='Untrust->trusted')
  p4 = plt.bar(xtickValues, yValues4, width, bottom=yValues1+yValues2+yValues3, color='green', label='Untrust->untrusted')



  plt.xticks(xtickValues+width/2.0, xValues)   
  plt.yticks(ytickValues, ytickValues)


  legend = plt.legend(loc='upper center', bbox_to_anchor=(0.5, 1.11), ncol=2)
    
  plt.xlabel('Epoch (mins)')
  plt.ylabel('Overall packet relay count')
   
  #plt.show()
  savefig('relay_classification_over_epoch.pdf')

  plt.close()
#end def



def plot_relay_classification_over_percentage():
  # plot x:percentage, y:delivered_count
  
  width = 0.4
  
  xValues = array(Percentage)
  xValues[0] = "0.0\n(Pure epidemic)"
  
  yValues1 = relayed_t_to_t[2]
  yValues2 = relayed_t_to_ut[2]
  yValues3 = relayed_ut_to_t[2]
  yValues4 = relayed_ut_to_ut[2]


  xtickValues = arange(len(relayed_t_to_t[2]))  
  ytickValues = arange(0.0, 120001, 10000)


  p1 = plt.bar(xtickValues, yValues1, width, color='r', label='Trusted->trusted')
  p2 = plt.bar(xtickValues, yValues2, width, bottom=yValues1, color='b', label='Trusted->untrusted')
  p3 = plt.bar(xtickValues, yValues3, width, bottom=yValues1+yValues2, color='gray', label='Untrust->trusted')
  p4 = plt.bar(xtickValues, yValues4, width, bottom=yValues1+yValues2+yValues3, color='green', label='Untrust->untrusted')



  plt.xticks(xtickValues+width/2.0, xValues)
  plt.yticks(ytickValues, ytickValues)



  legend = plt.legend(loc='upper center', bbox_to_anchor=(0.5, 1.11), ncol=2)
  
  plt.xlabel('Percentage of trusted nodes')
  plt.ylabel('Overall packet relay count')
 

  #plt.show()
  savefig('relay_classification_over_percentage.pdf')

  plt.close()
#end def




#####################################################################
# Delivered packet relay classification
#####################################################################
relayed_delivery_t_to_t = zeros((n_epoch, n_percentage), dtype=float)
relayed_delivery_t_to_ut = zeros((n_epoch, n_percentage), dtype=float)
relayed_delivery_ut_to_t = zeros((n_epoch, n_percentage), dtype=float)
relayed_delivery_ut_to_ut = zeros((n_epoch, n_percentage), dtype=float)


def plot_relay_delivery_classification_over_epoch():
  # plot x:epoch, y:delivered_count
  
 
  xValues = array(Epoch) / 60

  yValues1 = relayed_delivery_t_to_t[:,2]
  yValues2 = relayed_delivery_t_to_ut[:,2]
  yValues3 = relayed_delivery_ut_to_t[:,2]
  yValues4 = relayed_delivery_ut_to_ut[:,2]


  width = 0.4

  xtickValues = arange(len(Epoch))  
  ytickValues = arange(0.0, 2001.0, 200.0)


  p1 = plt.bar(xtickValues, yValues1, width, color='r', label='Trusted->trusted')
  p2 = plt.bar(xtickValues, yValues2, width, bottom=yValues1, color='b', label='Trusted->untrusted')
  p3 = plt.bar(xtickValues, yValues3, width, bottom=yValues1+yValues2, color='gray', label='Untrust->trusted')
  p4 = plt.bar(xtickValues, yValues4, width, bottom=yValues1+yValues2+yValues3, color='green', label='Untrust->untrusted')



  plt.xticks(xtickValues+width/2.0, xValues)   
  plt.yticks(ytickValues, ytickValues)


  legend = plt.legend(loc='upper center', bbox_to_anchor=(0.5, 1.11), ncol=2)
    
  plt.xlabel('Epoch (mins)')
  plt.ylabel('Delivered packet relay count')
   
  #plt.show()
  savefig('relay_delivery_classification_over_epoch.pdf')

  plt.close()
#end def



def plot_relay_delivery_classification_over_percentage():
  # plot x:percentage, y:delivered_count
  
  width = 0.4
  
  xValues = array(Percentage)
  xValues[0] = "0.0\n(Pure epidemic)"
  
  yValues1 = relayed_delivery_t_to_t[2]
  yValues2 = relayed_delivery_t_to_ut[2]
  yValues3 = relayed_delivery_ut_to_t[2]
  yValues4 = relayed_delivery_ut_to_ut[2]


  xtickValues = arange(len(relayed_delivery_t_to_t[2]))  
  ytickValues = arange(0.0, 2001, 200)


  p1 = plt.bar(xtickValues, yValues1, width, color='r', label='Trusted->trusted')
  p2 = plt.bar(xtickValues, yValues2, width, bottom=yValues1, color='b', label='Trusted->untrusted')
  p3 = plt.bar(xtickValues, yValues3, width, bottom=yValues1+yValues2, color='gray', label='Untrust->trusted')
  p4 = plt.bar(xtickValues, yValues4, width, bottom=yValues1+yValues2+yValues3, color='green', label='Untrust->untrusted')



  plt.xticks(xtickValues+width/2.0, xValues)
  plt.yticks(ytickValues, ytickValues)



  legend = plt.legend(loc='upper center', bbox_to_anchor=(0.5, 1.11), ncol=2)
  
  plt.xlabel('Percentage of trusted nodes')
  plt.ylabel('Delivered packet relay count')
 

  #plt.show()
  savefig('relay_delivery_classification_over_percentage.pdf')

  plt.close()
#end def












#####################################################################
# Drop classification
#####################################################################
relay = zeros((n_epoch, n_percentage), dtype=float)

drop_buffer_full = zeros((n_epoch, n_percentage), dtype=float)
drop_ttl_expiry = zeros((n_epoch, n_percentage), dtype=float)
drop_ephemeral_expiry = zeros((n_epoch, n_percentage), dtype=float)

def plot_drop_classification_over_epoch():
  # plot x:epoch, y:delivered_count
  width = 0.4
  
  xValues = array(Epoch) / 60
  
  yValues1 = drop_buffer_full[:,2]
  yValues2 = drop_ttl_expiry[:,2]
  yValues3 = drop_ephemeral_expiry[:,2]


  xtickValues = arange(len(Epoch))  
  ytickValues = arange(0.0, 280001, 40000)


  p1 = plt.bar(xtickValues, yValues1, width, color='r', label='Buffer overflow')
  p2 = plt.bar(xtickValues, yValues2, width, bottom=yValues1, color='b', label='TTL expiry')
  p3 = plt.bar(xtickValues, yValues3, width, bottom=yValues1+yValues2, color='gray', label='Ephemeral ID expiry')


  plt.xticks(xtickValues+width/2.0, xValues)
  plt.yticks(ytickValues, ytickValues)


  
  #legend = plt.legend(loc='upper left', bbox_to_anchor=(0.0, 1.11))
  legend = plt.legend(loc='upper center', bbox_to_anchor=(0.5, 1.12), ncol=3)  

  plt.xlabel('Epoch (mins)')
  plt.ylabel('# packet drops')
 
  #plt.show()
  savefig('drop_classification_over_epoch.pdf')
  plt.close()
#end def



def plot_drop_classification_over_percentage():
  # plot x:percentage, y:delivered_count
    
  xValues = array(Percentage)
  xValues[0] = "0.0\n(Pure epidemic)"

  yValues1 = drop_buffer_full[2]
  yValues2 = drop_ttl_expiry[2]
  yValues3 = drop_ephemeral_expiry[2]


  width = 0.4

  xtickValues = arange(len(Percentage))  
  ytickValues = arange(0.0, 360001, 40000)

  p1 = plt.bar(xtickValues, yValues1, width, color='r', label='Buffer overflow')
  p2 = plt.bar(xtickValues, yValues2, width, bottom=yValues1, color='b', label='TTL expiry')
  p3 = plt.bar(xtickValues, yValues3, width, bottom=yValues1+yValues2, color='gray', label='Ephemeral ID expiry')


  plt.xticks(xtickValues+width/2.0, xValues)
  plt.yticks(ytickValues, ytickValues)
  
  #legend = plt.legend(loc='upper left')
  legend = plt.legend(loc='upper center', bbox_to_anchor=(0.5, 1.12), ncol=3)  
  
  plt.xlabel('Percentage of trusted nodes')
  plt.ylabel('# packet drops')
 

  #plt.show()
  savefig('drop_classification_over_percentage.pdf')
  plt.close()
#end def






phase = 0


for line in freport.readlines():
  if '*********' in line:
    if phase == 0:
      phase = 1
    else:
      phase = 2
  elif phase == 1:
    ele = re.split(":|\t|\n| ", line)
    
    epoch = Epoch.index(int(ele[5]))
    percentage = Percentage.index(float(ele[2]))

  elif phase == 2:
    # delivery rate
    if 'delivery_prob' in line:
      ele = re.split(":|\t|\n| ", line)
   
      #print "insert(%d, %d)\n" % ( epoch, percentage)
      delivery_rate[epoch, percentage] += float(ele[2])
    
    # messages generated
    elif 'created:' in line:
      ele = re.split(":|\t|\n| ", line.strip())
      num_messages[epoch, percentage] += float(ele[2])
      
    # messages generated (classification)
    elif 'created_' in line:
      ele = re.split(":|\t|\n| ", line.strip())

      if 'created_t_to_t' in line:
        created_t_to_t[epoch, percentage] += float(ele[2])
      elif 'created_t_to_ut' in line:
        created_t_to_ut[epoch, percentage] += float(ele[2])
      elif 'created_ut_to_t' in line:
        created_ut_to_t[epoch, percentage] += float(ele[2])
      else:
        created_ut_to_ut[epoch, percentage] += float(ele[2])


    # delivery latency
    elif 'latency_avg' in line:
      ele = re.split(":|\t|\n| ", line)
      
      latency[epoch, percentage] += float(ele[2])

    # overall packet relay count
    elif 'relayed:' in line:
      ele = re.split(":|\t|\n| ", line)
      total_relays[epoch, percentage] += float(ele[2])

    # overall packet relay classification    
    elif 'nrelayed_t' in line or 'nrelayed_ut' in line:      
      ele = re.split(":|\t|\n| ", line.strip())

      if 'nrelayed_t_to_t' in ele[0]:
        relayed_t_to_t[epoch, percentage] += float(ele[2])
      elif 'nrelayed_t_to_ut' in ele[0]:
        relayed_t_to_ut[epoch, percentage] += float(ele[2])
      elif 'nrelayed_ut_to_t' in ele[0]:
        relayed_ut_to_t[epoch, percentage] += float(ele[2])
      else:
        relayed_ut_to_ut[epoch, percentage] += float(ele[2])
      #endif

    # delivered packet relay classification    
    elif 'relayed_delivery' in line:      
      ele = re.split(":|\t|\n| ", line.strip())

      if 'nrelayed_delivery_t_to_t' in ele[0]:
        relayed_delivery_t_to_t[epoch, percentage] += float(ele[2])
      elif 'nrelayed_delivery_t_to_ut' in ele[0]:
        relayed_delivery_t_to_ut[epoch, percentage] += float(ele[2])
      elif 'nrelayed_delivery_ut_to_t' in ele[0]:
        relayed_delivery_ut_to_t[epoch, percentage] += float(ele[2])
      elif 'nrelayed_delivery_ut_to_ut' in ele[0]:
        relayed_delivery_ut_to_ut[epoch, percentage] += float(ele[2])
      else:
        relayed_delivery[epoch, percentage] += float(ele[2])
      #endif

    # packet drop classification
    elif 'dropped_' in line:
      ele = re.split(":|\t|\n| ", line.strip())
      
      if 'dropped_buffer_full' in ele[0]:
        drop_buffer_full[epoch, percentage] += float(ele[2])
      elif 'dropped_ttl_expiry' in ele[0]:
        drop_ttl_expiry[epoch, percentage] += float(ele[2])
      elif 'dropped_ephmeral_expiry' in ele[0]:
        drop_ephemeral_expiry[epoch, percentage] += float(ele[2])
      #endif

		# packet delivered 
    elif 'delivered' in line:
      ele = re.split(":|\t|\n| ", line.strip())

      if 'delivered_with_ut_hops' in line:
        delivered_with_ut_hops[epoch, percentage] += float(ele[2])
      elif 'delivered:' in line:
        delivered[epoch, percentage] += float(ele[2])
      elif 'delivered_t_to_t' in line:
        delivered_t_to_t[epoch, percentage] += float(ele[2])
      elif 'delivered_t_to_ut' in line:
        delivered_t_to_ut[epoch, percentage] += float(ele[2])
      elif 'delivered_ut_to_t' in line:
        delivered_ut_to_t[epoch, percentage] += float(ele[2])
      elif 'delivered_ut_to_ut' in line:
        delivered_ut_to_ut[epoch, percentage] += float(ele[2])

      
    # delivery hop count ===> should be the last one
    elif 'hopcount_avg' in line:
      ele = re.split(":|\t|\n| ", line)

      hopcount[epoch, percentage] += float(ele[2])
      
      
      phase = 0
    #endif

  #endif
#endfor

delivery_rate /= n_test
latency /= n_test
hopcount /= n_test

total_relays /= n_test
relayed_t_to_t /= n_test
relayed_t_to_ut /= n_test
relayed_ut_to_t /= n_test
relayed_ut_to_ut /= n_test


relayed_delivery /= n_test
relayed_delivery_t_to_t /= n_test
relayed_delivery_t_to_ut /= n_test
relayed_delivery_ut_to_t /= n_test
relayed_delivery_ut_to_ut /= n_test

delivered_with_ut_hops /= n_test
delivered /= n_test

drop_buffer_full /= n_test
drop_ttl_expiry /= n_test
drop_ephemeral_expiry /= n_test

delivered_t_to_t /= n_test
delivered_t_to_ut /= n_test
delivered_ut_to_t /= n_test
delivered_ut_to_ut /= n_test

created_t_to_t /= n_test
created_t_to_ut /= n_test
created_ut_to_t /= n_test
created_ut_to_ut /= n_test

#num_messages /= n_test





plot_delivery_rate()

plot_delivery_rate_detail()

plot_latency()

plot_hopcount()

plot_relay_count()
plot_relay_classification_over_epoch()
plot_relay_classification_over_percentage()

plot_relays_delivery()
plot_relay_delivery_classification_over_epoch()
plot_relay_delivery_classification_over_percentage()

plot_delivery_with_ut_count()

plot_drop_classification_over_epoch()
plot_drop_classification_over_percentage()





#plot_avg_relay_per_message()




