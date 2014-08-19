##### Password quality checker for OpenLDAP

Allows to control the quality of passwords before storing them in the directory.  
If the password mets configured parameters, it is accepted. It is rejected otherwise.  
The controlled parameters are:  
+ Number of required uppercase characters.
+ Number of required lowercase characters.
+ Number of required special characters.
+ Number of required digits.
+ List of forbidden characters.


It is a plug-in for OpenLDAP directory server with ppolicy overlay. 
