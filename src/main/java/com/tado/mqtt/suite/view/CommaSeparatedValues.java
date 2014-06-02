package com.tado.mqtt.suite.view;

import com.tado.mqtt.suite.values.Configuration;
import com.tado.mqtt.suite.values.Result;
import org.apache.commons.io.FileUtils;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

/**
 * Created by simon on 30/05/14.
 */
public class CommaSeparatedValues implements Notification {

    @Override
    public void display(Configuration configuration, Result result) {
        Period executionTime = result.calculateExecutionTime();
        String executionTimeFormatted = PeriodFormat.getDefault().print(executionTime);
        CommandLindInterface.stdout(configuration.getClientCount() + "," + result.messagesSent + "," +
                (executionTime.toStandardSeconds().getSeconds() * 1000 + executionTime.getMillis()) + "," +
                executionTimeFormatted + "," + result.errorConnections + "," + result.errorMessages + "," +
                FileUtils.byteCountToDisplaySize(result.size.get()));
    }

    @Override
    public void shutdown() {
        // empty
    }
}
