create table COMMANDS (
    NAME varchar,
    STATUS varchar,
    PAYLOAD_INPUT text,
    PAYLOAD_OUTPUT text,
    CONTEXT text,
    PROCESSED_AT timestamp(9) with time zone,
    ID varchar,
    PERSISTED_AT timestamp(9) with time zone,
    EXECUTED_AT timestamp(9) with time zone
);