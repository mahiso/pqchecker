lib_LTLIBRARIES = pqchecker.la
pqchecker_la_SOURCES = pqchecker.c pparamio.c pqcheck.c JNIGateway.c shmap.c comm.c net_meddeb_pqmessenger_JNIGateway.h shmap.h pparamio.h pqcheck.h comm.h
include_HEADERS = pqchecker.h
pqchecker_la_LDFLAGS = -module -avoid-version -shared -lrt
pqchecker_la_CPPFLAGS = -DLOCALEDIR=\"$(localedir)\"
ACLOCAL_AMFLAGS = -I m4
install-exec-hook: ; cd $(libdir) ; \
          mv -f pqchecker.so pqchecker.so.$(VERSION) ; \
          $(LN_S) -f pqchecker.so.$(VERSION) pqchecker.so ; \
          $(LN_S) -f pqchecker.so.$(VERSION) libpqchecker.so
uninstall-hook: ; cd $(libdir) ; \
          rm -f pqchecker.so pqchecker.so.$(VERSION) libpqchecker.so
