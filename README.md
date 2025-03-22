# th-bingo

![](https://img.shields.io/github/languages/top/CuteReimu/th-bingo "语言")
![](https://img.shields.io/badge/java%20version-17-informational "Java 17")
[![](https://img.shields.io/github/actions/workflow/status/CuteReimu/th-bingo/build.yml?branch=master)](https://github.com/CuteReimu/th-bingo/actions/workflows/build.yml "代码分析")
[![](https://img.shields.io/github/contributors/CuteReimu/th-bingo)](https://github.com/CuteReimu/th-bingo/graphs/contributors "贡献者")
[![](https://img.shields.io/github/license/CuteReimu/th-bingo)](https://github.com/CuteReimu/th-bingo/blob/master/LICENSE "许可协议")

**本项目目前处于重构阶段，并且在重构结束前，不进行任何BUG的修复和新需求的修改**

## 使用

```shell
./gradlew run
```

## 协议

协议相关见：[doc](doc/README.md)

## 开发相关

### gradle镜像

如果gradle下载太慢，可以修改`gradle/wrapper/gradle-wrapper.properties`中的`distributionUrl`：

```diff
- distributionUrl=https\://services.gradle.org/distributions/gradle-8.13-bin.zip
+ distributionUrl=https\://mirrors.cloud.tencent.com/gradle/gradle-8.13-bin.zip

### maven镜像

如果各种依赖下载太慢，可以修改`build.gradle.kts`，设置镜像。例如：

```diff
repositories {
+   maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}
```

比较推荐的镜像有 `https://maven.aliyun.com/repository/public` 和 `https://mirrors.cloud.tencent.com/nexus/repository/maven-public/` 等