# 协议相关

服务器和客户端的连接纯采用websocket连接。

## 协议的定义

众所周知，一般“服务器-客户端”架构的项目的协议分为三类：客户端向服务器请求、服务器返回、服务器主动向客户端推送。

为了在一个websocket连接中正确区分这三种协议，在借鉴了很多大型项目的方案后，我们规定如下：

**客户端向服务器请求**

```jsonc
{
  "action": "login", // 协议名
  "data": { // data内是该协议的各种参数（也就是下文中的“请求参数”）
    "name": "test01",
    "pwd": "xxxxx"
  },
  "echo": 123 // echo字段用于唯一标识一次请求，可以是任何类型的数据，服务器将会在调用结果中原样返回。
}
```

**服务器返回**

```jsonc
{
  "code": 0, // 0为成功，其它为失败
  "msg": "ok", // 在code为非0时，表示错误信息
  "data": { // 在code为0时，data内是该协议的各种参数（也就是下文中的“返回参数”）
    "rid": "xxxxx"
  },
  "echo": 123 // 等于客户端请求中的echo字段
}
```

**服务器主动推送**

```jsonc
{
  "push_action": "player_sit_down", // 协议名
  "data": { // data内是该协议的各种参数
    "name": "test01"
  }
}
```

## 全部协议

<details><summary>心跳、登录相关</summary>

**心跳请求**

action名：`heart`

请求参数：`null`

返回参数：

```jsonc
{
  "now": 12345433342 // 服务器当前时间戳，单位毫秒
}
```

---

**登录**

action名：`login`

请求参数：

```jsonc
{
  "name": "test01", // 用户名
  "pwd": "xxx" // 密码
}
```

返回参数：

```jsonc
{
  "rid": "test01" // 房间号，如果为null则表示没有房间
}
```

---

**推送被顶号**

push_action名：`push_kick`

参数：`null`

</details> 

<details><summary>房间配置</summary>

**创建房间**

action名：`create_room`

请求参数：

```jsonc
{
  "room_config": { // 下文很多协议的结构都和这个一样
    "rid": "test01", // 房间名
    "type": 1, // 1-标准赛，2-BP赛，3-link赛
    "game_time": 30, // 游戏总时间（不含倒计时），单位：分
    "countdown": 5, // 倒计时，单位：秒
    "games": ["6", "7", "8"], // 含有哪些作品
    "ranks": ["L", "EX"], // 含有哪些游戏难度，也就是L卡和EX卡
    "need_win": 2, // 需要胜利的局数，例如2表示bo3
    "difficulty": 1, // 难度（影响不同星级的卡的分布），1对应E，2对应N，3对应L，其它对应随机
    "cd_time": 30, // 选卡cd，收卡后要多少秒才能选下一张卡
    "reserved_type": 1 // 是否为团体赛
  },
  "solo": true, // 是否为无房主模式
  "add_robot": true // 是否为单人练习模式
}
```

返回参数：

```jsonc
{
  "rid": "test01" // 房间名
}
```

---

**获取房间配置**

action名：`get_room_config`

请求参数：

```jsonc
{
  "rid": "test01" // 房间名
}
```

返回参数：和`create_room`的请求参数中的`room_config`一样

---

**修改房间配置**

action名：`update_room_config`

请求参数：和`create_room`的请求参数中的`room_config`一样

**请求参数中，`rid`字段必须有，其它每个字段都允许是空值。如果为空，意思是这个字段不修改**

返回参数：`null`

---

**推送房间配置更新**

push_action名：`push_update_room_config`

参数：和`create_room`的请求参数中的`room_config`一样

</details>

<details><summary>房间状态</summary>

**加入房间**

action名：`join_room`

请求参数：

```jsonc
{
  "rid": "test01" // 房间名
}
```

返回参数：

```jsonc
{
  "rid": "test01", // 房间名
  "type": 1, // 1-标准赛，2-BP赛，3-link赛
  "host": "test00", // 房主的名字
  "names": ["test01", "test02"], // 玩家名字列表，一定有2个，没有人则对应位置为空
  "change_card_count": [1, 2], // 换卡次数，一定有2个，和上面的names一一对应
  "started": false, // 是否已经开始
  "score": [1, 2], // 比分，一定有2个，和上面的names一一对应
  "watchers": ["test03", "test04"], // 观众名字列表，有几个就是几个
  "last_winner": 1, // 上一场是谁赢，0或1，-1表示没有上一场
  "ban_pick": {} // 赛前BP的相关数据，同push_ban_pick协议的参数，如果不是赛前BP则为null
}
```

---

**获取房间**

action名：`get_room`

请求参数：`null`

返回参数：和`join_room`的返回参数结构一样

---

**离开房间**

action名：`leave_room`

请求参数：`null`

返回参数：`null`

---

**观战（站起）**

action名：`stand_up`

请求参数：`null`

返回参数：`null`

---

**作为选手（坐下）**

action名：`sit_down`

请求参数：`null`

返回参数：`null`

---

**推送加入房间**

push_action名：`push_join_room`

参数：

```jsonc
{
  "name": "xxx", // 加入房间的玩家名
  "position": 0 // 0：左边玩家，1：右边玩家，-1：观众
}
```

---

**推送离开房间**

push_action名：`push_leave_room`

参数：

```jsonc
{
  "name": "xxx" // 离开房间的玩家名，如果是自己，表示自己被踢出房间
}
```

---

**推送观战（站起）**

push_action名：`push_stand_up`

参数：

```jsonc
{
  "name": "xxx" // 站起的玩家名
}
```

---

**推送作为选手（坐下）**

push_action名：`push_sit_down`

参数：

```jsonc
{
  "name": "xxx", // 坐下的玩家名
  "position": 1 // 位置，左0右1
}
```

</details>

<details><summary>客户端透传协议</summary>

action名：`set_phase`

请求参数：

```json
{
  "phase": 1
}
```

返回参数：`null`

---

action名：`get_phase`

请求参数：`null`

返回参数：
```json
{
  "phase": 1
}
```

</details>

<details><summary>游戏相关</summary>

**请求开始游戏**

action名：`start_game`

请求参数：`null`

返回参数：`null`

---

**推送游戏开始**

push_action名：`push_start_game`

参数：和`create_room`的请求参数中的`room_config`一样，目的是同步一下房间配置，以防之前同步失败

---

**请求结束游戏**

action名：`stop_game`

请求参数：

```jsonc
{
  "winner": -1 // -1表示平局，0表示左边，1表示右边
}
```

返回参数：`null`

---

**推送游戏结束**

push_action名：`push_stop_game`

参数：

```jsonc
{
  "winner": -1 // -1表示平局，0表示左边，1表示右边
}
```

---

**重置房间**

action名：`reset_room`

请求参数：`null`

返回参数：`null`

---

**推送重置房间**

push_action名：`push_reset_room`

参数：`null`

---

**警告玩家**

action名：`gm_warn_player`

请求参数：

```jsonc
{
  "name": "test01" // 玩家名
}
```

返回参数：`null`

---

**推送警告玩家**

push_action名：`push_gm_warn_player`

参数：`null`

---

**修改换卡次数**

action名：`update_change_card_count`

请求参数：

```jsonc
{
  "name": "test01", // 玩家名
  "count": 2 // 新次数
}
```

返回参数：`null`

---

**推送修改换卡次数**

push_action名：`push_update_change_card_count`

参数：

```jsonc
{
  "name": "test01", // 玩家名
  "count": 2 // 新次数
}
```

---

**获取所有符卡**

action名：`get_all_spells`

请求参数：`null`

返回参数：

```jsonc
{
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
    },
    //...有25个符卡
  ],
  "spell_status": [1, 0, 1], // 25张符卡的收取状态
  "left_time": 1, // 倒计时剩余时间，单位：毫秒
  "status": 1, // 0-未开始，1-赛前倒计时中，2-开始，3-暂停中，4-结束
  "left_cd_time": 1, // 选卡cd剩余时间，单位：毫秒
  "bp_data": { // BP赛相关数据，如果不是BP赛则为null
    "whose_turn": 1, // 轮到谁了，0-左边，1-右边
    "ban_pick": 1, // 0-选，1-ban，2-轮到收卡了
    "spell_failed_count_a": [1, 2, 3], // 左边玩家25张符卡的失败次数
    "spell_failed_count_b": [1, 2, 3] // 右边玩家25张符卡的失败次数
  },
  "link_data": {} // BP赛相关数据，同push_link_data协议的参数，如果不是BP赛则为null
}
```

---

**房主暂停**

action名：`pause`

请求参数：

```jsonc
{
  "pause": true // true为暂停，false为取消暂停
}
```

返回参数：`null`

---

**推送暂停**

push_action名：`push_pause`

参数：

```jsonc
{
  "pause": true // true为暂停，false为取消暂停
}
```

---

**设置调试用符卡**

action名：`set_debug_spells`

请求参数：

```jsonc
{
  "spells": [1, 2, 3] // 25张符卡的符卡唯一id，null为取消设置调试用符卡
}
```

返回：`null`

---

**选卡**

action名：`select_spell`

请求参数：

```jsonc
{
  "index": 1 // 第几张卡，0-24
}
```

返回参数：`null`

---

**收卡**

action名：`finish_spell`

请求参数：

```jsonc
{
  "name": "finish_spell",
  "data": {
    "index": 1, // 第几张卡，0-24
    "success": true, // 是否成功，true为成功，false为失败，不填为成功，BP赛中必填此字段
    "player_index": 1 // 0-左边玩家，1-右边玩家，link赛中如果你是房主则必填此字段
  }
}
```

返回参数：`null`（注意即使是发起这条协议的玩家，也还会额外收到一条下文中的 `push_update_spell_status` 协议）

---

**房主修改卡（或者控制机器人修改卡）**

action名：`update_spell_status`

请求参数：

```jsonc
{
  "index": 1, // 第几张，0-24
  "status": 1 // 状态
}
```

参数：`null`

---

**推送符卡状态**

push_action名：`push_update_spell_status`

示例：

```jsonc
{
  "index": 1, // 第几张，0-24
  "status": 1, // 状态
  "causer": "test01", // 造成这个状态变化的玩家
  "spell_failed_count_a": 1, // 左边玩家的失败次数，只有BP赛时才有此字段
  "spell_failed_count_b": 1 // 右边玩家的失败次数，只有BP赛时才有次字段
}
```

</details>

<details><summary>BP赛相关</summary>

**选手进行BP**

action名：`bp_game_ban_pick`

请求参数：

```jsonc
{
  "idx": 1 // 格子序号，0-24
}
```

返回参数：`null`

---

**房主操控BP赛进入下一回合**

action名：`bp_game_next_round`

请求参数：`null`

返回参数：`null`

---

**推送BP赛进入下一回合**

push_action名：`push_bp_game_next_round`

参数：

```jsonc
{
  "whose_turn": 1, // 轮到谁了，0-左边，1-右边
  "ban_pick": 1 // 0-选，1-ban，2-轮到收卡了
}
```

返回参数：`null`

</details>

<details><summary>Link赛相关</summary>

**选手取消选卡**

action名：`cancel_select_spell`

请求参数：

```jsonc
{
  "index": 1 // 第几张卡，0-24
}
```

返回参数：`null`

---

**Link赛计时**

action名：`link_time`

请求参数：

```jsonc
{
  "whose": 1,
  "event": 1
}
```

返回参数：`null`

---

**推送Link赛信息**

push_action名：`push_link_data`

参数：

```jsonc
{
  "link_idx_a": [1, 2, 3], // 左边玩家的连线
  "link_idx_b": [1, 2, 3], // 右边玩家的连线
  "start_ms_a": 1,
  "end_ms_a": 2,
  "event_a": 1,
  "start_ms_b": 1,
  "end_ms_b": 2,
  "event_b": 1
}
```

</details>

<details><summary>赛前BP相关</summary>

**开始BP**

action名：`start_ban_pick`

请求参数：`null`

返回参数：`null`

---

**选手进行BP**

action名：`ban_pick`

请求参数：

```jsonc
{
  "selection": "6" // 选择的作品
}
```

返回参数：`null`

---

**推送BP状态**

push_action名：`push_ban_pick`

参数：[其中“BP状态”枚举参考代码注释](../src/main/kotlin/BanPick.kt)

```jsonc
{
  "who_first": 1, // 谁是第一个操作的，0-左边，1-右边
  "phase": 1, // BP状态
  "a_pick": ["6", "7"], // 左玩家保了哪些作品
  "a_ban": ["8", "9"], // 左玩家ban了哪些作品
  "b_pick": ["10", "11"], // 右玩家保了哪些作品
  "b_ban": ["12", "13"], // 右玩家ban了哪些作品
  "a_open_ex": 1, // 左玩家是否选EX难度
  "b_open_ex": 1 // 右玩家是否选EX难度
}
```

</details>

<details><summary>符卡状态枚举</summary>

| 枚举值 |  含义   |
|:---:|:-----:|
| -1  | 被ban  |
|  0  |   无   |
|  1  | 左玩家选了 |
|  2  | 双方都选了 |
|  3  | 右玩家选了 |
|  5  | 左玩家收了 |
|  6  | 双方都收了 |
|  7  | 右玩家收了 |

</details>
