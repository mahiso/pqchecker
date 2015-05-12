SUBDIRS = src
dist_doc_DATA = README README.fr LICENSE LICENSE.fr
dist_man3_MANS = pqchecker.man
vardatdir = $(PARAMDIR)
frmandir=$(mandir)/fr/man3
nobase_dist_vardat_DATA = pqparams.dat
nobase_dist_frman_DATA = po/fr/pqchecker.man.fr
ACLOCAL_AMFLAGS = -I m4
showwarning:
showwarning: ; echo "----------------------------------- WARNING !!  WARNING !! -------------------------------------" \
             ; echo " You must modify $(vardatdir) and its content owner to the user executing slapd" \
             ; echo "------------------------------------------------------------------------------------------------"
install-exec-hook: showwarning
