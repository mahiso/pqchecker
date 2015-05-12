SUBDIRS = src doc doc/fr
vardatdir = $(PARAMDIR)
ACLOCAL_AMFLAGS = -I m4
showwarning: ; @echo "----------------------------------- WARNING !!  WARNING !! -------------------------------------" \
             ; @echo " You must modify $(vardatdir) and its content owner to the user executing slapd" \
             ; @echo "------------------------------------------------------------------------------------------------"
install-exec-hook: showwarning
