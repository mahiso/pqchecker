lib_LTLIBRARIES = pqchecker.la
pqchecker_la_SOURCES = pqchecker.c pparamio.c pqcheck.c JNIGateway.c net_meddeb_pqmessenger_JNIGateway.h pparamio.h pqcheck.h
include_HEADERS = pqchecker.h
pqchecker_la_LDFLAGS = -module -avoid-version -shared
pqchecker_la_CPPFLAGS = -DLOCALEDIR=\"$(localedir)\"
ACLOCAL_AMFLAGS = -I m4
install-exec-hook: ; cd $(libdir) ; \
          mv -f pqchecker.so pqchecker.so.$(VERSION) ; \
          $(LN_S) -f pqchecker.so.$(VERSION) pqchecker.so ; \
          $(LN_S) -f pqchecker.so.$(VERSION) libpqchecker.so
uninstall-hook: ; cd $(libdir) ; \
          rm -f pqchecker.so pqchecker.so.$(VERSION) libpqchecker.so
