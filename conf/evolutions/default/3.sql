# DC SCHEMA

# --- !Ups

CREATE TABLE `security_user` (
  `id`          INTEGER      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `login`       VARCHAR(255) NOT NULL,
  `password`    VARCHAR(255) NOT NULL,
  `created_at`  DATETIME,
  `modified_at` DATETIME
);

CREATE TABLE `access_token` (
  `id`               INTEGER      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `id_security_user` INTEGER      NOT NULL,
  `token`            VARCHAR(255) NOT NULL,
  `created_at`       DATETIME,
  `modified_at`      DATETIME,
  FOREIGN KEY (`id_security_user`) REFERENCES `security_user` (`id`)
);

ALTER TABLE security_user
ADD CONSTRAINT security_user_login_unique UNIQUE (login);

CREATE INDEX security_user_login_idx ON security_user(login);


# --- !Downs

DROP TABLE `access_token`;
DROP TABLE `security_user`;