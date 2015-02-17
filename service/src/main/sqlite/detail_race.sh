#!/bin/bash
if [ "Z$1" = "Z" ] ; then echo "Usage $0: <database>"; exit 1; fi
7z -so e detail_race.7z | sqlite3 -init <(cat <<'EOF'
drop table if exists detail_race;
create table detail_race (
	detail_race text,
	broad_race text,
	description text
);
.mode csv
.separator ","
delete from detail_race;
.import /dev/stdin detail_race
EOF
) $1
