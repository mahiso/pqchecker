#### Password quality checker for OpenLDAP

Allows to control the quality of passwords *before* storing them in the directory.  
If the password mets *configured* parameters, it is accepted. It is rejected otherwise.  
The controlled parameters are:  
+ Number of required uppercase characters.
+ Number of required lowercase characters.
+ Number of required special characters.
+ Number of required digits.
+ List of forbidden characters.

Also allows reading and modifying the passwords quality parameters, programmatically

##### Two independent modules

###### 1. Checking passwords module 
Native shared library for POSIX compliant system
It is a plug-in for OpenLDAP directory server with ppolicy overlay. 
