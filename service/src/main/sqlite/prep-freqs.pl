#!/usr/bin/perl -w

foreach (@ARGV) {
	/\/([^\/.]*).freqs$/ || die "invalid filename: $_";
	$race = $1;
	open FILE, $_ || die "can't open file: $_";
	while (<FILE>) {
		if (/~([^,]*),[^,]*,([^,]*),/) { $freqs{$1} += $2; }
	}
	foreach (sort keys %freqs) {
		@_ = split (/\*/, $_);
		print "$race,$_[0],$_[1],$freqs{$_}\n";
	}
}
