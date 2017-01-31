import matplotlib.pyplot as plt
import pandas as pd

df = pd.read_csv('output_pad.csv')
#df.plot(kind='scatter', x='track5', y='steering')

a = -0.35
b = -0.2
c = 0.13
d = 0.2

line = pd.DataFrame(
    {'x': [-1, 			a, b, c, d,				1],
     'y': [0, 				0, 1.14, 1.14, 0,			0]})


a2 = -0.15
b2 = -0.02
c2 = 0.13
d2 = 0.43

line2 = pd.DataFrame(
    {'x': [-1, 			a2, b2, c2, d2,				1],
     'y': [0, 				0, -1.14, -1.14, 0,			0]})

#left_2013.plot(kind='line', x='x', y='y')

df['tmp'] = 1
line['tmp'] = 1
line2['tmp'] = 1

df = pd.merge(df, line, on=['tmp'])
df = pd.merge(df, line2, on=['tmp'])
df = df.drop('tmp', axis=1)

ax = line.plot(kind='line', color='red', x='x', y='y')
bx = line2.plot(kind='line', color='green', x='x', y='y', ax=ax)
df.plot(kind='scatter', x='angle', y='steering', ax=bx)

plt.show()