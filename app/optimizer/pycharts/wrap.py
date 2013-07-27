#!/usr/bin/python
import sys
import json
from pyplot import *

def plot(method, data): 
	print method
	#print data
	#data = json.loads(param)

	if method=="line":
		line(data)
	elif method=="scatter":
		scatter(data)
	else: 
		print "Such method is NOT exists..."

	return

def fread(fname):
	data = []
	with open(fname) as f:
		for line in f:
			data.append(json.loads(line))
	return data

''' 
sys.argv[1] - method
sys.argv[2] - param
''' 
plot(sys.argv[1], fread(sys.argv[2])[0])

