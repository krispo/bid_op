#!/usr/bin/python
import matplotlib.pyplot as plt

'''
Line Plot
'''
def line(data):   
  print "Line Plot"

  x = data['x']
  y = data['y']

  if y==None: 
    plt.plot(x)
  else:  
    plt.plot(x,y,'-')

  plt.ylabel ('some numbers')
  plt.show()
  return

'''
Scatter Plot
'''
def scatter(data): 
  print "Scatter Plot"

  x = data['x']
  y = data['y']

  plt.plot(x,y,'ro')
  plt.ylabel ('some numbers')
  plt.show()
  return