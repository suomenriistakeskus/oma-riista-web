-- https://wiki.postgresql.org/wiki/Audit_trigger_91plus
-- CREATE EXTENSION IF NOT EXISTS hstore;
CREATE SCHEMA IF NOT EXISTS audit;
REVOKE ALL ON SCHEMA audit FROM public;

CREATE TABLE audit.logged_actions (
  event_id BIGSERIAL PRIMARY KEY,
  table_name TEXT NOT NULL,
  primary_key_value text NOT NULL,
  relid OID NOT NULL,
  action_tstamp_stm TIMESTAMP WITH TIME ZONE NOT NULL,
  user_id BIGINT,
  transaction_id BIGINT NOT NULL,
  action CHAR(1) NOT NULL CHECK (action IN ('I','D','U', 'T')),
  row_data hstore,
  changed_fields hstore
);

REVOKE ALL ON audit.logged_actions FROM public;

CREATE INDEX logged_actions_relid_idx ON audit.logged_actions(relid);
CREATE INDEX logged_actions_action_tstamp_tx_stm_idx ON audit.logged_actions(action_tstamp_stm);
CREATE INDEX logged_actions_user_idx ON audit.logged_actions(user_id);
