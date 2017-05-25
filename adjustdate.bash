#! /bin/bash
#
# set last modify timestamp of m4 files to early date than
# configuration files
FROMDATE=$(stat configure | grep -i ^change | cut -d ' ' -f2,3)
EPFROM=$(date --date "$FROMDATE" +%s)
EPTO=$(expr $EPFROM - 86400)
TODATE=$(date --date "@$EPTO" "+%y%m%d%H%M")
for f in $(ls *.m4)
do
  touch -t $TODATE $f
done 
for f in $(ls *.am)
do
  touch -t $TODATE $f
done 
if [ -d m4 ]; then
  for f in $(ls m4/*.m4)
  do
    touch -t $TODATE $f
  done 
fi       
if [ -d src ]; then
  for f in $(ls src/*.am)
  do
    touch -t $TODATE $f
  done 
fi       
touch -t $TODATE configure.ac
