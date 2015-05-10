SUBDIRS = src
dist_doc_DATA = README README.fr LICENSE LICENSE.fr
dist_man3_MANS = pqchecker.man pqchecker.man.fr
# vardatdir = $(localstatedir)
vardatdir = $(PARAMDIR)
nobase_dist_vardat_DATA = pqparams.dat
ACLOCAL_AMFLAGS = -I m4
install-exec-hook: ; mkdir -p $(mandir)/fr/man3 ; cp -p pqchecker.man.fr $(mandir)/fr/man3/pqchecker.3 ; \
  cp -p pqchecker.man $(mandir) ; mkdir -p $(vardatdir) ; cp -p pqparams.dat $(vardatdir) ; mkdir -p $(docdir) ; \
  cp -p README README.fr LICENSE LICENSE.fr $(docdir)
uninstall-hook: ; rm -f $(mandir)/fr/man3/pqchecker.3 ; rm -f $(mandir)/pqchecker.man ; rm -f $(vardatdir)/pqparams.dat ; \
  rm -rf $(docdir)
