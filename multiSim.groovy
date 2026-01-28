import groovy.transform.Field
import jse.code.IO
import jse.code.UT
import sim.DoublePendulum

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.util.concurrent.Future

import static jse.code.UT.Math.*

@Field static final int width = 1024
@Field static final int height = 1024
@Field static Future pngTask = null

// 初始化
def sims = new DoublePendulum[width][height]
for (y in 0..<height) for (x in 0..<width) {
    double theta1 = (x/width - 0.5d) * PI*2d  // 0.16d*PI  0.05d
    double theta2 = (0.5d - y/height) * PI*2d  // 0.88d*PI  0.05d
    sims[x][y] = new DoublePendulum(theta1, theta2)
}

// 动画设置
double fps = 1
double time = 100d // s
double dt = 0.005d

int step = ceil(1/fps/dt) as int
dt = 1/fps/step
int loop = ceil(time*fps) as int

// 保存图像
IO.rmdir('_multisim')
savePng(sims, sprintf('_multisim/%05d.png', 0))
// 进行模拟，并行
UT.Timer.pbar(loop)
for (fi in 1..loop) {
    UT.Par.parfor(width*height) {ii ->
        int y = ii.intdiv(width)
        int x = ii % width
        def sim = sims[x][y]
        sim.run(step, dt)
    }
    // 保存图像
    savePng(sims, sprintf('_multisim/%05d.png', fi))
    UT.Timer.pbar()
}


static void savePng(DoublePendulum[][] sims, String path) {
    def image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    for (y in 0..<height) for (x in 0..<width) {
        def sim = sims[x][y]
        double x1 = sin(sim.theta1), x2 = sin(sim.theta2)
        double y1 = cos(sim.theta1), y2 = cos(sim.theta2)
        float r = 1d - abs(x1+x2)/2d as float
        float b = 1d - abs(x1-x2)/2d as float
        float g = 0.5d + (y1+y2)/4d as float
        image.setRGB(x, y, rgb2int(r, g, b))
    }
    IO.validPath(path)
    // 简单提交后台任务避免 io 阻塞计算
    pngTask?.get()
    pngTask = UT.Par.runAsync {
        ImageIO.write(image, 'png', IO.toFile(path))
    }
}

static int rgb2int(float r, float g, float b) {
    int ri = (int)(r * 255.0f + 0.5f)
    int gi = (int)(g * 255.0f + 0.5f)
    int bi = (int)(b * 255.0f + 0.5f)
    return (ri << 16) | (gi << 8) | bi
}
