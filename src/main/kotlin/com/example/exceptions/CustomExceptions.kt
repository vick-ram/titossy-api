package com.example.exceptions

import org.valiktor.Constraint
import org.valiktor.ConstraintViolation
import org.valiktor.Validator
import org.valiktor.i18n.toMessage
import java.util.*
import javax.print.attribute.standard.Severity

/**
 * Sign In exceptions
 */
class AccountPendingException(override val message: String?) : Exception(message)
class UnAuthorizedAccessException(override val message: String?) : Exception(message)
class NoRecordFoundException(override val message: String?) : Exception(message)
class AlreadyExistsException(override val message: String?) : Exception(message)
class InvalidCredentials(override val message: String?) : Exception(message)
class FailedToCreate(override val message: String?) : Exception(message)
class UnexpectedError(override val message: String?) : Exception(message)

/**
 * Sign out exceptions
 */
class InvalidToken(message: String?) : Exception(message)
class TokenAlreadyBlacklisted(message: String?) : Exception(message)

/**
 * Delete exception
 */
class DeleteException(message: String?) : Exception(message)

fun <E> Validator<E>.Property<String?>.password(): Validator<E>.Property<String?> =
    this.validate(PasswordConstraint()) { it == null || it.matches(Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}\$")) }

class PasswordConstraint : Constraint

data class CustomMessageConstraint(val message: String) : Constraint

fun <E, T> Validator<E>.Property<T>.withMessage(message: String): Validator<E>.Property<T> =
    this.validate(CustomMessageConstraint(message)) { true }

fun Set<ConstraintViolation>.mapToMessages(): List<Pair<String, String>> {
    return this.map { violation ->
        val message = when (val constraint = violation.constraint) {
            is CustomMessageConstraint -> constraint.message
            is PasswordConstraint -> "Password must contain at least 8 characters, including at least one letter and one number."
            is HasLength -> "Phone number must be exactly ${(violation.constraint as HasLength).length} digits long."
            is PhoneNumberConstraint -> "Phone number must begin with 01 or 07."
            else -> violation.toMessage(baseName = "message", Locale.ENGLISH).message
        }
        Pair(violation.property, message)
    }
}

fun <E> Validator<E>.Property<Long?>.hasLength(length: Int): Validator<E>.Property<Long?> =
    this.validate(HasLength(length)) { it == null || it.toString().length == length }

data class HasLength(val length: Int) : Constraint


fun <E> Validator<E>.Property<String?>.isPhoneNumber(): Validator<E>.Property<String?> =
    this.validate(PhoneNumberConstraint()) {
        it == null || it.startsWith("01") || it.startsWith("07")
    }

class PhoneNumberConstraint : Constraint

