package commons.repositories

import slick.lifted.{CanBeQueryCondition, Query, Rep}

// optionally filter on a column with a supplied predicate - taken from: https://gist.github.com/cvogt/9193220
case class MaybeFilter[X, Y](query: Query[X, Y, Seq]) {

  def filter[T, R <: Rep[_] : CanBeQueryCondition](data: Option[T])(f: T => X => R): MaybeFilter[X, Y] = {
    data.map(v => MaybeFilter(query.filter(f(v)))).getOrElse(this)
  }

}