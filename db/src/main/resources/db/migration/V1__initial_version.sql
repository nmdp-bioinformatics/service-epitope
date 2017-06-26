CREATE TABLE allele_group (
    allele_group_id integer primary key,
    locus text not null,
    allele text not null,
    immune_group integer not null,
    unique(locus, allele)
);
CREATE TABLE race_freq (
    race_freq_id integer primary key,
    locus text not null,
    detail_race text not null,
    allele text not null,
    frequency real not null,
    unique(locus, detail_race, allele)
);
CREATE TABLE detail_race (
    detail_race_id integer primary key,
    detail_race text not null,
    broad_race text not null,
    description text not null,
    unique(detail_race, broad_race)
);
CREATE TABLE allele_code (
    allele_code_id integer primary key,
    allele_code text not null,
    allele text not null,
    family_included boolean not null,
    unique(allele_code, allele)
);
CREATE TABLE hla_g_group (
    hla_g_group_id integer primary key,
    g_group text not null,
    locus text not null,
    allele not null,
    unique(g_group, locus, allele)
);
CREATE TABLE hla_p_group (
    hla_p_group_id integer primary key,
    p_group text not null,
    locus text not null,
    allele not null,
    unique(p_group, locus, allele)
);
CREATE TABLE hla_allele (
    hla_allele_id integer primary key,
    locus text not null,
    allele not null,
    unique(locus, allele)
);
CREATE TABLE dataset_date (        
    dataset_date_id text primary key,
    dataset text unique not null,
    modification_date integer not null
);
