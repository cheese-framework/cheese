// 创建一个名为 "default" 的配置
configurations.maybeCreate("default")
// 将本地的 test.aar 文件添加到 "default" 配置中
artifacts.add("default", file("opencv-4.8.0.aar"))
