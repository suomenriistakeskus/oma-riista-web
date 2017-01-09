CREATE OR REPLACE FUNCTION audit.if_modified_func() RETURNS TRIGGER AS $body$
DECLARE
    audit_row audit.logged_actions;
    excluded_cols text[] = ARRAY[]::text[];
    primary_key_name text;
    user_column_name text = 'modified_by_user_id';
BEGIN
    primary_key_name = TG_ARGV[0]::text;

    audit_row = ROW(
        nextval('audit.logged_actions_event_id_seq'), -- event_id
        TG_TABLE_NAME::text,                          -- table_name
        NULL,                                         -- primary key value
        TG_RELID,                                     -- relation OID for much quicker searches
        statement_timestamp(),                        -- action_tstamp_stm
        NULL,                                         -- user_id
        txid_current(),                               -- transaction ID
        substring(TG_OP,1,1),                         -- action
        NULL, NULL                                    -- row_data, changed_fields
    );

    IF TG_ARGV[1] IS NOT NULL THEN
        excluded_cols = TG_ARGV[1]::text[];
    END IF;

    IF (TG_OP = 'UPDATE') THEN
        audit_row.row_data = hstore(OLD.*) - excluded_cols;
        audit_row.changed_fields =  (hstore(NEW.*) - hstore(OLD.*)) - excluded_cols;
        EXECUTE 'SELECT ($1).' || primary_key_name INTO audit_row.primary_key_value USING NEW;
        EXECUTE 'SELECT ($1).' || user_column_name INTO audit_row.user_id USING NEW;
        IF audit_row.changed_fields = hstore('') THEN
            -- All changed fields are ignored. Skip this update.
            RETURN NULL;
        END IF;
    ELSIF (TG_OP = 'DELETE') THEN
        audit_row.row_data = hstore(OLD.*);
        EXECUTE 'SELECT ($1).' || primary_key_name INTO audit_row.primary_key_value USING OLD;
        EXECUTE 'SELECT ($1).' || user_column_name INTO audit_row.user_id USING OLD;
    ELSIF (TG_OP = 'INSERT') THEN
        audit_row.row_data = hstore(NEW.*) - excluded_cols;
        EXECUTE 'SELECT ($1).' || primary_key_name INTO audit_row.primary_key_value USING NEW;
        EXECUTE 'SELECT ($1).' || user_column_name INTO audit_row.user_id USING NEW;
    ELSE
        RETURN NULL;
    END IF;

    INSERT INTO audit.logged_actions VALUES (audit_row.*);

    RETURN NULL;
END;
$body$
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = pg_catalog, public;
