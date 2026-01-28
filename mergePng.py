from PIL import Image
import os

img_dir = "_multisim"
output_gif = "multiSim.gif"

files = sorted([
    os.path.join(img_dir, f)
    for f in os.listdir(img_dir)
    if f.endswith(".png")
])

frames = []
for f in files:
    img = Image.open(f)
    img = img.convert(
        "P",
        palette=Image.ADAPTIVE,
        colors=128,
        dither=Image.NONE
    )
    frames.append(img)

frames[0].save(
    output_gif,
    save_all=True,
    append_images=frames[1:],
    duration=1000/12.5,
    loop=0
)

