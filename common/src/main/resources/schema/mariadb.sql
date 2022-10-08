-- create table
create table if not exists `SpleefXData`
(
    `PlayerUUID`              varchar(36) not null primary key unique,
    `Coins`                   int         not null default 40,
    `SpleggUpgrade`           text        not null,
    `PurchasedSpleggUpgrades` text        not null,
    `GlobalStats`             text        not null,
    `ExtensionStats`          text        not null
);

-- select player
select *
from `SpleefXData`
where PlayerUUID = ?;

-- upsert player
replace into `SpleefXData`(PlayerUUID, Coins, SpleggUpgrade, PurchasedSpleggUpgrades, GlobalStats,
                           ExtensionStats)
values
%s;

-- delete player
DELETE
from `SpleefXData`
where PlayerUUID = ?;

-- select all
select *
from `SpleefXData`;