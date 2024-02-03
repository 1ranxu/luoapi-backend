# 数据库初始化
-- 创建库
create database if not exists luoapi;

-- 切换库
use luoapi;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userName     varchar(256)                           null comment '用户昵称',
    userAccount  varchar(256)                           not null comment '账号',
    userAvatar   varchar(1024)                          null comment '用户头像',
    email        varchar(256)                           null comment '邮箱',
    gender       tinyint                                null comment '性别 0-男 1-女',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    userPassword varchar(512)                           not null comment '密码',
    userProfile  varchar(512)                           null comment '用户简介',
    accessKey    varchar(512)                           not null comment 'accessKey',
    secretKey    varchar(512)                           not null comment 'secretKey',
    score        bigint       default 30                not null comment '积分余额,注册送30积分',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除'
) comment '用户' collate = utf8mb4_unicode_ci;

-- 每日签到表
create table if not exists sign_in
(
    id          bigint auto_increment comment 'id' primary key,
    userId      bigint                             not null comment '签到人',
    description varchar(256)                       null comment '描述',
    addScores   bigint   default 10                not null comment '签到增加积分个数',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '每日签到表';

-- 接口信息表
create table interface_info
(
    id             bigint auto_increment comment '主键'
        primary key,
    name           varchar(256)                           not null comment '接口名称',
    url            varchar(512)                           not null comment '接口地址',
    userId         bigint                                 not null comment '创建人',
    method         varchar(256)                           not null comment '请求方法',
    requestParams  text                                   null comment '接口请求参数',
    responseParams text                                   null comment '接口响应参数',
    reduceScore    bigint       default 0                 null comment '扣减积分数',
    requestExample text                                   null comment '请求示例',
    requestHeader  text                                   null comment '请求头',
    responseHeader text                                   null comment '响应头',
    returnFormat   varchar(512) default 'JSON'            null comment '返回格式(JSON等等)',
    description    varchar(1024)                          null comment '接口描述',
    status         tinyint      default 0                 not null comment '接口状态 0-关闭 1-开启',
    totalInvokes   bigint       default 0                 not null comment '接口总调用次数',
    avatarUrl      varchar(1024)                          null comment '接口头像',
    createTime     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint      default 0                 not null comment '逻辑删除'
) comment '接口信息';

-- 用户接口调用表
create table user_interface_info
(
    id              bigint auto_increment comment '主键'
        primary key,
    userId          bigint                             not null comment '调用者Id',
    interfaceInfoId bigint                             not null comment '接口id',
    totalInvokes    bigint   default 0                 not null comment '总调用次数',
    status          tinyint  default 1                 not null comment '调用状态 0-限制 1-正常',
    createTime      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    idDelete        tinyint  default 0                 not null comment '逻辑删除'

) comment '用户接口调用表';

insert into luoapi.interface_info(`id`, `name`, `url`, `userId`, `method`, `requestParams`, `reduceScore`,
                                  `requestExample`,
                                  `requestHeader`, `responseHeader`, `description`, `status`, `totalInvokes`,
                                  `avatarUrl`,
                                  `returnFormat`, `responseParams`)
values (1705234447153963010, '随机毒鸡汤', 'http://gateway.luoapi.icu/api/poisonousChickenSoup', 1709135865515171844, 'GET',
        NULL, 1, 'http://gateway.luoapi.icu/api/poisonousChickenSoup', NULL, NULL, '随机毒鸡汤', 1, 228, '', 'JSON',
        '[{\"fieldName\":\"code\",\"type\":\"int\",\"desc\":\"响应码\"},
          {\"fieldName\":\"data.text\",\"type\":\"string\",\"desc\":\"随机毒鸡汤\"},
          {\"fieldName\":\"message\",\"type\":\"string\",\"desc\":\"响应描述\"}]'),

       (1705237104270712833, '获取输入的名称', 'http://gateway.luoapi.icu/api/name', 1709135865515171844, 'GET',
        '[{\"fieldName\":\"name\",\"type\":\"string\",\"desc\":\"输入的名称\",\"required\":\"是\"}]',
        1, 'http://gateway.luoapi.icu/api/name?name=zhangshan', NULL, NULL, '获取输入的名称', 1, 36,
        'http://img.luoying.icu/interface_avatar/1699981437456797697/XqT3Nsto-psc.jfif', 'JSON',
        '[{\"fieldName\":\"data.name\",\"type\":\"object\",\"desc\":\"输入的参数\"},
          {\"fieldName\":\"code\",\"type\":\"int\",\"desc\":\"响应码\"},
          {\"fieldName\":\"message\",\"type\":\"string\",\"desc\":\"响应信息描述\"}]'),

       (1705237990061580289, '随机壁纸', 'http://gateway.luoapi.icu/api/randomWallpaper', 1709135865515171844, 'GET',
        '[{\"fieldName\":\"method\",\"type\":\"string\",\"desc\":\"输出壁纸端[mobile|pc|zsy]默认为pc\",\"required\":\"否\"},
          {\"fieldName\":\"lx\",\"type\":\"string\",\"desc\":\"选择输出分类[meizi|dongman|fengjing|suiji]，为空随机输出\",\"required\":\"否\"}]',
        1, 'http://gateway.luoapi.icu/api/randomWallpaper?lx=dongman', NULL, NULL, '获取随机壁纸', 1, 97,
        'http://img.luoying.icu/typory/logo.jpg', 'JSON',
        '[{\"fieldName\":\"code\",\"type\":\"string\",\"desc\":\"响应码\"},
          {\"fieldName\":\"data.imgurl\",\"type\":\"string\",\"desc\":\"返回的壁纸地址\"},
          {\"fieldName\":\"message\",\"type\":\"string\",\"desc\":\"响应消息\"}]'),

       (1705238841173942274, '每日星座运势', 'http://gateway.luoapi.icu/api/horoscope', 1709135865515171844, 'GET',
        '[{\"fieldName\":\"type\",\"type\":\"string\",\"desc\":\"十二星座对应英文小写，aries, taurus, gemini, cancer, leo, virgo, libra, scorpio, sagittarius, capricorn, aquarius, pisces\",\"required\":\"是\"},
          {\"fieldName\":\"time\",\"type\":\"string\",\"desc\":\"今日明日一周等运势,today, nextday, week, month, year, love\",\"required\":\"是\"}]',
        1, 'http://gateway.luoapi.icu/api/horoscope?type=scorpio&time=nextday', NULL, NULL, '获取每日星座运势', 1, 23,
        'http://img.luoying.icu/interface_avatar/1709135865515171844/r2X9jsoT-horoscope2.png', 'JSON',
        '[{\"fieldName\":\"code\",\"type\":\"int\",\"desc\":\"响应码\"},
          {\"fieldName\":\"data.todo.yi\",\"type\":\"string\",\"desc\":\"宜做\"},
          {\"fieldName\":\"data.todo.ji\",\"type\":\"string\",\"desc\":\"忌做\"},
          {\"fieldName\":\"data.fortunetext.all\",\"type\":\"string\",\"desc\":\"整体运势\"},
          {\"fieldName\":\"data.fortunetext.love\",\"type\":\"string\",\"desc\":\"爱情运势\"},
          {\"fieldName\":\"data.fortunetext.work\",\"type\":\"string\",\"desc\":\"工作运势\"},
          {\"fieldName\":\"data.fortunetext.money\",\"type\":\"string\",\"desc\":\"财运运势\"},
          {\"fieldName\":\"data.fortunetext.health\",\"type\":\"string\",\"desc\":\"健康运势\"},
          {\"fieldName\":\"data.fortune.all\",\"type\":\"int\",\"desc\":\"整体运势评分\"},
          {\"fieldName\":\"data.fortune.love\",\"type\":\"int\",\"desc\":\"爱情运势评分\"},
          {\"fieldName\":\"data.fortune.work\",\"type\":\"int\",\"desc\":\"工作运势评分\"},
          {\"fieldName\":\"data.fortune.money\",\"type\":\"int\",\"desc\":\"财运运势评分\"},
          {\"fieldName\":\"data.fortune.health\",\"type\":\"int\",\"desc\":\"健康运势评分\"},
          {\"fieldName\":\"data.shortcomment\",\"type\":\"string\",\"desc\":\"简评\"},
          {\"fieldName\":\"data.luckycolor\",\"type\":\"string\",\"desc\":\"幸运颜色\"},
          {\"fieldName\":\"data.index.all\",\"type\":\"string\",\"desc\":\"整体指数\"},
          {\"fieldName\":\"data.index.love\",\"type\":\"string\",\"desc\":\"爱情指数\"},
          {\"fieldName\":\"data.index.work\",\"type\":\"string\",\"desc\":\"工作指数\"},
          {\"fieldName\":\"data.index.money\",\"type\":\"string\",\"desc\":\"财运指数\"},
          {\"fieldName\":\"data.index.health\",\"type\":\"string\",\"desc\":\"健康指数\"},
          {\"fieldName\":\"data.luckynumber\",\"type\":\"string\",\"desc\":\"幸运数字\"},
          {\"fieldName\":\"data.time\",\"type\":\"string\",\"desc\":\"日期\"},
          {\"fieldName\":\"data.title\",\"type\":\"string\",\"desc\":\"星座名称\"},
          {\"fieldName\":\"data.type\",\"type\":\"string\",\"desc\":\"运势类型\"},
          {\"fieldName\":\"data.luckyconstellation\",\"type\":\"string\",\"desc\":\"幸运星座\"},
          {\"fieldName\":\"message\",\"type\":\"string\",\"desc\":\"响应描述\"}]'),

       (1705239469589733378, '随机土味情话', 'http://gateway.luoapi.icu/api/loveTalk', 1709135865515171844, 'GET', NULL, 1,
        'http://gateway.luoapi.icu/api/loveTalk', NULL, NULL, '获取土味情话', 1, 413,
        'http://img.luoying.icu/interface_avatar/1709135865515171844/g8FTal0P-love.png', 'JSON',
        '[{\"fieldName\":\"code\",\"type\":\"int\",\"desc\":\"响应码\"},
          {\"fieldName\":\"data.value\",\"type\":\"string\",\"desc\":\"随机土味情话\"},
          {\"fieldName\":\"message\",\"type\":\"string\",\"desc\":\"返回信息描述\"}]'),

       (1705239928861827073, '获取IP信息归属地', 'http://gateway.luoapi.icu/api/ipInfo', 1709135865515171844, 'GET',
        '[{\"fieldName\":\"ip\",\"type\":\"string\",\"desc\":\"输入IP地址\",\"required\":\"是\"}]',
        1, 'http://gateway.luoapi.icu/api/ipInfo?ip=58.154.0.0', NULL, NULL, '获取IP信息归属地详细版', 1, 59,
        'http://img.luoying.icu/interface_avatar/1709135865515171844/6DPoYYZe-ipInfo.png', 'JSON',
        '[{\"fieldName\":\"code\",\"type\":\"int\",\"desc\":\"响应码\"},
          {\"fieldName\":\"data.ip\",\"type\":\"string\",\"desc\":\"IP地址\"},
          {\"fieldName\":\"data.info.country\",\"type\":\"string\",\"desc\":\"国家\"},
          {\"fieldName\":\"data.info.prov\",\"type\":\"string\",\"desc\":\"省份\"},
          {\"fieldName\":\"data.info.city\",\"type\":\"string\",\"desc\":\"城市\"},
          {\"fieldName\":\"data.info.lsp\",\"type\":\"string\",\"desc\":\"运营商\"},
          {\"fieldName\":\"message\",\"type\":\"string\",\"desc\":\"响应描述\"}]'),

       (1705240565347459073, '获取天气信息', 'http://gateway.luoapi.icu/api/weather', 1709135865515171844, 'GET',
        '[{\"fieldName\":\"city\",\"type\":\"string\",\"desc\":\"输入城市或县区\",\"required\":\"否\"},
          {\"fieldName\":\"ip\",\"type\":\"string\",\"desc\":\"输入IP\",\"required\":\"否\"},
          {\"fieldName\":\"type\",\"type\":\"string\",\"desc\":\"默认一天，可配置 week获取周\",\"required\":\"否\"}]',
        1, 'http://gateway.luoapi.icu/api/weather?ip=180.149.130.16', NULL, NULL, '获取每日每周的天气信息', 1, 109,
        'http://img.luoying.icu/interface_avatar/1709135865515171844/gYNay1Y0-weather.png', 'JSON',
        '[{\"fieldName\":\"code\",\"type\":\"int\",\"desc\":\"响应码\"},
          {\"fieldName\":\"data.city\",\"type\":\"string\",\"desc\":\"城市\"},
          {\"fieldName\":\"data.info.date\",\"type\":\"string\",\"desc\":\"日期\"},
          {\"fieldName\":\"data.info.week\",\"type\":\"string\",\"desc\":\"星期几\"},
          {\"fieldName\":\"data.info.type\",\"type\":\"string\",\"desc\":\"天气类型\"},
          {\"fieldName\":\"data.info.low\",\"type\":\"string\",\"desc\":\"最低温度\"},
          {\"fieldName\":\"data.info.high\",\"type\":\"string\",\"desc\":\"最高温度\"},
          {\"fieldName\":\"data.info.fengxiang\",\"type\":\"string\",\"desc\":\"风向\"},
          {\"fieldName\":\"data.info.fengli\",\"type\":\"string\",\"desc\":\"风力\"},
          {\"fieldName\":\"data.info.night.type\",\"type\":\"string\",\"desc\":\"夜间天气类型\"},
          {\"fieldName\":\"data.info.night.fengxiang\",\"type\":\"string\",\"desc\":\"夜间风向\"},
          {\"fieldName\":\"data.info.night.fengli\",\"type\":\"string\",\"desc\":\"夜间风力\"},
          {\"fieldName\":\"data.info.air.aqi\",\"type\":\"int\",\"desc\":\"空气质量指数\"},
          {\"fieldName\":\"data.info.air.aqi_level\",\"type\":\"int\",\"desc\":\"空气质量指数级别\"},
          {\"fieldName\":\"data.info.air.aqi_name\",\"type\":\"string\",\"desc\":\"空气质量指数名称\"},
          {\"fieldName\":\"data.info.air.co\",\"type\":\"string\",\"desc\":\"一氧化碳浓度\"},
          {\"fieldName\":\"data.info.air.no2\",\"type\":\"string\",\"desc\":\"二氧化氮浓度\"},
          {\"fieldName\":\"data.info.air.o3\",\"type\":\"string\",\"desc\":\"臭氧浓度\"},
          {\"fieldName\":\"data.info.air.pm10\",\"type\":\"string\",\"desc\":\"PM10浓度\"},
          {\"fieldName\":\"data.info.air.pm25\",\"type\":\"string\",\"desc\":\"PM2.5浓度\"},
          {\"fieldName\":\"data.info.air.so2\",\"type\":\"string\",\"desc\":\"二氧化硫浓度\"},
          {\"fieldName\":\"data.info.tip\",\"type\":\"string\",\"desc\":\"提示信息\"},
          {\"fieldName\":\"message\",\"type\":\"string\",\"desc\":\"响应描述\"}\n]');




