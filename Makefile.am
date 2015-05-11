SUBDIRS = src
dist_doc_DATA = README README.fr LICENSE LICENSE.fr
dist_man3_MANS = pqchecker.man
vardatdir = $(PARAMDIR)
nobase_dist_vardat_DATA = pqparams.dat pqchecker.man.fr
ACLOCAL_AMFLAGS = -I m4
install-exec-hook: ; mkdir -p $(vardatdir) ; cp -p pqparams.dat $(vardatdir) ; \
  mkdir -p $(mandir)/fr/man3 ; cp -p pqchecker.man.fr $(mandir)/fr/man3/pqchecker.3
#; mkdir -p $(docdir) ; cp -p README README.fr LICENSE LICENSE.fr $(docdir)
uninstall-hook: ; rm -rf $(vardatdir) ; rm -rf $(docdir) ; rm -f $(mandir)/fr/man3/pqchecker.3
