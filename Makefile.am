SUBDIRS = src doc doc/fr
vardatdir = $(PARAMDIR)
ACLOCAL_AMFLAGS = -I m4
makedirs:    ; @mkdir -p $(vardatdir) \
             ; USR=`id -un ldap 2>/dev/null` \
             ; if [ -z $$USR ]; then \
                 USR=`id -un openldap 2>/dev/null` \
             ;   if [ -z $$USR ]; then \
                     echo "-------------------------------------- WARNING !!  WARNING !! ---------------------------------------" \
                   ; echo " To complete the installation.." \
                   ; echo " The folder $(vardatdir) and its content owner must be modified to the user executing slapd" \
                   ; echo "------------------------------------------------------------------------------------------------------" \
             ;   fi \
               fi \
             ; cp pqparams.dat $(vardatdir) \
             ; if [ ! -z $$USR ]; then \
                chown $$USR:$$USR -R $(vardatdir) \
             ; fi
install-exec-hook: makedirs
uninstall-hook: ; rm -rf $(vardatdir)
