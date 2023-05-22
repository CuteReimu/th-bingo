# th-bingo

![](https://img.shields.io/github/languages/top/CuteReimu/th-bingo "语言")
[![](https://img.shields.io/github/actions/workflow/status/CuteReimu/th-bingo/build.yml?branch=master)](https://github.com/CuteReimu/th-bingo/actions/workflows/build.yml "代码分析")
[![](https://img.shields.io/github/contributors/CuteReimu/th-bingo)](https://github.com/CuteReimu/th-bingo/graphs/contributors "贡献者")
[![](https://img.shields.io/github/license/CuteReimu/th-bingo)](https://github.com/CuteReimu/th-bingo/blob/master/LICENSE "许可协议")

## 使用

需要提前安装Java环境（建议Java 17）

```shell
./gradlew run
```

## 协议

***因为协议会频繁更改，所以下行协议请暂时不要参照此表格***

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

例如`"name": "error_sc"`对应ErrorSc

## 把比赛推送到QQ群

通过 [mirai-http-api](https://github.com/project-mirai/mirai-api-http)
的 **Http Adapter** 将比赛内容推送到QQ群。因此需要首先自行使用 [mirai](https://github.com/mamoe/mirai) 登录QQ。

第一次运行会生成配置文件 `application.properties`，修改后重启即可

```properties
# 推送至少间隔时间（分）
push_interval=10
# 是否开启推送功能
enable_push=true
# bingo比赛的房间url
self_room_addr=http://127.0.0.1:9961/room
# 机器人的QQ号
robot_qq=12345678
# 要推送到的QQ群号
push_qq_groups=12345678,12345678
# mirai-http-api监听的http端口
mirai_http_url=http://127.0.0.1:8080
# mirai-http-api的verifyKey
mirai_verify_key=XXXXXXXX
```
