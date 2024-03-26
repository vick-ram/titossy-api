package com.example.exceptions

import kotlinx.serialization.Serializable

@Serializable
class AccountPendingException(override val message: String?): Exception()
@Serializable
class UnAuthorizedAccessException(override val message: String?): Exception()
@Serializable
class UserDoesNotExist(override val message: String?): Exception()
@Serializable
class AlreadyExistsException(override val message: String?): Exception()
@Serializable
class PasswordDidNotMatch(override val message: String?): Exception()
@Serializable
class FailedToValidate(override val message: String?): Exception()