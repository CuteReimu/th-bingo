# 协议相关

服务器和客户端的连接纯采用websocket连接。

## 协议的定义

众所周知，一般“服务器-客户端”架构的项目的协议分为三类：客户端向服务器请求、服务器返回、服务器主动向客户端推送。

为了在一个websocket连接中正确区分这三种协议，在借鉴了很多大型项目的方案后，我们规定如下：

**客户端向服务器请求**

```jsonc
{
  "action": "login", // 协议名
  "params": { // params内是该协议的各种参数
    "token": "xxxxx",
    "name": "test01"
  }
  "echo": 123 // echo字段用于唯一标识一次请求，可以是任何类型的数据，服务器将会在调用结果中原样返回。
}
```

**服务器返回**

```jsonc
{
  "code": 0, // 0为成功，其它为失败
  "msg": "ok", // 在code为非0时，表示错误信息
  "data": { // data内是该协议的各种参数，code为0时才有意义
    "rid": "xxxxx"
  }
  "echo": 123 // 等于客户端请求中的echo字段
}
```

**服务器主动推送**

```jsonc
{
  "push_action": "player_sit_down", // 协议名
  "params": { // params内是该协议的各种参数
    "name": "test01"
  }
}
```

## 全部协议

<details><summary>心跳、登录相关</summary>

**心跳请求**

协议类型：websocket

示例：

```jsonc
{
  "name": "heart_cs",
}
```

**心跳返回**

协议类型：websocket

示例：

```jsonc
{
  "name": "heart_sc",
  "data": {
    "now": 12345433342 // 服务器当前时间戳，单位毫秒
  }
}
```

**登录**

协议类型：http

请求：

```jsonc
{
  "name": "login",
  "data": {
    "token": "xxxxx", // 客户端token，服务器是以token来判定唯一的用户
    "name": "test01" // 用户名
  }
}
```

返回：

```jsonc
{
  "code": 0,
  "data": {
    "rid": "test01" // 房间号，如果为null则表示没有房间
  }
}
```

</details> 

<details><summary>房间配置</summary>

**创建房间**

协议类型：http

请求：

```jsonc
{
  "name": "create_room",
  "data": { // 下文很多协议的结构都和这个一样
    "rid": "test01", // 房间名
    "type": 1, // 1-标准赛，2-BP赛，3-link赛
    "solo": false, // 是否为无导播局
    "add_robot": false, // 是否为打机器人局
    "game_time": 30, // 游戏总时间（不含倒计时），单位：分
    "countdown": 5, // 倒计时，单位：秒
    "games": ["6", "7", "8"], // 含有哪些作品
    "ranks": ["L", "EX"], // 含有哪些游戏难度，也就是L卡和EX卡
    "need_win": 2, // 需要胜利的局数，例如2表示bo3
    "difficulty": 1, // 难度（影响不同星级的卡的分布），1对应E，2对应N，3对应L，其它对应随机
    "cd_time": 30, // 选卡cd，收卡后要多少秒才能选下一张卡
    "reserved_type": 1 // 纯客户端用的一个类型字段，服务器只负责透传
  }
}
```

返回：

```jsonc
{
  "code": 0,
  "data": {
    "rid": "10" // 房间名
  }
}
```

**获取房间配置**

协议类型：http

请求：

```jsonc
{
  "name": "get_room_config",
  "data": {
    "rid": "test01" // 房间名
  }
}
```

返回：

```jsonc
{
  "code": 0,
  "data": { // 和create_room结构一样
  }
}
```

**修改房间配置**

协议类型：http

请求：

```jsonc
{
  "name": "update_room_config",
  "data": { // 和create_room结构一样
  }
}
```

返回：

```jsonc
{
  "code": 0
}
```

**推送房间配置更新**

协议类型：websocket

示例：

```jsonc
{
  "name": "update_room_config_sc",
  "data": { // 和create_room结构一样
  }
}
```

</details>

<details><summary>房间状态</summary>

**加入房间**

协议类型：http

请求：

```jsonc
{
  "name": "join_room",
  "data": {
    "rid": "test01" // 房间名
  }
}
```

返回：

```jsonc
{
  "code": 0,
  "data": {
    "rid": "test01", // 房间名
    "type": 1, // 1-标准赛，2-BP赛，3-link赛
    "host": "test00", // 房主的名字
    "names": ["test01", "test02"], // 玩家名字列表，一定有2个，没有人则对应位置为空
    "change_card_count": [1, 2], // 换卡次数，一定有2个，和上面的names一一对应
    "started": false, // 是否已经开始
    "score": [1, 2], // 比分，一定有2个，和上面的names一一对应
    "watchers": ["test03", "test04"] // 观众名字列表，有几个就是几个
  }
}
```

**离开房间**

协议类型：http

请求：

```jsonc
{
  "name": "leave_room"
}
```

返回：

```jsonc
{
  "code": 0
}
```

**观战（站起）**

协议类型：http

请求：

```jsonc
{
  "name": "stand_up"
}
```

返回：

```jsonc
{
  "code": 0
}
```

**作为选手（坐下）**

协议类型：http

请求：

```jsonc
{
  "name": "sit_down"
}
```

返回：

```jsonc
{
  "code": 0
}
```

**获取房间**

协议类型：http

请求：

```jsonc
{
  "name": "get_room"
}
```

返回：

```jsonc
{
  "code": 0,
  "data": { // 和join_room结构一样
  }
}
```

**推送房间状态更新**

*由于实时性要求非常高，考虑到websocket和http是两条信道，可能有并发问题，一律采用推送只推送发生了变化，客户端重新请求获取信息接口的方式*

协议类型：websocket

示例：

```jsonc
{
  "name": "update_room_sc"
}
```

</details>

<details><summary>客户端透传协议</summary>

协议类型：websocket

请求：

```json
{
  "name": "set_phase",
  "data": {
    "phase": 1
  }
}
```

返回：

```json
{
  "code": 0
}
```

请求：

```json
{
  "name": "get_phase"
}
```

返回：

```json
{
  "code": 0,
  "data": {
    "phase": 1
  }
}
```

</details>

<details><summary>游戏相关</summary>

**游戏开始**

协议类型：http

请求：

```jsonc
{
  "name": "start_game"
}
```

返回：

```jsonc
{
  "code": 0
}
```

**推送游戏开始**

协议类型：websocket

示例：

```jsonc
{
  "name": "start_game_sc",
  "data": { // 和create_room结构一样，也就是房间配置，以防同步失败
  }
}
```

**游戏结束**

协议类型：http

请求：

```jsonc
{
  "name": "stop_game",
  "data": {
    "winner": -1 // -1表示平局，0表示左边，1表示右边
  }
}
```

返回：

```jsonc
{
  "code": 0
}
```

**推送游戏结束**

协议类型：websocket

示例：

```jsonc
{
  "name": "stop_game_sc",
  "data": {
    "winner": -1 // -1表示平局，0表示左边，1表示右边
  }
}
```

**警告玩家**

协议类型：http

请求：

```jsonc
{
  "name": "gm_warn_player",
  "data": {
    "name": "test01" // 玩家名
  }
}
```

返回：

```jsonc
{
  "code": 0
}
```

**推送警告玩家**

协议类型：websocket

示例：

```jsonc
{
  "name": "gm_warn_player_sc",
  "data": {
    "name": "test01" // 玩家名
  }
}
```

**获取所有符卡**

协议类型：http

请求：

```jsonc
{
  "name": "get_all_spells"
}
```

返回：

```jsonc
{
  "code": 0,
  "data": {
    "spells": [
      {
        "index": 1, // 符卡唯一ID
        "game": "6", // 作品
        "name": "", // 符卡名
        "rank": "L", // 难度
        "star": 3, // 星级
        "desc": "", // 符卡描述
        "id": 1, // 在对应作品里的id
        "fastest": 1.0, // AI参数
        "one": 1.0, // AI参数
        "two": 1.0, // AI参数
        "three": 1.0, // AI参数
        "final": 1.0, // AI参数
        "bonus_rate": 1.0 // AI参数
      }, // 有25个符卡
    ],
    "left_time": 1, // 倒计时剩余时间，单位：毫秒
    "status": 1, // 0-未开始，1-赛前倒计时中，2-开始，3-暂停中，4-结束
    "left_cd_time": 1 // 选卡cd剩余时间，单位：毫秒
  }
}
```

**选卡**

协议类型：http

请求：

```jsonc
{
  "name": "select_spell",
  "data": {
    "index": 1 // 第几张卡，0-24
  }
}
```

返回：

```jsonc
{
  "code": 0
}
```

**收卡**

协议类型：http

请求：

```jsonc
{
  "name": "finish_spell",
  "data": {
    "index": 1 // 第几张卡，0-24
  }
}
```

返回：

```jsonc
{
  "code": 0
}
```

**房主修改卡**

协议类型：http

请求：

```jsonc
{
  "name": "update_spell_status",
  "data": {}
}
```

返回：

```jsonc
{
  "code": 0
}
```

**推送符卡状态**

*收到获取所有符卡协议之后一定会推送一个*

协议类型：websocket

示例：

```jsonc
{
  "name": "update_spell_status_sc",
  "data": {
    "spells": [ // 有可能一次推送多个
      {
        "index": 1, // 第几张，0-24
        "status": [1, 2] // 0-未选，1-已选，2-已收，分别左右两人
      }
    ],
    "causer": "test01" // 造成这个状态变化的玩家（可能是房主）（如果是获取所有符卡后的全量推送，则这个字段为空）
  }
}
```

</details>