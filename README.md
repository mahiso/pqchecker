- Remove of pqmessenger
- Set default path of config file to /etc/ldap/pqparams.dat

#### Password quality checker for OpenLDAP password policy overlay

Allows to control the passwords quality (passwords strength) **before** storing them into the OpenLDAP directory server.  
If the password matches **configured** settings, it's accepted. Otherwise, it's rejected.  
The controlled parameters are:  
+ Number of required uppercase characters.
+ Number of required lowercase characters.
+ Number of required special characters.
+ Number of required digits.
+ List of forbidden characters.

The password quality settings are stored in a text file who may be modified by a system administrator. But pqChecker allows reading and modifying these settings, programmatically.

##### Modules

###### 1. Checking passwords module: pqchecker.so
Native shared library for POSIX compliant systems. Checks modified passwords, before storing them into directory.

For further details visit http://www.meddeb.net/pqchecker
