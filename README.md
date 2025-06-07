# OpenSMSGateway

Android application for Open SMS Gateway

## http://10.10.10.13:8181/checkSMSSendQueue

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

## http://10.10.10.13:8181/receivedSMS

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
