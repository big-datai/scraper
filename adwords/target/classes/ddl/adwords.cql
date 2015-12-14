CREATE KEYSPACE adwords
WITH replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};

CREATE TABLE adwords.campaigns (
	store_id text,
	name text,
	id bigint,
	status int,
	budget_id bigint,
	strategy_type text,
	budget_amount decimal,
	budget_period text,
	budget_delivery_method text,
	PRIMARY KEY (( store_id, id ), name)
);