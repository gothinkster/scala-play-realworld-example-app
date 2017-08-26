import play.filters.cors.CORSFilter
import play.http._

class Filters (corsFilter: CORSFilter)
  extends DefaultHttpFilters(corsFilter)