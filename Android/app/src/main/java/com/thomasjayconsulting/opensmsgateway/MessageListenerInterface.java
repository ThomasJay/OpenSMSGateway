package com.thomasjayconsulting.opensmsgateway;

public interface MessageListenerInterface {
    // creating an interface method for messages received.
    void messageReceived(String sender, String message);
}