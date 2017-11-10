AC_INIT([pqchecker], [2.0.0], [abdelhamid@meddeb.net])
AC_CONFIG_HEADERS([config.h])
AC_ARG_VAR([PARAMDIR],AS_HELP_STRING([path to password quality parameters file location]))
AM_INIT_AUTOMAKE([-Wall -Werror foreign])
AM_PROG_AR
LT_INIT
AC_CONFIG_MACRO_DIR([m4])
AC_PROG_CC
AC_DEFINE_UNQUOTED([PARAMS_DATA_FILE],["$PARAMDIR/pqparams.dat"],["Parameters data file location"])
AC_CONFIG_FILES([
Makefile
src/Makefile
doc/Makefile
doc/fr/Makefile
])
AC_OUTPUT
