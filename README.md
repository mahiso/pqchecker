#### Password quality checker for OpenLDAP password policy overlay

Allows to control the passwords quality (passwords strength) **before** storing them into the OpenLDAP directory server.  
If the password matches **configured** settings, it's accepted. Otherwise, it's rejected.  
The controlled parameters are:  
+ Number of required uppercase characters.
+ Number of required lowercase characters.
+ Number of required special characters.
+ Number of required digits.
+ List of forbidden characters.

The password quality settings are stored in a text file who may be modified by a system administrator. But pqChecker allows reading and modifying these settings, programmatically. It allows, also, broadcasting the modified passwords, in real time, to another systems who use it (database system, mails server..).

##### Two independent modules

###### 1. Checking passwords module: pqchecker.so 
Native shared library for POSIX compliant systems. Checks modified passwords, before storing them into directory.

###### 2. Communicating with JMS compliant server: pqmessenger.jar 
Cross platform Java daemon:
+ Allows reading and modifying passwords quality settings programmatically.
+ Allows broadcasting the modified passwords in real time.

![alt tag](http://www.meddeb.net/pqchecker/res/pqchecker-overview-evolution.png)

For further details visit http://www.meddeb.net/pqchecker
