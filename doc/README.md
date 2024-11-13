# 协议相关

## 协议的定义

协议分为两类：**http协议**和**websocket协议**。

所有的请求类都通过**http协议**，心跳和推送类通过**websocket协议**。

### http协议

http协议全部采用POST请求，请求体和回包都采用json格式。

请求：

```jsonc
{
  "name": "login", // 协议名称，不含"_cs"或"_sc"
  "data": { // 协议内容，下文一一列举（如果协议体为空，则没有这个字段，以便减小协议大小）
    "name": "test01"
  }
}
```

返回：

```jsonc
{
  "code": 0,
  "msg": "ok", // 如果code不为0，则为错误信息，否则没有这个字段
  "data": { // 协议内容，下文一一列举（如果协议体为空，则没有这个字段，以便减小协议大小）
    "token": "xxxxx"
  }
}
```

### websocket协议

也采用json格式，只有心跳和推送类两种，客户端to服务器和服务器to客户端的协议格式一致：

```jsonc
{
  "name": "heart_sc", // 协议名称，含"_sc"和"_cs"
  "data": {
    "now": 12345433342
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
  "data": { // 很多协议的结构都和这个一样
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
  "name": "update_room",
  "data": { // 和create_room结构一样
  }
}
```

返回：

```jsonc
{
  "code": 0,
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
