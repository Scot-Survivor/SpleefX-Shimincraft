--: create table
create table if not exists "SpleefXData"
(
    "PlayerUUID"              varchar(36) not null primary key,
    "Coins"                   int         not null default 40,
    "SpleggUpgrade"           text        not null,
    "PurchasedSpleggUpgrades" text        not null,
    "GlobalStats"             text        not null,
    "ExtensionStats"          text        not null
);

--: select player
select *
from "SpleefXData"
where "PlayerUUID" = ?;

--: select all
select *
from "SpleefXData";

--: delete player
DELETE
from "SpleefXData"
where "PlayerUUID" = ?;

--: upsert player
INSERT into "SpleefXData"("PlayerUUID", "Coins", "SpleggUpgrade", "PurchasedSpleggUpgrades", "GlobalStats",
                          "ExtensionStats")
values
%s ON CONFLICT("PlayerUUID")
DO
UPDATE
    SET "PlayerUUID" = excluded."PlayerUUID",
    "Coins" = excluded."Coins",
    "SpleggUpgrade" = excluded."SpleggUpgrade",
    "PurchasedSpleggUpgrades" = excluded."PurchasedSpleggUpgrades",
    "GlobalStats" = excluded."GlobalStats",
    "ExtensionStats" = excluded."ExtensionStats"
;