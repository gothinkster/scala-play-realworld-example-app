# DC SCHEMA

# --- !Ups

CREATE TABLE `user` (
  `id`    INT(11)      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `login` VARCHAR(255) NOT NULL
);

  # --- !Downs

DROP TABLE `user`;