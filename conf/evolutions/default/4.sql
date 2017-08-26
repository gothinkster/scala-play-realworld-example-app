# DC SCHEMA

# --- !Ups

CREATE TABLE `articles` (
  `id`          INTEGER      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `slug`       VARCHAR(255) NOT NULL,
  `title`    VARCHAR(255) NOT NULL,
  `description`    VARCHAR(255) NOT NULL,
  `body`    TEXT NOT NULL,
  `created_at`  DATETIME,
  `modified_at` DATETIME
);

# --- !Downs

DROP TABLE `articles`;