# th-bingo

![](https://img.shields.io/github/languages/top/CuteReimu/th-bingo "语言")
[![](https://img.shields.io/github/actions/workflow/status/CuteReimu/th-bingo/build.yml?branch=master)](https://github.com/CuteReimu/th-bingo/actions/workflows/build.yml "代码分析")
[![](https://img.shields.io/github/contributors/CuteReimu/th-bingo)](https://github.com/CuteReimu/th-bingo/graphs/contributors "贡献者")
[![](https://img.shields.io/github/license/CuteReimu/th-bingo)](https://github.com/CuteReimu/th-bingo/blob/master/LICENSE "许可协议")

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

### 下行协议

下行协议一共只有六种：成功协议、错误信息协议、心跳返回、房间信息的同步协议、游戏表格的同步协议、游戏表格选择状态的同步

**成功协议: success_sc**

| 字段  | 类型  | 备注  |
|-----|-----|-----|

**错误信息协议: error_sc**

| 字段   | 类型  | 备注   |
|------|-----|------|
| code | int | 错误码  |
| msg  | str | 错误信息 |

**心跳返回: heart_sc**

| 字段   | 类型  | 备注          |
|------|-----|-------------|
| time | int | 服务端时间戳，单位毫秒 |

**房间信息的同步协议: room_info_sc**

| 字段      | 类型         | 备注                                    |
|---------|------------|---------------------------------------|
| name    | str        | 自己的用户名                                |
| rid     | str        | 房间号                                   |
| type    | int        | 房间类别，同上                               |
| host    | str        | 主持人的名字                                |
| names   | Array[str] | 所有选手的用户名，含自己，数组长度就是选手人数。没进入的位置会留个空字符串 |
| started | bool       | 游戏是否开始                                |

**游戏数据的同步协议: game_info_sc**

*根据不同玩法，数据不同，待定*

### 登录相关

**登录协议: login_cs**

| 字段    | 类型  | 备注  |
|-------|-----|-----|
| token | str | 识别码 |

**心跳请求协议: heart_cs**

| 字段  | 类型  | 备注  |
|-----|-----|-----|

### 创建/进入/离开房间协议

**创建房间: create_room_cs**

| 字段   | 类型  | 备注                                 |
|------|-----|------------------------------------|
| name | str | 用户名                                |
| rid  | str | 房间号                                |
| type | int | 房间类别，1：bingo标准赛，2：bingo BP赛，3：大富翁。 |

**进入房间：join_room_cs**

| 字段   | 类型  | 备注  |
|------|-----|-----|
| name | str | 用户名 |
| rid  | str | 房间号 |

**离开房间（房主和玩家通用）: leave_room_cs**

| 字段  | 类型  | 备注  |
|-----|-----|-----|

**修改房间类型（仅房主，比赛开始后不能使用）: update_room_type_cs**

| 字段   | 类型  | 备注        |
|------|-----|-----------|
| type | int | 房间类型，枚举同上 |

**修改自己的名字（必须在房间内，房主和玩家通用）: update_name_cs**

| 字段   | 类型  | 备注    |
|------|-----|-------|
| name | str | 自己的名字 |

**开始比赛（房主才能使用）: start_game_cs**

| 字段        | 类型         | 备注                |
|-----------|------------|-------------------|
| game_time | int        | 比赛时间长度，分钟         |
| countdown | int        | 倒计时时长，秒           |
| games     | Array[str] | 作品代号，和表中所配的相同     |
| ranks     | Array[str] | 符卡难度代号，和表中所配的相同   |
| need_win  | int        | 需要赢几场才算赢，例如bo3就发2 |

**请求符卡数据（例如断线重连后请求）: get_spells_cs**

| 字段  | 类型  | 备注  |
|-----|-----|-----|

**停止比赛（房主才能使用）: stop_game_cs**

| 字段  | 类型  | 备注  |
|-----|-----|-----|

**请求修改一张符卡的status（房主和选手通用，选手只能改自己这边的）: update_spell_cs**

| 字段     | 类型  | 备注                                                             |
|--------|-----|----------------------------------------------------------------|
| idx    | int | 符卡所在格子的下标，从0开始                                                 |
| status | int | 0-变为双方未选择，1-左玩家变为开始打，2-左玩家变为打完，4-右玩家变为开始打，8-右玩家变为打完。不能一次操作两个玩家 |

**通知客户端把一张符卡变为某种status: update_spell_sc**

| 字段     | 类型  | 备注                                               |
|--------|-----|--------------------------------------------------|
| idx    | int | 符卡所在格子的下标，从0开始                                   |
| status | int | 0-未选择，1-左玩家开始打，2-左玩家打完，4-右玩家开始打，5-双方都开始打，8-右玩家打完 |

**重置房间（重置分数）: reset_room_cs**

| 字段  | 类型  | 备注  |
|-----|-----|-----|

**设置换卡计数: change_card_count_cs**

| 字段  | 类型         | 备注      |
|-----|------------|---------|
| cnt | Array[int] | 长度为2的数组 | 

**暂停: pause_cs**

| 字段    | 类型   | 备注                 |
|-------|------|--------------------|
| pause | bool | true-暂停，false-取消暂停 | 

**下一回合: next_round_cs**

| 字段  | 类型  | 备注  |
|-----|-----|-----|

**link模式收卡记时: link_time_cs**

| 字段    | 类型   | 备注                     |
|-------|------|------------------------|
| whose | int  | 0左，1右                  |
| start | bool | true-开始/继续，false-停止/暂停 |

**设置phase: set_phase_cs**

| 字段    | 类型  | 备注  |
|-------|-----|-----|
| phase | int |     |
