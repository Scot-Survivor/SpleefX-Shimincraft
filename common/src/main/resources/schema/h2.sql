--: create table
create table if not exists `SpleefXData`
(
    `PlayerUUID`              varchar(36) not null unique,
    `Coins`                   int         not null default 40,
    `SpleggUpgrade`           text        not null,
    `PurchasedSpleggUpgrades` text        not null,
    `GlobalStats`             text        not null,
    `ExtensionStats`          text        not null,
    primary key (`PlayerUUID`)
);

--: select player
select *
from "SpleefXData"
where "PlayerUUID" = ?;

--: delete player
DELETE
from `SpleefXData`
where "PlayerUUID" = ?;

--: select all
select *
from `SpleefXData`;

--: upsert player
MERGE into `SpleefXData` key (`PlayerUUID`) values
%s;