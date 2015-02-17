#!/usr/bin/env perl

use strict;
use warnings;

use Data::Dumper;

# REST::Client and JSON can be installed from CPAN
# sudo cpan -i REST::Client
# sudo cpan -i JSON
#
use REST::Client;
use JSON;

my $service_url = 'http://epearsonone:8080';

my $request = {
  alleles => [
    "HLA-DPB1*13:01",
    "HLA-DPB1*105:01"
  ]
};

my $json_request = JSON::to_json($request);
print "REQUEST:\n$json_request\n";

my $client = REST::Client->new({
		host    => $service_url,
	});
$client->addHeader('Content-Type', 'application/json;charset=UTF-8');
$client->addHeader('Accept', 'application/json');

# Post
$client->POST('/alleles/', $json_request, {});

my $json_response = $client->responseContent;
my $response = JSON::from_json($json_response);

print "RESPONSE:\n";
print Dumper($response);
