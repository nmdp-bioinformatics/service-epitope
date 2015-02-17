#!/bin/bash
if [ "Z$1" = "Z" ] ; then echo "Usage $0: <database>"; exit 1; fi
7z -so e race_freq.7z | sqlite3 -init <(cat <<'EOF'
drop table if exists race_freq;
create table race_freq (
	detail_race text,
	locus text,
	allele text,
	frequency real
);
.mode csv
.separator ","
delete from race_freq;
.import /dev/stdin race_freq
EOF
) $1
