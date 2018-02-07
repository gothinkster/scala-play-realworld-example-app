# ![RealWorld Example App using Scala and Play Framework](logo.png)

> ### Scala & Play Framework codebase containing real world examples (CRUD, auth, advanced patterns, etc) that adheres to the [RealWorld](https://github.com/gothinkster/realworld) spec and API.

[![Build Status](https://travis-ci.org/Dasiu/realworld-starter-kit.svg?branch=master)](https://travis-ci.org/Dasiu/realworld-starter-kit)
[![Coverage Status](https://coveralls.io/repos/github/Dasiu/realworld-starter-kit/badge.svg?branch=master)](https://coveralls.io/github/Dasiu/realworld-starter-kit?branch=master)

### [Demo with current state of backend implementation and with complete frontend](https://react-redux-realworld-play-backend.stackblitz.io/#/)
### [Referential demo](https://react-redux.realworld.io/)

This codebase was created to demonstrate a fully fledged fullstack application built with **Scala & Play Framework** including CRUD operations, authentication, routing, pagination, and more.

For more information on how to this works with other frontends/backends, head over to the [RealWorld](https://github.com/gothinkster/realworld) repo.

# Getting started

## You need installed:
 * Java >= 8,
 * sbt.

Then:
 * sbt test -- to run tests,
 * sbt run -- to run server in dev mode.

# Project's structure

Application is divided into three main modules: commons, authentication and core.

For simplification they are not represented as sbt's subprojects/submodules. Module is represented as single class with `Components` suffix,
for instance `AuthenticationComponents` or `ArticleComponents`.

Core module contains main business logic which is also divided into `articles` and `users` (and other) modules.
Class `RealWorldApplicationLoader` contains "description" of whole application, it combines all modules together and set up
things like logging, evolutions (db migrations), etc.

Compile time dependency injection is used.

# Security

Pack4j is used to simplify JWT authentication implementation. Generally authentication is implemented as external module implementing
core's API's (`core.authentication.api`). The idea behind it was to allow replacing module's implementation without touching core's code.

# Database

Project uses a H2 in memory database by default, it can be changed in the `application.conf`.
Tests override that properties to use H2 nevertheless, H2 is used for convenience and can be changed easily as well in the `TestUtils.config`.

Proper JDBC driver needs to be added as dependency in build.sbt.

Additionally to avoid Slick's dependent types all over the place, static imports to concrete Slick's profile are used.
For instance `import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}`. They should be changed as well in case of changing underlying database. It looks ugly but imho still better than usage of
dynamic import through dependent types (check out Slick examples to see that).

Slick was used to implement data access layer mainly because it is supported by Lightbend. It also looks more "scala-ish"
and gives perspective for JPA standard in Java ecosystem.

======

Feel free to comment and improve current!

