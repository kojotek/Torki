import matplotlib.pyplot as plt
import pandas as pd
import scipy.stats as stats
import numpy as np
from sklearn.metrics import mean_squared_error
from sklearn.ensemble import RandomForestRegressor
a = 'curvePred0'
b = 'curvePred2'
e = 'steering'
aa = [[-20, -0.0], [-0.2, 0.2], [0.0, 20]]
bb = [[-20, -0.0], [-0.2, 0.2], [0.0, 20]]
cc = [[-20, -0.2], [-1, 1], [0.2, 20]]
dd = [[-20, -0.2], [-1, 1], [0.2, 20]]

df = pd.read_csv('output.csv')

counter = 1

for i in aa:
	for j in bb:
		df = pd.read_csv('output.csv')
		df = df[df[a] > i[0]]
		df = df[df[a] < i[1]]
		df = df[df[b] > j[0]]
		df = df[df[b] < j[1]]
		mean = df.mean(axis=0)[e]
		std = df.std(axis=0)[e]
		print("TERM",e+str(counter),":=","(",(mean-std),", 0) ", "(",(mean),", 1) ", "(",(mean+std),", 0);")
		counter = counter+1

#df.plot(kind='scatter', x=c, y=b)



#plt.show()