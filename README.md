# OpenSMSGateway

Android application for Open SMS Gateway

Device used in Video - Tracfone | Motorola Moto g Play 2024

This application was built during a YouTube video from my Channel Fast and Simple Development.

The purpose of this code is to enable anyone to send and receive SMS in a low cost fashion.

There are too many large companies that no longer support small developers or Startups and this is a great way to get going, no reason to limit yourself as this system supports multiple phones to it is very scale-able.

The application is a standard Android Java application, minimal UI. It checks for permission to use SMS, asking the user if needed via standard system interface.

Once the applicaiton starts, it polls the API for something to send, the API can return an array of messages to send, normally its about 5 messages per poll.

Each polling interval is normally 1 second.

When a message is received the receivedSMS API is called with the information to include the devices phoneNumber, senders number and message.

Messages received can exceed 140 chars thus can be multi part, the application attempts to maintain order.

Messages send that exceed 140 chars are divided into multi-part messages.

Give this a test and let me know what you think.

The device can connect into a secure http connection.

You can run your server in the cloud, as long as the device can access the API points it works well.

## Check API for Data to Send

### http://10.10.10.13:8181/checkSMSSendQueue

###Check SMS to Send Format

```
{
    "items":[
        {
            "sender":"+14157922640",
            "message":"Hi There"
        }
    ]
}
```

## Send Received SMS Message to Service

### http://10.10.10.13:8181/receivedSMS

###Send Recevied SMS to Server

```
{

    "phoneNumber":"+15105045395",
    "sender":"+14157922640",
    "message":"Hi There"
}
```

## Note: You will NEVER get any SMS Messages received if you have the default Google SMS Client, it eats all SMS Messages.

### You need to disable it or install another client and make it the default like "textra" then you will receive messages.
