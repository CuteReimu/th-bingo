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

### 心跳、登录相关

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
  "name": "login_cs",
  "data": {
    "name": "test01"
  }
}
```

返回：

```jsonc
{
  "code": 0,
  "data": {
    "room_id": 10 // 房间号，如果为0则表示没有房间
  }
}
```
