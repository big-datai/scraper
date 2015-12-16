CREATE KEYSPACE adwords
WITH replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};

CREATE TABLE adwords.campaigns (
	store_id text,
	name text,
	id bigint,
	url text,
	status text,
	budget_id bigint,
	strategy_type text,
	budget_amount decimal,
	budget_period text,
	budget_delivery_method text,
	PRIMARY KEY (( store_id, id ), name)
);

CREATE TABLE adwords.adgroups (
	campaign_id bigint,
	name text,
	PRIMARY KEY (campaign_id, name)
);

CREATE TABLE adwords.adtexts (
	group_id BigInt,
    headline text,
    desc1 text,
    desc2 text,
    displayUrl text,
    finalUrl text,
	PRIMARY KEY (group_id, headline)
)