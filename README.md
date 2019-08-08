# ![RealWorld Example App using Scala and Play Framework](logo.png)

> ### Scala & Play Framework codebase containing real world examples (CRUD, auth, advanced patterns, etc) that adheres to the [RealWorld](https://github.com/gothinkster/realworld) spec and API.

### [Referential demo](https://react-redux.realworld.io/)

This codebase was created to demonstrate a fully fledged fullstack application built with **Scala & Play Framework** including CRUD operations, authentication, routing, pagination, and more.

For more information on how to this works with other frontends/backends, head over to the [RealWorld](https://github.com/gothinkster/realworld) repo.

# Getting started

## You need installed:
 * Java >= 8 <= 11,
 * sbt.

Then:
 * sbt test -- to run tests,
 * sbt run -- to run server in dev mode.

# Project's structure

Application is divided into three main modules: articles, authentication and users.

For simplification they are not represented as sbt's subprojects/submodules. Module is represented as single class with `Components` suffix,
for instance `AuthenticationComponents` or `ArticleComponents`.

Class `RealWorldApplicationLoader` contains "description" of whole application, it combines all modules together and set up
things like logging, evolutions (db migrations), etc.

Compile time dependency injection is used.

# Security

Simple - naive - JWT authentication implementation is used. Authentication module exists to separate authentication logic from business logic related to users, like update of user's profile, etc.

# Database

Project uses a H2 in memory database by default, it can be changed in the `application.conf` (and in `TestUtils.config` for tests).

Proper JDBC driver needs to be added as dependency in build.sbt.

Additionally to avoid Slick's dependent types all over the place, static imports to concrete Slick's profile are used.
For instance `import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}`. They should be changed as well in case of changing underlying database. It looks ugly but imho still better than usage of
dynamic import through dependent types (check out Slick examples to see that).

Slick was used to implement data access layer mainly because it is supported by Lightbend. It also looks more "scalish".

======

Feel free to comment and improve current implementation!

