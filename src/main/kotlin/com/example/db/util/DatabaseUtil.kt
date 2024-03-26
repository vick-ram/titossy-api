package com.example.db.util

import com.example.db.booking.BookingItemsTable
import com.example.db.booking.BookingTable
import com.example.db.customer.CustomerAddressTable
import com.example.db.customer.CustomerTable
import com.example.db.employee.EmployeeTable
import com.example.db.feedback.FeedbackTable
import com.example.db.inventory.InventoryTable
import com.example.db.order.OrderItemsTable
import com.example.db.order.OrderTable
import com.example.db.payment.CustomerPaymentTable
import com.example.db.payment.SupplierPaymentTable
import com.example.db.product.ProductTable
import com.example.db.service.ServiceTable
import com.example.db.supplier.SupplierAddressTable
import com.example.db.supplier.SupplierTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseUtil {
    fun init() {
        Database.connect(hikari())
        transaction {
            exec(
                stmt = """
            DO $$ BEGIN
            IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'roles') THEN
                CREATE TYPE roles AS ENUM ('ADMIN', 'MANAGER', 'INVENTORY', 'FINANCE', 'SUPERVISOR', 'CLEANER');
            END IF;
            END $$;
        """.trimIndent()
            )
            exec(
                stmt = """
            DO $$ BEGIN
            IF NOT EXISTS (SELECT 1 FROM Pg_type WHERE typname = 'availability') THEN
            CREATE TYPE availability AS ENUM ('AVAILABLE', 'UNAVAILABLE', 'ON_LEAVE');
            END IF;
            END $$;
            """.trimIndent()
            )
            exec(
                stmt = """
                DO $$ BEGIN
                IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'gender') THEN
                CREATE TYPE gender AS ENUM ('MALE', 'FEMALE', 'OTHER', 'NOT_SPECIFIED');
                END IF;
                END $$;
            """.trimIndent()
            )
            exec(
                stmt = """
                DO $$ BEGIN
                IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'bookingstatus') THEN
                CREATE TYPE bookingstatus AS ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');
                END IF;
                END $$;
            """.trimIndent()
            )
            exec(
                stmt = """
                DO $$ BEGIN
                IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'approvalstatus') THEN
                CREATE TYPE approvalstatus AS ENUM ('PENDING', 'APPROVED', 'REJECTED');
                END IF;
                END $$;
            """.trimIndent()
            )
            exec(
                stmt = """
                DO $$ BEGIN
                IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'paymentstatus') THEN
                CREATE TYPE paymentstatus AS ENUM ('PENDING', 'CONFIRMED', 'REFUNDED', 'CANCELLED');
                END IF;
                END $$;
            """.trimIndent()
            )
            SchemaUtils.create(
                CustomerTable,
                CustomerAddressTable,
                SupplierTable,
                SupplierAddressTable,
                EmployeeTable,
                ServiceTable,
                InventoryTable,
                ProductTable,
                OrderTable,
                OrderItemsTable,
                BookingTable,
                BookingItemsTable,
                FeedbackTable,
                CustomerPaymentTable,
                SupplierPaymentTable
            )

            exec(stmt = "UPDATE ${ProductTable.tableName} SET tsv = to_tsvector('english', name || ' ' || description)")
            exec(
                """
                DO $$ BEGIN
                IF NOT EXISTS (
                    SELECT 1
                    FROM   pg_class c
                    JOIN   pg_namespace n ON n.oid = c.relnamespace
                    WHERE  c.relname = 'product_tsv_idx'
                    AND    n.nspname = 'public'
                ) THEN
                    EXECUTE 'CREATE INDEX product_tsv_idx ON ${ProductTable.tableName} USING GIN(tsv)';
                END IF;
                END $$;
            """.trimIndent()
                        )
//            exec("CREATE INDEX product_tsv_idx ON ${ProductTable.tableName} USING GIN(tsv)")
            exec(
                """
                CREATE OR REPLACE FUNCTION product_tsv_trigger() RETURNS trigger AS $$
                BEGIN
                NEW.tsv :=
                to_tsvector('english', COALESCE(NEW.name, '') || ' ' || COALESCE(NEW.description, ''));
                RETURN NEW;
                END
                $$ LANGUAGE plpgsql;
                """.trimIndent()
                )

            exec(
                """
                DROP TRIGGER IF EXISTS product_tsv_update ON ${ProductTable.tableName};
                """.trimIndent()
            )
            exec(
                """
                CREATE TRIGGER product_tsv_update BEFORE INSERT OR UPDATE
                ON ${ProductTable.tableName} FOR EACH ROW EXECUTE FUNCTION product_tsv_trigger();
                """.trimIndent()
            )

            /*Search for services*/
            exec(stmt = "UPDATE ${ServiceTable.tableName} SET tsv = to_tsvector('english', name || ' ' || description)")
            exec(
                """
                DO $$ BEGIN
                IF NOT EXISTS (
                    SELECT 1
                    FROM   pg_class c
                    JOIN   pg_namespace n ON n.oid = c.relnamespace
                    WHERE  c.relname = 'service_tsv_idx'
                    AND    n.nspname = 'public'
                ) THEN
                    EXECUTE 'CREATE INDEX service_tsv_idx ON ${ProductTable.tableName} USING GIN(tsv)';
                END IF;
                END $$;
            """.trimIndent()
                        )
//            exec("CREATE INDEX service_tsv_idx ON ${ServiceTable.tableName} USING GIN(tsv)")
            exec(
                """
                CREATE OR REPLACE FUNCTION service_tsv_trigger() RETURNS trigger AS $$
                BEGIN
                NEW.tsv :=
                to_tsvector('english', COALESCE(NEW.name, '') || ' ' || COALESCE(NEW.description, ''));
                RETURN NEW;
                END
                $$ LANGUAGE plpgsql;
            """.trimIndent()
            )

            exec(
                """
                DROP TRIGGER IF EXISTS service_tsv_update ON ${ServiceTable.tableName};
            """.trimIndent()
            )
            exec(
                """
                CREATE TRIGGER service_tsv_update BEFORE INSERT OR UPDATE
                ON ${ServiceTable.tableName} FOR EACH ROW EXECUTE FUNCTION service_tsv_trigger();
            """.trimIndent()
            )
        }
    }

    suspend fun <T> dbQuery(block: () -> T): T {
        return newSuspendedTransaction(Dispatchers.IO) { block() }
    }
}

fun hikari(): HikariDataSource {
    val config = HikariConfig()
        .apply {
            dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
            addDataSourceProperty("databaseName", "titossy-cleaning-services")
            addDataSourceProperty("portNumber", 5432)
            addDataSourceProperty("serverName", "localhost")
            addDataSourceProperty("user", "postgres")
            addDataSourceProperty("password", System.getenv("DB_PASSWORD"))

            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

    return HikariDataSource(config)
}