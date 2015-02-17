#!/bin/bash
if [ "Z$1" = "Z" ] ; then echo "Usage $0: <database>"; exit 1; fi
7z -so e allele_code_map.7z | sqlite3 -init <(cat <<'EOF'
drop table if exists allele_code_map;
create table allele_code_map (
	locus text,
	allele_code text,
	allele text
);
.mode csv
.separator ","
delete from allele_code_map;
.import /dev/stdin allele_code_map
EOF
) $1
