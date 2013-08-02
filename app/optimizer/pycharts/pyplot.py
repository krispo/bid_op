#!/usr/bin/python
import matplotlib.pyplot as plt

'''
Line Plot
'''
def line(data):   
  print "Line Plot"

  x = data['x']
  y = data['y']

  #fig = plt.figure()

  if y==None: 
    plt.plot(x)
  else: 
    for i in range(0,len(y)): 
      plt.plot(x,y[i],'-')

  plt.title(data['title'])
  plt.xlabel(data['xlabel'])
  plt.ylabel (data['ylabel'])
  #plt.ion()     # turns on interactive mode
  plt.show()    # now this should be non-blocking
  #plt.show(block=True)
  #fig.show()
  return

'''
Scatter Plot
'''
def scatter(data): 
  print "Scatter Plot"

  x = data['x']
  y = data['y']

  #fig = plt.figure()

  plt.plot(x,y,'go')

  plt.title(data['title'])
  plt.xlabel(data['xlabel'])
  plt.ylabel (data['ylabel'])
  #plt.ion()     # turns on interactive mode
  plt.show()    # now this should be non-blocking
  #plt.show(block=False)
  #fig.show()
  return