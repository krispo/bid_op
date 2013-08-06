#!/usr/bin/python
import matplotlib.pyplot as plt

'''
Line Plot
'''
def line(data):   
  print "Line Plot"

  x = data['line']['x']
  y = data['line']['y']

  f = plt.figure()

  if y==None: 
    plt.plot(x[0])
  elif len(x)==1: 
    for i in range(0,len(y)): 
      plt.plot(x[0],y[i],'-')
  else:
    for i in range(0,len(y)): 
      plt.plot(x[i],y[i],'-o')

  plt.title(data['title'])
  plt.xlabel(data['xlabel'])
  plt.ylabel (data['ylabel'])

  #plt.ion()     # turns on interactive mode
  plt.show()    # now this should be non-blocking
  #plt.show(block=True)
  #fig.show()
  return plt

  '''
  Scatter Plot
  '''
def scatter(data): 
  print "Scatter Plot"

  x = data['scatter']['x']
  y = data['scatter']['y']

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