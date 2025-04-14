# th-bingo

![](https://img.shields.io/github/languages/top/CuteReimu/th-bingo "语言")
![](https://img.shields.io/badge/java%20version-17-informational "Java 17")
[![](https://img.shields.io/github/actions/workflow/status/CuteReimu/th-bingo/build.yml?branch=master)](https://github.com/CuteReimu/th-bingo/actions/workflows/build.yml "代码分析")
[![](https://img.shields.io/github/contributors/CuteReimu/th-bingo)](https://github.com/CuteReimu/th-bingo/graphs/contributors "贡献者")
[![](https://img.shields.io/github/license/CuteReimu/th-bingo)](https://github.com/CuteReimu/th-bingo/blob/master/LICENSE "许可协议")

## 运行

```shell
# 调试命令
./gradlew run
```

> [!NOTE]
> 执行`run`后卡在88%左右是正常现象，并且显示`> :run`是说明已经正在运行了，已经开启监听对应端口了。（为什么不显示100%？因为100%就是运行结束了！可以自行了解一下gradle。）

> [!IMPORTANT]
> `./gradlew run`一般用于本地调试，方便使用IDE工具进行断点调试，占用内存较大。
>
> 想要编译并部署请使用：
>
> ```shell
> # 编译
> ./gradlew build
>
> # 编译后的jar包在build/libs目录下
> cd build/libs
>
> # 部署后自行用java运行
> java -jar th-bingo-1.0.0.jar
> ```

## 协议

协议相关见：[doc](doc/README.md)

## 开发相关

本项目的业务逻辑线程纯用单线程，因此在业务逻辑部分完全不用考虑任何并发问题。

### gradle镜像

如果gradle下载太慢，可以修改`gradle/wrapper/gradle-wrapper.properties`中的`distributionUrl`：

```diff
- distributionUrl=https\://services.gradle.org/distributions/gradle-8.13-bin.zip
+ distributionUrl=https\://mirrors.cloud.tencent.com/gradle/gradle-8.13-bin.zip
```

### maven镜像

如果各种依赖下载太慢，可以修改`build.gradle.kts`，设置镜像。例如：

```diff
repositories {
+   maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}
```

比较推荐的镜像有 `https://maven.aliyun.com/repository/public` 和 `https://mirrors.cloud.tencent.com/nexus/repository/maven-public/` 等