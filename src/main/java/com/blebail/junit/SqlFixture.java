package com.blebail.junit;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Executes operations on a DataSource
 *
 * @see DataSource
 * @see Operation
 */
public final class SqlFixture implements BeforeEachCallback {

    private final Supplier<DataSource> dataSourceSupplier;

    private final Operation[] initialOperations;

    private final DbSetupTracker dbSetupTracker;

    public SqlFixture(Supplier<DataSource> dataSourceSupplier) {
        this(dataSourceSupplier, new Operation[]{});
    }

    public SqlFixture(Supplier<DataSource> dataSourceSupplier, Operation... initialOperations) {
        this.dataSourceSupplier = Objects.requireNonNull(dataSourceSupplier);
        this.initialOperations = initialOperations;
        this.dbSetupTracker = new DbSetupTracker();
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        if (initialOperations.length > 0) {
            dbSetupTracker.launchIfNecessary(dbSetup(initialOperations));
        }
    }

    private DbSetup dbSetup(Operation... operations) {
        return new DbSetup(new DataSourceDestination(dataSourceSupplier.get()), Operations.sequenceOf(operations));
    }

    /**
     * Avoids initializing the database in the next test.
     * Use only if the test does not make any modification, such as insert, update or delete, to the database.
     */
    public void readOnly() {
        dbSetupTracker.skipNextLaunch();
    }

    /**
     * Execute operations against the database
     *
     * @param operations operations to be exectuted against the database
     */
    public void exec(Operation... operations) {
        dbSetup(operations).launch();
    }
}
