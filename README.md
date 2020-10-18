# JUnit SQL test library
[![Build Status](https://travis-ci.org/baptistelebail/junit-sql.svg?branch=master)](https://travis-ci.org/baptistelebail/junit-sql)

This library provides a way to simply and quickly test any SQL-related code within JUnit on a database, via two [JUnit 5](https://junit.org/junit5/) [Extension](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/extension/Extension.html):
- [SqlMemoryDb](https://github.com/baptistelebail/junit-sql/blob/master/src/main/java/com.blebail.junit/SqlMemoryDb.java)
- [SqlFixture](https://github.com/baptistelebail/junit-sql/blob/master/src/main/java/com.blebail.junit/SqlFixture.java)

It uses [DbSetup](http://dbsetup.ninja-squad.com/) for its nice and simple DSL for SQL [Operations](http://dbsetup.ninja-squad.com/apidoc/2.1.0/com/ninja_squad/dbsetup/Operations.html).

## How to use
### Add the repository
```xml
<repositories>
    <repository>
        <id>blebail-repository</id>
        <url>http://blebail.com/repository/</url>
    </repository>   
</repositories>
```
### Add the junit-sql dependency
```xml
    <dependency>
        <groupId>com.blebail.junit</groupId>
        <artifactId>junit-sql</artifactId>
        <scope>test</scope>
        <version>0.1</version>
    </dependency>
```

### The SqlMemoryDb extension

- `SqlMemoryDb` is a [BeforeAllCallback](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/extension/BeforeAllCallback.html), it will be executed once before all tests
- `new SqlMemoryDb()` will provide a memory [DataSource](https://docs.oracle.com/javase/8/docs/api/javax/sql/DataSource.html) (with [H2 Database](https://www.h2database.com/html/main.html)), and load a SQL schema from the file `src/test/resources/db/schema.sql` if it exists
- `new SqlMemoryDb(Path)` allows to override the default file path of the SQL schema

### The SqlFixture extension
- `SqlFixture` is a [BeforeEachCallback](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/extension/BeforeEachCallback.html), it will be executed once before each test
- `SqlFixture` needs a [DataSource](https://docs.oracle.com/javase/8/docs/api/javax/sql/DataSource.html) (and thus can used in conjonction with `SqlMemoryDb`)
- `new SqlFixture(Supplier<DataSource>, Operation...)` allows to initialize the database with a set of [Operations](http://dbsetup.ninja-squad.com/apidoc/2.1.0/com/ninja_squad/dbsetup/Operations.html)
- `SqlFixture.exec(Operation...)` allows to execute a set of [Operations](http://dbsetup.ninja-squad.com/apidoc/2.1.0/com/ninja_squad/dbsetup/Operations.html) against the database
- `SqlFixture.readOnly()` is just a perfomance optimisation in case a test only reads the test database, in that case it's useless to clean and reset the test database

### Example

The `src/test/resoruces/db/schema.sql` file:

```sql
DROP TABLE IF EXISTS account;

CREATE TABLE account(
    id VARCHAR(4) NOT NULL,
    username VARCHAR(255) NOT NULL
);

ALTER TABLE account ADD PRIMARY KEY (id);
```

The JUnit test:

```java
class SqlTest {
    
    @RegisterExtension
    static SqlMemoryDb sqlMemoryDb = new SqlMemoryDb();

    @RegisterExtension
    SqlFixture sqlFixture = new SqlFixture(sqlMemoryDb::dataSource, insertDefaultAccounts());

    @Test
    void testReadOnly() {
        sqlFixture.readOnly();
        
        // test
    }

    @Test
    void testWrite() {
        sqlFixture.exec(insertAccountJohnDoe());
        
        // test
    }

    private Operation insertDefaultAccounts() {
        return Operations.sequenceOf(
                Operations.deleteAllFrom("account"),
                Operations.insertInto("account")
                    .columns("id", "username")
                    .values("0001", "admin")
                    .build()
        );
    }

    private Operation insertJohnDoe() {
        return Operations.insertInto("account")
                .columns("id", "username")
                .values("0002", "johndoe")
                .build();
    }
}
```

## Technical Stack
* [Java 11](https://jdk.java.net/11/)
* [Maven](https://maven.apache.org/)
* [JUnit 5](https://junit.org/junit5/)
* [H2 Database](https://www.h2database.com/html/main.html)
* [DbSetup](http://dbsetup.ninja-squad.com/)
