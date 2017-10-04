# DC SCHEMA

# --- !Ups

ALTER TABLE `user`
ADD COLUMN email VARCHAR(255) NOT NULL;

# --- !Downs