CREATE OR REPLACE FUNCTION audit.audit_table(target_table regclass, ignored_cols text[]) RETURNS void AS $body$
DECLARE
  _q_txt text;
  _ignored_cols_snip text = '';
  _primary_key_name text;
BEGIN
  EXECUTE 'DROP TRIGGER IF EXISTS audit_trigger ON ' || target_table;

  -- Lookup primary key column name
  SELECT pg_attribute.attname INTO _primary_key_name
  FROM pg_index, pg_class, pg_attribute
  WHERE pg_class.oid = target_table AND indrelid = pg_class.oid AND pg_attribute.attrelid = pg_class.oid
  AND pg_attribute.attnum = any(pg_index.indkey) AND indisprimary;

  IF array_length(ignored_cols,1) > 0 THEN
    _ignored_cols_snip = ', ' || quote_literal(ignored_cols);
  END IF;

  _q_txt = 'CREATE TRIGGER audit_trigger AFTER INSERT OR UPDATE OR DELETE ON ' ||
           target_table || ' FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func(' ||
           quote_literal(_primary_key_name) || _ignored_cols_snip || ');';
  RAISE NOTICE '%',_q_txt;
  EXECUTE _q_txt;
END;
$body$
language 'plpgsql';
