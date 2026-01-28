from jse.code import IO, SP
import math
import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation, PillowWriter

table = IO.csv2table('singleSim.csv')

theta1Func = SP.Groovy.invoke('jse.math.function.Func1.from', table.col('time'), table.col('theta1'))
theta2Func = SP.Groovy.invoke('jse.math.function.Func1.from', table.col('time'), table.col('theta2'))

fps = 12
interp = 10
nframes = int(theta1Func.x().last() * fps)

l1 = 1.0
l2 = 1.5

fig = plt.figure(figsize=(6, 4))
ax = plt.axes([0, 0, 1, 1])
ax.set_aspect('equal')

R = (l1 + l2)
ax.set_xlim(-R, R)
ax.set_ylim(-R, R*0.5)
ax.axis('off')

line, = ax.plot([], [], 'o-', lw=2)
trace, = ax.plot([], [], lw=1, alpha=0.5)

trace_x, trace_y = [], []

def init():
    line.set_data([], [])
    trace.set_data([], [])
    trace_x.clear()
    trace_y.clear()
    return line, trace

def update(i):
    for j in range(interp):
        theta1 = theta1Func.subs(i/fps + j/interp/fps)
        theta2 = theta2Func.subs(i/fps + j/interp/fps)

        x1 =  l1 * math.sin(theta1)
        y1 = -l1 * math.cos(theta1)
        x2 = x1 + l2 * math.sin(theta2)
        y2 = y1 - l2 * math.cos(theta2)

        trace_x.append(x2)
        trace_y.append(y2)

    this_x = [0, x1, x2]
    this_y = [0, y1, y2]

    line.set_data(this_x, this_y)

    trace.set_data(trace_x, trace_y)

    return line, trace

ani = FuncAnimation(
    fig,
    update,
    frames=nframes,
    init_func=init,
    interval=1000/fps,
    blit=True
)
ani.save(
    'singleSim.gif',
    writer=PillowWriter(fps=fps),
    dpi=80
)

plt.close()

