SUBDIRS = src
dist_doc_DATA = README README.fr LICENSE LICENSE.fr
dist_man3_MANS = pqchecker.man
vardatdir = $(PARAMDIR)
frmandir=$(mandir)/fr/man3
nobase_dist_vardat_DATA = pqparams.dat
nobase_dist_frman_DATA = pqchecker.man.fr
ACLOCAL_AMFLAGS = -I m4
install-exec-hook: ; mkdir -p $(vardatdir) ; mkdir -p $(frmandir) ; cp -p pqchecker.man.fr $(frmandir)/pqchecker.3
uninstall-hook: ; rm -rf $(vardatdir) ; rm -rf $(docdir) ; rm -f $(frmandir)/pqchecker.3
