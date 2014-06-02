package com.tado.mqtt.suite.values;

import org.joda.time.Period;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by simon on 30/05/14.
 */
public class Result {

    public AtomicInteger messagesSent = new AtomicInteger(0);
    public AtomicInteger errorMessages = new AtomicInteger(0);
    public AtomicInteger errorConnections = new AtomicInteger(0);
    public AtomicLong size = new AtomicLong(0);
    public long startTimeNanosec;
    public long endTimeNanosec;

    public Period calculateExecutionTime() {
        return new Period(this.startTimeNanosec/1000000, this.endTimeNanosec/1000000);
    }
}
