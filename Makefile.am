SUBDIRS = src doc doc/fr
vardatdir = $(PARAMDIR)
ACLOCAL_AMFLAGS = -I m4
showwarning: ; @echo "-------------------------------------- WARNING !!  WARNING !! ---------------------------------------" \
             ; echo " $(vardatdir) and its content owner must be modified to the user executing slapd" \
             ; echo "------------------------------------------------------------------------------------------------------"
install-exec-hook: showwarning
