To generate detail_race.csv:
----------------------------

```
select detail_race_cde, detail_race.broad_race_cde from detail_race
```

To generate allele_code_map.csv:
--------------------------------

```
select ae1.search_typing, ae2.search_typing from antigen a1
join antigen_encoding ae1 on a1.antigen_eid = ae1.antigen_eid
join ALLELE_CODE_ALLELE_MAP acam on a1.antigen_eid = acam.antigen_1_eid
join antigen a2 on acam.antigen_2_eid = a2.antigen_eid
join antigen_encoding ae2 on a2.antigen_eid = ae2.antigen_eid
where a1.hla_locus_cde = 'HLA-DPB1'
and ae1.nomenclature_version = '3'
and ae2.nomenclature_version = '3'
order by ae1.search_typing, ae2.search_typing;
```

To generate race_freq.csv:
--------------------------

- Run prep-freqs.sh
- prepend `HLA-` to all loci: `sed -i .bak 's/,DPB1,/,HLA-DPB1,/' race-freq.csv`

To generate allele_group.csv:
-----------------------------

This data was manually extracted from the following manuscript:

> Crivello P, Zito L, Sizzano F, Zino E, Maiers M, Mulder A, Toffalori C, Naldini L, Ciceri F, Vago L, Fleischhauer K, The impact of amino acid variability on alloreactivity defines
> a functional distance predictive of permissive HLA-DPB1 mismatches in hematopoietic stem cell transplantation, Biology of Blood and Marrow Transplantation (2014), doi: 10.1016/j.bbmt.2014.10.017.

To view rowcounts:
------------------

```
sqlite3 epitopeservice.db ".tables" | xargs -n 1 -I{} sqlite3 epitopeservice.db "select '{}: ' || count(1) from {};"
```
