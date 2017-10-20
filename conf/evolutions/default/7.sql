    # DC SCHEMA

# --- !Ups

ALTER TABLE `user`
CHANGE COLUMN login username VARCHAR(255) NOT NULL;

ALTER TABLE `user`
ADD COLUMN created_at DATETIME;

ALTER TABLE `user`
ADD COLUMN modified_at DATETIME;



# --- !Downs