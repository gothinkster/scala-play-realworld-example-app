# DC schema

# --- !Ups

CREATE TABLE users (
  id INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  bio VARCHAR(1024),
  image VARCHAR(255),
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  CONSTRAINT user_email_unique UNIQUE (email),
  CONSTRAINT user_username_unique UNIQUE (username)
);

CREATE TABLE security_users (
  id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  CONSTRAINT security_user_email_unique UNIQUE (email)
);

CREATE TABLE articles (
  id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
  slug VARCHAR(255) NOT NULL,
  title VARCHAR(255) NOT NULL,
  description VARCHAR(255) NOT NULL,
  body TEXT NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  author_id INTEGER NOT NULL,
  FOREIGN KEY (author_id) REFERENCES users(id),
);

CREATE TABLE tags (
  id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  CONSTRAINT tag_name_unique UNIQUE(name)
);

CREATE TABLE articles_tags (
  id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
  article_id INTEGER NOT NULL,
  tag_id INTEGER NOT NULL,
  FOREIGN KEY (article_id) REFERENCES articles(id),
  FOREIGN KEY (tag_id) REFERENCES tags(id),
  CONSTRAINT article_tag_id_unique UNIQUE (article_id, tag_id)
);

CREATE TABLE comments (
  id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
  body VARCHAR(4096) NOT NULL,
  article_id INTEGER NOT NULL,
  author_id INTEGER NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  FOREIGN KEY (article_id) REFERENCES articles(id),
  FOREIGN KEY (author_id) REFERENCES users(id)
);

CREATE TABLE follow_associations (
  id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
  follower_id INTEGER NOT NULL,
  followed_id INTEGER NOT NULL,
  FOREIGN KEY (follower_id) REFERENCES users(id),
  FOREIGN KEY (followed_id) REFERENCES users(id),
  CONSTRAINT follow_associations_follower_followed_unq UNIQUE (follower_id, followed_id),
);

CREATE TABLE favorite_associations (
  id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id INTEGER NOT NULL,
  favorited_id INTEGER NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (favorited_id) REFERENCES articles(id),
  CONSTRAINT favorite_associations_user_favorited_unq UNIQUE (user_id, favorited_id),
);

# --- !Downs

DROP TABLE users;
DROP TABLE security_users;
DROP TABLE articles;
DROP TABLE tags;
DROP TABLE articles_tags;
DROP TABLE comments;
DROP TABLE follow_associations;
DROP TABLE favorite_associations;
