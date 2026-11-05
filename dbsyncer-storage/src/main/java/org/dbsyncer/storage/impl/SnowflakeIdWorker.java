package org.dbsyncer.storage.impl;

import org.dbsyncer.common.CommonException;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@ConfigurationProperties(prefix = "dbsyncer.web.worker")
public class SnowflakeIdWorker {

    /**
     * Work MachineID(0~31)
     */
    private long id = 1L;

    /**
     * Data centreID(0~31)
     */
    private long dataCenterId = 1L;

    /**
     * msProcurate(0~4095)
     */
    private long sequence = 0L;

    /**
     * Last time.GenerateIDYes.TimeCut.
     */
    private long lastTimestamp = -1L;

    /**
     * Start timeCut!(2015-01-01)
     */
    private final long twepoch = 1420041600000L;

    /**
     * MachineidNumber of places
     */
    private final long workerIdBits = 5L;

    /**
     * Data identifieridNumber of places
     */
    private final long dataCenterIdBits = 5L;

    /**
     * Sequence inidNumber of places
     */
    private final long sequenceBits = 12L;

    /**
     * MachineIDMove Left12bit
     */
    private final long workerIdShift = sequenceBits;

    /**
     * Data identifieridMove Left17bit(12+5)
     */
    private final long dataCenterIdShift = sequenceBits + workerIdBits;

    /**
     * TimeMove left22bit(5+5+12)
     */
    private final long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;

    /**
     * GenerateSequence mask.4095 (0b111111111111=0xfff=4095)
     */
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    public SnowflakeId revert(Long id) {
        long workerId = id >> workerIdShift & ~(-1L << workerIdBits);
        long dataCenterId = id >> dataCenterIdShift & ~(-1L << dataCenterIdBits);
        long timestamp = (id >> timestampLeftShift) + twepoch;
        return new SnowflakeId(workerId, dataCenterId, timestamp);
    }

    /**
     * Get nextID (The methodYesThreadsClear.)
     *
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        // If CurrentTimeLess than previousIDGenerateYes.TimePoke.DescriptionThe system clock should be thrown back at this time.Error
        if (timestamp < lastTimestamp) {
            throw new CommonException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        // IfYesThe same.TimeGenerateOther OrganisermsProcurate
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            // msInner series spill
            if (sequence == 0) {
                // Block the next one.ms,Get NewTimePoke
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        // TimeIt's a change.msProcurateReset
        else {
            sequence = 0L;
        }

        // Last time.GenerateIDYes.TimeCut.
        lastTimestamp = timestamp;

        // Organisation64BitID
        return ((timestamp - twepoch) << timestampLeftShift) //
                | (dataCenterId << dataCenterIdShift) //
                | (id << workerIdShift) //
                | sequence;
    }

    /**
     * Block the next one.ms，Until I get a new one.TimePoke
     *
     * @param lastTimestamp Last time.GenerateIDYes.TimeCut.
     * @return CurrentTimePoke
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * BackHere 2008-2012.msCurrent in UnitTime
     *
     * @return CurrentTime(ms)
     */
    private long timeGen() {
        return Instant.now().toEpochMilli();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDataCenterId() {
        return dataCenterId;
    }

    public void setDataCenterId(long dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    static class SnowflakeId {

        private final long workerId;
        private final long dataCenterId;
        private final long timestamp;

        public SnowflakeId(long workerId, long dataCenterId, long timestamp) {
            this.workerId = workerId;
            this.dataCenterId = dataCenterId;
            this.timestamp = timestamp;
        }

        public long getWorkerId() {
            return workerId;
        }

        public long getDataCenterId() {
            return dataCenterId;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
