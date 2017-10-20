    # DC SCHEMA

# --- !Ups

ALTER TABLE security_user
ADD COLUMN email VARCHAR(255) NOT NULL;

ALTER TABLE security_user
MODIFY login VARCHAR(255) NULL;

# --- !Downs