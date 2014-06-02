package com.tado.mqtt.suite.view;

import com.tado.mqtt.suite.values.Configuration;
import com.tado.mqtt.suite.values.Result;

/**
 * Created by simon on 30/05/14.
 */
public interface Notification {
    public void display(Configuration configuration, Result result);
    public void shutdown();

}
