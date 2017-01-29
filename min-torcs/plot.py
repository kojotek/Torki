import matplotlib.pyplot as plt
import pandas as pd
df = pd.read_csv('output_pad.csv')
df.plot(kind='scatter', x='trackPos', y='steering')

plt.show()