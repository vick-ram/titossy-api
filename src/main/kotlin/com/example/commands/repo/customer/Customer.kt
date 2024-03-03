package com.example.commands.repo.customer

import com.example.auth.Config
import com.example.auth.TokePair
import com.example.auth.TokenBlackList
import com.example.auth.generateTokens
import com.example.db.AddressTable
import com.example.db.AddressTable.city
import com.example.db.AddressTable.state
import com.example.db.AddressTable.street
import com.example.db.AddressTable.zip
import com.example.db.CustomerTable
import com.example.db.DatabaseUtil.dbQuery
import com.example.models.Address
import com.example.models.Customer
import com.example.models.CustomerRequest
import com.example.models.hashedPassword
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll

/*suspend fun insertCustomer(customer: Customer) = dbQuery {
    val addressId = AddressTable.insert {address->
        address[street] = customer.address.street
        address[city] = customer.address.city
        address[state] = customer.address.state
        address[zip] = customer.address.zip
    } get AddressTable.id
    CustomerTable.insert {
        it[id] = customer.id
        it[username] = customer.username
        it[firstName] = customer.firstName
        it[lastName] = customer.lastName
        it[gender] = customer.gender
        it[phone] = customer.phone
        it[address] = addressId
        it[email] = customer.email
        it[password] = hashedPassword(customer.password)
        it[status] = "pending"
    }
}*/

suspend fun insertCustomer(customerRequest: CustomerRequest) = dbQuery {
    val addressId = AddressTable.insertAndGetId { addressEntry ->
        addressEntry[street] = customerRequest.address.street
        addressEntry[city] = customerRequest.address.city
        addressEntry[state] = customerRequest.address.state
        addressEntry[zip] = customerRequest.address.zip
    }
    val customerId = CustomerTable.insertAndGetId {
        it[username] = customerRequest.username
        it[firstName] = customerRequest.firstName
        it[lastName] = customerRequest.lastName
        it[gender] = customerRequest.gender
        it[phone] = customerRequest.phone
        it[address] = addressId
        it[email] = customerRequest.email
        it[password] = hashedPassword(customerRequest.password)
        it[status] = "pending"
    }
    Customer(
        id = customerId.value,
        username = customerRequest.username,
        firstName = customerRequest.firstName,
        lastName = customerRequest.lastName,
        phone = customerRequest.phone,
        address = Address(
            id = addressId.value,
            street = customerRequest.address.street,
            city = customerRequest.address.city,
            state = customerRequest.address.state,
            zip = customerRequest.address.zip
        ),
        gender = customerRequest.gender,
        email = customerRequest.email,
        password = hashedPassword(customerRequest.password),
        status = "pending"
    )
}

suspend fun signInCustomer(username: String, password: String): TokePair? = dbQuery {
    val customerInDb = (CustomerTable innerJoin AddressTable)
        .selectAll().where { CustomerTable.username eq username }
        .mapNotNull { resultRow(it) }
        .singleOrNull()


    if (customerInDb != null && customerInDb.password == hashedPassword(password)) {
        generateTokens(username, Config.SECRET, Config.ISSUER, Config.AUDIENCE)
    } else {
        null
    }
}

private fun resultRow(row: ResultRow) = Customer(
    row[CustomerTable.id].value,
    row[CustomerTable.username],
    row[CustomerTable.firstName],
    row[CustomerTable.lastName],
    row[CustomerTable.phone],
    Address(
        row[CustomerTable.address].value,
        row[street],
        row[city],
        row[state],
        row[zip]
    ),
    row[CustomerTable.gender],
    row[CustomerTable.email],
    row[CustomerTable.password],
    row[CustomerTable.status]
)

suspend fun getCustomerByUsername(username: String): Customer? = dbQuery {
    (CustomerTable innerJoin AddressTable)
        .selectAll().where { CustomerTable.username eq username }
        .mapNotNull { resultRow(it) }
        .singleOrNull()
}

suspend fun signOutCustomer(token: String) = dbQuery {
    TokenBlackList.blacklistToken(token)
}
