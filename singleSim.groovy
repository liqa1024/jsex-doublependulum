import groovy.transform.CompileStatic
import groovy.transform.Field
import jse.code.IO
import jse.code.UT

import static jse.code.UT.Math.*

// 全局参数，这里选择一个非平凡的参数来测试公式正确性
@Field static final double l1 = 1.0d
@Field static final double l2 = 1.5d
@Field static final double m1 = 2.0d
@Field static final double m2 = 0.7d
@Field static final double g = 9.8d
// 模拟参数
final boolean rk4 = true
final double dt = 0.005d
final int loop = 100000
final int step = 10
final String csvPath = 'singleSim.csv'

// 初始角度
double theta1 =  PI * 0.5d
double theta2 = -PI * 0.55d

// 初始角速度
double omega1 = 0.0d
double omega2 = 0.0d

double[] beta1 = new double[1]
double[] beta2 = new double[1]

def writer = IO.toWriteln(csvPath)
writer.writeln('time,theta1,theta2,omega1,omega2,energy')
UT.Timer.pbar(loop/step as int)
for (int i = 0; i < loop; ++i) {
    if (rk4) {
        // k1
        double k1_t1 = omega1
        double k1_t2 = omega2
        calBeta12(theta1, theta2, k1_t1, k1_t2, beta1, beta2)
        double k1_w1 = beta1[0]
        double k1_w2 = beta2[0]
        // k2
        double k2_t1 = omega1 + 0.5d*k1_w1*dt
        double k2_t2 = omega2 + 0.5d*k1_w2*dt
        calBeta12(theta1 + 0.5d*k1_t1*dt,
                  theta2 + 0.5d*k1_t2*dt,
                  k2_t1, k2_t2, beta1, beta2)
        double k2_w1 = beta1[0]
        double k2_w2 = beta2[0]
        // k3
        double k3_t1 = omega1 + 0.5d*k2_w1*dt
        double k3_t2 = omega2 + 0.5d*k2_w2*dt
        calBeta12(theta1 + 0.5d*k2_t1*dt,
                  theta2 + 0.5d*k2_t2*dt,
                  k3_t1, k3_t2, beta1, beta2)
        double k3_w1 = beta1[0]
        double k3_w2 = beta2[0]
        // k4
        double k4_t1 = omega1 + k3_w1*dt
        double k4_t2 = omega2 + k3_w2*dt
        calBeta12(theta1 + k3_t1*dt,
                  theta2 + k3_t2*dt,
                  k4_t1, k4_t2, beta1, beta2)
        double k4_w1 = beta1[0]
        double k4_w2 = beta2[0]
        // 更新状态
        theta1 += dt/6d * (k1_t1 + 2d*k2_t1 + 2d*k3_t1 + k4_t1)
        theta2 += dt/6d * (k1_t2 + 2d*k2_t2 + 2d*k3_t2 + k4_t2)
        omega1 += dt/6d * (k1_w1 + 2d*k2_w1 + 2d*k3_w1 + k4_w1)
        omega2 += dt/6d * (k1_w2 + 2d*k2_w2 + 2d*k3_w2 + k4_w2)
    } else {
        calBeta12(theta1, theta2, omega1, omega2, beta1, beta2)
        omega1 += dt*beta1[0]
        omega2 += dt*beta2[0]
        theta1 += dt*omega1
        theta2 += dt*omega2
    }
    // 写入输出
    if (i%step == 0) {
        double engPot = -g * (m1*l1*cos(theta1) + m2*(l1*cos(theta1) + l2*cos(theta2)))
        double engKin = 0.5d * (m1*pow2(l1*omega1) + m2*(pow2(l1*omega1) + pow2(l2*omega2) + 2d*l1*l2*omega1*omega2*cos(theta2-theta1)))
        writer.writeln("${i*dt},$theta1,$theta2,$omega1,$omega2,${engPot+engKin}")
        UT.Timer.pbar()
    }
}
writer.close()


@CompileStatic
static void calBeta12(double theta1, double theta2, double omega1, double omega2, double[] beta1, double[] beta2) {
    // 根据当前状态计算 F2 F1
    double _upper = l2*pow2(omega2) + (l1*pow2(omega1) + g*cos(theta1))*cos(theta2-theta1)
    double _lower = m1/m2 + pow2(sin(theta2-theta1))
    double force2m1 = _upper / _lower
    double force1m1 = l1*pow2(omega1) + force2m1*cos(theta2-theta1) + g*cos(theta1)
    // 根据 F2 计算角加速度
    beta1[0] = (force2m1*sin(theta2-theta1) - g*sin(theta1))/l1
    beta2[0] = -force1m1*sin(theta2-theta1)/l2
}
