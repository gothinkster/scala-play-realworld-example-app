package authentication.api

trait TokenGenerator[ProfileType <: Profile, ResultType] {
  def generate(profile: ProfileType): ResultType
}