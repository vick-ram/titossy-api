package com.example.commons

import com.example.db.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI

object DatabaseUtil {
    fun init(
        url: String,
        driver: String,
/*        user: String,
        password: String*/
    ) {
        Database.connect(hikari(url, driver/*, user, password*/))
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(
                CustomerTable,
                SupplierTable,
                EmployeeTable,
                ProductTable,
                PurchaseOrders,
                PurchaseOrderItems,
                FeedbackTable,
                CustomerPaymentTable,
                SupplierPaymentTable,
            )
            SchemaUtils.create(
                Bookings,
                BookingAssignments,
                Services,
                ServiceCart,
                ProductCarts,
                ServiceAddOns,
                BookingServiceAddOns,
                Notifications,
                ActivityLogs,
                Messages
            )
            /**
             * Trigger for customer for performing full text search
             */
            exec(
                stmt = """
                    UPDATE ${CustomerTable.tableName}
                    SET tsv = to_tsvector('english', full_name || ' ' || email)
                """.trimIndent()
            )
            exec(
                """
                    DO $$ BEGIN
                    IF NOT EXISTS (
                        SELECT 1
                        FROM pg_class c
                        JOIN pg_namespace n on n.oid = c.relnamespace
                        WHERE c.relname = 'customer_tsv_idx'
                        AND n.nspname = 'public'
                    ) THEN
                        EXECUTE 'CREATE INDEX customer_tsv_idx ON ${CustomerTable.tableName} USING gin(tsv)';
                    END IF;
                    END $$;
                """.trimIndent()
            )
            exec(
                stmt = """
                CREATE OR REPLACE FUNCTION customer_tsv_trigger() RETURNS trigger AS $$
                BEGIN
                NEW.tsv :=
                to_tsvector('english', COALESCE(NEW.full_name, '') || ' ' || COALESCE(NEW.email, ''));
                RETURN NEW;
                END
                $$ LANGUAGE plpgsql;
                """.trimIndent()
            )
            exec(
                """
                DROP TRIGGER IF EXISTS customer_tsv_update ON ${CustomerTable.tableName};
                """.trimIndent()
            )
            exec(
                stmt = """
                CREATE TRIGGER customer_tsv_update BEFORE INSERT OR UPDATE
                ON ${CustomerTable.tableName} FOR EACH ROW EXECUTE FUNCTION customer_tsv_trigger();
                """.trimIndent()
            )


            /**
             * Full-text search for Supplier
             */
            exec(
                stmt = """
                    UPDATE ${SupplierTable.tableName}
                    SET tsv = to_tsvector('english', full_name || ' ' || email)
                """.trimIndent()
            )
            exec(
                """
                    DO $$ BEGIN
                    IF NOT EXISTS (
                        SELECT 1
                        FROM pg_class c
                        JOIN pg_namespace n on n.oid = c.relnamespace
                        WHERE c.relname = 'supplier_tsv_idx'
                        AND n.nspname = 'public'
                    ) THEN
                        EXECUTE 'CREATE INDEX supplier_tsv_idx ON ${SupplierTable.tableName} USING gin(tsv)';
                    END IF;
                    END $$;
                """.trimIndent()
            )
            exec(
                stmt = """
                CREATE OR REPLACE FUNCTION supplier_tsv_trigger() RETURNS trigger AS $$
                BEGIN
                NEW.tsv :=
                to_tsvector('english', COALESCE(NEW.full_name, '') || ' ' || COALESCE(NEW.email, ''));
                RETURN NEW;
                END
                $$ LANGUAGE plpgsql;
                """.trimIndent()
            )
            exec(
                """
                DROP TRIGGER IF EXISTS supplier_tsv_update ON ${SupplierTable.tableName};
                """.trimIndent()
            )
            exec(
                stmt = """
                CREATE TRIGGER supplier_tsv_update BEFORE INSERT OR UPDATE
                ON ${SupplierTable.tableName} FOR EACH ROW EXECUTE FUNCTION supplier_tsv_trigger();
                """.trimIndent()
            )


            /**
             * Full-text search for employees
             */
            exec(
                stmt = """
                    UPDATE ${EmployeeTable.tableName}
                    SET tsv = to_tsvector('english', username || ' ' || full_name || ' ' || email || ' ' || role)
                """.trimIndent()
            )
            exec(
                """
                    DO $$ BEGIN
                    IF NOT EXISTS (
                        SELECT 1
                        FROM pg_class c
                        JOIN pg_namespace n on n.oid = c.relnamespace
                        WHERE c.relname = 'employee_tsv_idx'
                        AND n.nspname = 'public'
                    ) THEN
                        EXECUTE 'CREATE INDEX employee_tsv_idx ON ${EmployeeTable.tableName} USING gin(tsv)';
                    END IF;
                    END $$;
                """.trimIndent()
            )
            exec(
                stmt = """
                CREATE OR REPLACE FUNCTION employee_tsv_trigger() RETURNS trigger AS $$
                BEGIN
                NEW.tsv :=
                to_tsvector('english', COALESCE(NEW.username, '') || ' ' || COALESCE(NEW.full_name, '') || ' ' || COALESCE(NEW.email, '') || ' ' || COALESCE(NEW.role));
                RETURN NEW;
                END
                $$ LANGUAGE plpgsql;
                """.trimIndent()
            )
            exec(
                """
                DROP TRIGGER IF EXISTS employee_tsv_update ON ${EmployeeTable.tableName};
                """.trimIndent()
            )
            exec(
                stmt = """
                CREATE TRIGGER employee_tsv_update BEFORE INSERT OR UPDATE
                ON ${EmployeeTable.tableName} FOR EACH ROW EXECUTE FUNCTION employee_tsv_trigger();
                """.trimIndent()
            )

            /**
             * Trigger functions and indices for Product table for performing
             * full-text search
             */

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
                stmt = """
                CREATE TRIGGER product_tsv_update BEFORE INSERT OR UPDATE
                ON ${ProductTable.tableName} FOR EACH ROW EXECUTE FUNCTION product_tsv_trigger();
                """.trimIndent()
            )


            /*Search for services*/
            exec(stmt = "UPDATE ${Services.tableName} SET tsv = to_tsvector('english', name || ' ' || description)")
            exec(
                stmt = """
                DO $$ BEGIN
                IF NOT EXISTS (
                    SELECT 1
                    FROM   pg_class c
                    JOIN   pg_namespace n ON n.oid = c.relnamespace
                    WHERE  c.relname = 'service_tsv_idx'
                    AND    n.nspname = 'public'
                ) THEN
                    EXECUTE 'CREATE INDEX service_tsv_idx ON ${Services.tableName} USING GIN(tsv)';
                END IF;
                END $$;
            """.trimIndent()
            )
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
                stmt = """
                DROP TRIGGER IF EXISTS service_tsv_update ON ${Services.tableName};
            """.trimIndent()
            )
            exec(
                stmt = """
                CREATE TRIGGER service_tsv_update BEFORE INSERT OR UPDATE
                ON ${Services.tableName} FOR EACH ROW EXECUTE FUNCTION service_tsv_trigger();
            """.trimIndent()
            )
        }
    }

    suspend fun <T> dbQuery(block: Transaction.() -> T): T {
        return newSuspendedTransaction(Dispatchers.IO) { block() }
    }
}

fun hikari(
    url: String,
    driver: String,
/*    user: String,
    password: String*/
): HikariDataSource {
    val config = HikariConfig()
        .apply {
            //this.jdbcUrl = "${url}?rewriteBatchedInserts=true"
            val uri = URI(url)
            val username = uri.userInfo.split(":").toTypedArray()[0]
            val pass = uri.userInfo.split(":").toTypedArray()[1]
            val jdbcUrl = "jdbc:postgresql://${uri.host}:${uri.port}${uri.path}?sslmode=require&user=$username&password=$pass"
            this.jdbcUrl = jdbcUrl
            this.driverClassName = driver
            /*this.username = user
            this.password = password*/
            this.maximumPoolSize = 6
            this.isAutoCommit = false
            this.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

    return HikariDataSource(config)
}