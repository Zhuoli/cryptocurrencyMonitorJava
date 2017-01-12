/**
 * This class is generated by jOOQ
 */
package JooqORM.tables.records;


import JooqORM.tables.Heartbeat;

import java.sql.Timestamp;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.8.4"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class HeartbeatRecord extends UpdatableRecordImpl<HeartbeatRecord> implements Record2<String, Timestamp> {

    private static final long serialVersionUID = -690610785;

    /**
     * Setter for <code>EarningManagerDB.heartbeat.name</code>.
     */
    public void setName(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>EarningManagerDB.heartbeat.name</code>.
     */
    public String getName() {
        return (String) get(0);
    }

    /**
     * Setter for <code>EarningManagerDB.heartbeat.heartbeat</code>.
     */
    public void setHeartbeat(Timestamp value) {
        set(1, value);
    }

    /**
     * Getter for <code>EarningManagerDB.heartbeat.heartbeat</code>.
     */
    public Timestamp getHeartbeat() {
        return (Timestamp) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row2<String, Timestamp> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row2<String, Timestamp> valuesRow() {
        return (Row2) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field1() {
        return Heartbeat.HEARTBEAT.NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field2() {
        return Heartbeat.HEARTBEAT.HEARTBEAT_;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value1() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value2() {
        return getHeartbeat();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HeartbeatRecord value1(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HeartbeatRecord value2(Timestamp value) {
        setHeartbeat(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HeartbeatRecord values(String value1, Timestamp value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached HeartbeatRecord
     */
    public HeartbeatRecord() {
        super(Heartbeat.HEARTBEAT);
    }

    /**
     * Create a detached, initialised HeartbeatRecord
     */
    public HeartbeatRecord(String name, Timestamp heartbeat) {
        super(Heartbeat.HEARTBEAT);

        set(0, name);
        set(1, heartbeat);
    }
}