package com.example.auth

/*import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

object GenericSerializer {
    // Serialize an object to a String
    inline fun <reified T : Any> serialize(obj: T): String {
        val properties = obj::class.memberProperties
        return buildString {
            append("{")
            properties.forEachIndexed { index, prop ->
                append("\"${prop.name}\":\"${prop.get(obj)}\"")
                if (index < properties.size - 1) {
                    append(",")
                }
            }
            append("}")
        }
    }

    // Deserialize a String to an object
    inline fun <reified T : Any> deserialize(json: String): T {
        val map = mutableMapOf<String, Any?>()
        var currentKey: String? = null
        var currentValue = StringBuilder()
        var inQuotes = false

        for (char in json) {
            when (char) {
                '{', ',', '}' -> {
                    if (!inQuotes) {
                        if (currentKey != null) {
                            map[currentKey] = currentValue.toString()
                            currentKey = null
                            currentValue.clear()
                        }
                        if (char != ',') {
                            if (char == '{') {
                                // Start of object
                            } else {
                                // End of object
                                break
                            }
                        }
                    } else {
                        currentValue.append(char)
                    }
                }
                '"' -> {
                    inQuotes = !inQuotes
                    if (!inQuotes && currentKey == null) {
                        currentKey = currentValue.toString()
                        currentValue.clear()
                    } else {
                        currentValue.append(char)
                    }
                }
                else -> {
                    currentValue.append(char)
                }
            }
        }

        return map.entries.associate { (key, value) ->
            key to when (val property = T::class.memberProperties.find { it.name == key }) {
                null -> value
                else -> castValue(value, property.returnType.classifier as KClass<*>)
            }
        }.let { T::class.constructors.first().call(*it.values.toTypedArray()) }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> castValue(value: Any?, kClass: KClass<*>): T {
        return when (kClass) {
            String::class -> value as T
            Int::class -> (value as String).toInt() as T
            Boolean::class -> (value as String).toBoolean() as T
            else -> error("Unsupported type: $kClass")
        }
    }
}

// Example usage
data class Person(val name: String, val age: Int, val isActive: Boolean)

fun main() {
    val person = Person("Alice", 30, true)
    val serializedPerson = GenericSerializer.serialize(person)
    println(serializedPerson) // Output: {"name":"Alice","age":"30","isActive":"true"}

    val deserializedPerson: Person = GenericSerializer.deserialize(serializedPerson)
    println(deserializedPerson) // Output: Person(name=Alice, age=30, isActive=true)
}*/
