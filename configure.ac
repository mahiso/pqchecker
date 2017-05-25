AC_INIT([pqchecker], [2.0.0], [abdelhamid@meddeb.net])
AC_CONFIG_HEADERS([config.h])
AC_ARG_VAR([LDAPSRC],AS_HELP_STRING([path to openldap server source files root location]))
AC_ARG_VAR([JAVAHOME],AS_HELP_STRING([path to jdk root location]))
AC_ARG_VAR([PARAMDIR],AS_HELP_STRING([path to password quality parameters file location]))
AM_INIT_AUTOMAKE([-Wall -Werror foreign])
AM_PROG_AR
LT_INIT
AC_CONFIG_MACRO_DIR([m4])
AC_PROG_CC
AC_CHECK_FILES([$LDAPSRC/include/ldap_config.hin $LDAPSRC/include/portable.hin],
                [],
                [AC_MSG_ERROR([OpenLDAP header source files are missing, install them and retry.])])
AC_CHECK_FILES([$LDAPSRC/include/ldap_config.h $LDAPSRC/include/portable.h],
               [],
               [AC_MSG_ERROR([Execute <./configure && make depend> on OpenLDAP sources to obtain usable config header files])])
AC_CHECK_FILES([$JAVAHOME/include/jni.h],
               [],
               [AC_MSG_ERROR([Java JNI header sources files are missing, please install the jdk and retry])])
AC_SUBST([CPPFLAGS],["-I$LDAPSRC/servers/slapd -I$LDAPSRC/include -I$JAVAHOME/include -I$JAVAHOME/include/linux"]) 
AC_CHECK_HEADER([slap.h],
                [],
                [AC_MSG_ERROR([Something is wrong with header files !!])],
                [#include <portable.h>
                ])
AC_DEFINE_UNQUOTED([PARAMS_DATA_FILE],["$PARAMDIR/pqparams.dat"],["Parameters data file location"])
AC_CONFIG_FILES([
Makefile
src/Makefile
doc/Makefile
doc/fr/Makefile
])
AC_OUTPUT
