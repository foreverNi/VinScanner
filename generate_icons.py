from PIL import Image, ImageDraw

sizes = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}

base_dir = r"e:\AI\VinScanner\app\src\main\res"
bg_color = (63, 81, 181)  # #3F51B5
text_color = (255, 255, 255)  # white

for density, size in sizes.items():
    # 正方形图标
    img = Image.new('RGB', (size, size), bg_color)
    draw = ImageDraw.Draw(img)

    # 使用整数大小绘制文字（居中）
    text = "V"
    # 根据图标大小调整文字大小比例
    scale = max(1, size // 16)
    for i in range(len(text)):
        # 简单的V字形绘制
        pass

    # 直接绘制文字（使用整数参数）
    draw.text((size//3, size//4), text, fill=text_color)

    out_path = f"{base_dir}\\mipmap-{density}\\ic_launcher.png"
    img.save(out_path, "PNG")
    import os
    sz = os.path.getsize(out_path)
    print(f"Created {out_path} - {sz} bytes")

    # 圆形图标
    img_round = Image.new('RGB', (size, size), bg_color)
    draw_round = ImageDraw.Draw(img_round)
    # 画圆形（用椭圆形近似）
    draw_round.ellipse([2, 2, size-3, size-3], outline=None)
    draw_round.text((size//3, size//4), text, fill=text_color)

    out_path_round = f"{base_dir}\\mipmap-{density}\\ic_launcher_round.png"
    img_round.save(out_path_round, "PNG")
    sz = os.path.getsize(out_path_round)
    print(f"Created {out_path_round} - {sz} bytes")

print("All icons created!")
