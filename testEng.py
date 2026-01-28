from jse.code import IO
import matplotlib.pyplot as plt

# table0 = IO.csv2table('singleSim_old.csv')
table1 = IO.csv2table('singleSim.csv')

time = table1.col('time').numpy()
# energy0 = table0.col('energy').numpy()
energy1 = table1.col('energy').numpy()

plt.gcf().subplots_adjust(left=0.20, bottom=0.15)
# plt.plot(time, energy0, label='euler')
plt.plot(time, energy1, label='rk4')

# plt.axis((0, 100, -16.54, -16.48))
# plt.yticks((-16.60, -16.55, -16.50, -16.45, -16.40))
plt.xlabel('time (s)')
plt.ylabel('total energy (J)')
plt.legend(loc='lower left')

plt.show()

