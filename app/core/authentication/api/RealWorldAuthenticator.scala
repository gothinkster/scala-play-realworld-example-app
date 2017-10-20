package core.authentication.api

trait RealWorldAuthenticator[ProfileType <: Profile, AuthenticationResultType <: AuthenticationResult] {
  def authenticate(profile: ProfileType): AuthenticationResultType
}