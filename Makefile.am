SUBDIRS = src
doc_DATA = README README.fr LICENSE LICENSE.fr
dist_doc_DATA = README README.fr LICENSE LICENSE.fr
man3_MANS = pqchecker.man pqmessenger.man
dist_man3_MANS = pqchecker.man pqchecker.man.fr pqmessenger.man pqmessenger.man.fr
# vardatdir = $(localstatedir)
vardatdir = $(PARAMDIR)
nobase_dist_vardat_DATA = pqparams.dat
ACLOCAL_AMFLAGS = -I m4
install-exec-hook: ; mkdir -p $(mandir)/fr/man3 ; cp -p pqchecker.man.fr $(mandir)/fr/man3/pqchecker.3 ; \
  mkdir -p $(localedir)/fr/LC_MESSAGES ; cp po/fr/pqchecker.mo $(localedir)/fr/LC_MESSAGES
uninstall-hook: ; rm -f $(mandir)/fr/man3/pqchecker.3 ; rm -f $(localedir)/fr/LC_MESSAGES/pqchecker.mo
