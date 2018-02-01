## API gateway

使用到的框架和组建：

netty+spring+redis

系统分成两部分：

1. 线上接受web请求，并负载转发的服务。
2. 线下同步psql和redis的脚本，同步转发服务配置和资源权限等信息。[todo]

notice： `严重依赖redis的可用性，失败时候，不会去psql中获取。`

运行方式：

mvn clean install [package]

java -classpath gateway-servers.jar -Dport=9900 com.fivebit.App

### 性能
裸框架的TPS：

ab -n10000 -c100 -k http://127.0.0.1:9900/

最高能达到3K的TPS。

只增加了token认证，
ab -n10000 -c50 -k -H "token:72320e52-e65d-4d6c-8018-9399ed949afa" http://127.0.0.1:9900/notice-api/ping/567
达到3百的TPS。

如果是添加了逻辑，取决于网络，后端服务的的性能。

和原来接口性能相比：

会多增加30ms左右的转发时间。

### 功能
1. 通过uri的前缀，寻找后端服务的列表。通过随机方法，寻找权重大于0的服务。后期可以通过weight进行负载升级。
2. 支持GET PUT POST DELETE  的http restful 协议透明使用。其它协议不支持。
3. 默认对所有请求进行token认证。通过配置，可以某个uri或者已*结尾的某类uri关闭token认证。
4. 上线服务和下线服务，通过redis实现实时上线下服务。
5. 出口入口统一log。
6. 数据cache。目前只有用户信息添加了cache。

* todo

1. 用户权限请求url。估计会是10分钟同步一次。
2. 监控请求次数。是按照jupiter-api这样的粒度来统计。



### redis 里面存储的key
后端服务地址负载配置：

	gateway::servers:loadbalance:notice-api  ==> [{"host":"http://192.168.1.2","port":8095,"weight":0},{"host":"http://192.168.1.2","port":8095,"weight":3}]

后端所有的服务列表：

	gateway::servers:all  ==>  [jupiter-v1,mars-v1,notice-api,ucenter]

验证白名单

	gateway::auth:white_list:/ucenter/login  ==> 1
	gateway::auth:white_list:reg_list   => ['/ucenter/log*','xxx*']
IP 黑名单：

	gateway::filters:ip_black:xxx    ==> 1(访问的次数)
#### 示例

GET

	GET http://127.0.0.1:6789/notice-api/ping/
	headers: token:72320e52-e65d-4d6c-8018-9399ed949afa
	return :
	{
    "code": "0",
    "data": {},
    "status": 200
	}
	http://127.0.0.1:6789/chain-api/token/72320e52-e65d-4d6c-8018-9399ed949afa
	headers: token:72320e52-e65d-4d6c-8018-9399ed949afa
	{
    "code": "0",
    "data": {
        "expiresIn": 1800,
        "updateTime": "2017-07-06 16:06:03",
        "userId": "latetime-api-v1"
    },
    "status": 200
	}

POST

	POST http://127.0.0.1:6789/notice-api/ping/
	headers: token:72320e52-e65d-4d6c-8018-9399ed949afa
	return :
	{
    "code": "0",
    "data": {},
    "status": 200
	}
 
PUT
	
	PUT http://127.0.0.1:6789/notice-api/ping/111
	headers: token:72320e52-e65d-4d6c-8018-9399ed949afa
	return :
	{
    "code": "0",
    "data": {},
    "status": 200
	}
DELETE

	DELETE http://127.0.0.1:6789/notice-api/ping/11
	headers: token:72320e52-e65d-4d6c-8018-9399ed949afa
	return :{
	    "code": "0",
	    "data": 111,
	    "status": 200
		}
