# th-bingo

![](https://img.shields.io/github/languages/top/CuteReimu/th-bingo "语言")
![](https://img.shields.io/badge/java%20version-17-informational "Java 17")
[![](https://img.shields.io/github/actions/workflow/status/CuteReimu/th-bingo/build.yml?branch=master)](https://github.com/CuteReimu/th-bingo/actions/workflows/build.yml "代码分析")
[![](https://img.shields.io/github/contributors/CuteReimu/th-bingo)](https://github.com/CuteReimu/th-bingo/graphs/contributors "贡献者")
[![](https://img.shields.io/github/license/CuteReimu/th-bingo)](https://github.com/CuteReimu/th-bingo/blob/master/LICENSE "许可协议")

## 使用

```shell
./gradlew run
```

## 协议

协议全部采用json的格式

| 字段      | 类型  | 备注                                      |
|---------|-----|-----------------------------------------|
| name    | str | 协议名                                     |
| reply   | str | 回应的协议名，如果只是推送协议则没有这个字段                  |
| trigger | str | 触发事件的玩家的名字，如果没有则没有这个字段                  |
| data    | obj | 协议内容，下文一一列举（如果返回协议体为空，则没有这个字段，以便减小协议大小） |

示例：

```json
{
  "name": "error_sc",
  "reply": "join_room_cs",
  "trigger": "xxx",
  "data": {
    "code": 1,
    "msg": "create room failed"
  }
}
```

协议与`org.tfcc.bingo.message`下的类(`Dispatcher.kt`、`Handler.kt`、`Message.kt`除外)一一对应。

例如`"name": "error_sc"`对应`ErrorSc`

## 开发相关

### gradle镜像

如果gradle下载太慢，可以修改`gradle/wrapper/gradle-wrapper.properties`中的`distributionUrl`：

```diff
- distributionUrl=https\://services.gradle.org/distributions/gradle-7.4.2-bin.zip
+ distributionUrl=https\://mirrors.cloud.tencent.com/gradle/gradle-7.4.2-bin.zip
