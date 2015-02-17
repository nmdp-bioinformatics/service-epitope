#!/bin/bash
if [ "Z$1" = "Z" ] ; then echo "Usage $0: <database>"; exit 1; fi
7z -so e allele_group.7z | sqlite3 -init <(cat <<'EOF'
drop table if exists allele_group;
create table allele_group (
	locus text,
	allele text,
	immune_group integer
);
.mode csv
.separator ","
delete from allele_group;
.import /dev/stdin allele_group
EOF
) $1
