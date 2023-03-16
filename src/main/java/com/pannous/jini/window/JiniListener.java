package com.pannous.jini.window;
import com.intellij.util.messages.Topic;

public interface JiniListener {

    Topic<JiniListener> TOPIC = Topic.create("Jini", JiniListener.class);

    void onMessageReceived(String message);
}
