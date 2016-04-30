SUBDIRS = src doc doc/fr
vardatdir = $(PARAMDIR)
ACLOCAL_AMFLAGS = -I m4
showwarning: ; @echo "-------------------------------------- WARNING !!  WARNING !! ---------------------------------------" \
             ; echo " To complete the installation.." \
             ; echo " The folder $(vardatdir) and its content owner must be modified to the user executing slapd" \
             ; echo "------------------------------------------------------------------------------------------------------" \
             ; mkdir -p $(vardatdir) \
             ; cp pqparams.dat $(vardatdir) 
install-exec-hook: showwarning
uninstall-hook: ; rm -rf $(vardatdir)
