# 'RCurl' package is used for accessing the webservice.
# Install RCurl package as:
# install.packages('RCurl')
library('RCurl')

# 'rsjon' package is used for converting between R and JSON structures. 
# Install rjson package as:
# install.packages('rjson')
library('rjson')

EPITOPE_SERVICE_HOST <- "epearsonone.nmdp.org"
EPITOPE_SERVICE_PORT <- "8080"

# build the complete URL
url <-  paste('http://', EPITOPE_SERVICE_HOST, ':', EPITOPE_SERVICE_PORT, '/alleles/', sep='')

dpb1Pairs <- list("HLA-DPB1*13:01", "HLA-DPB1*105:01")
request <- list(alleles = dpb1Pairs)
requestJSON <- rjson::toJSON(request)

# Sending JSON and will accept JSON
httpHeaders <- c('Accept' = "application/json",
                 'Content-Type' = 'application/json;charset=UTF-8')

# Send it to the server
reader <- basicTextGatherer()
status <- curlPerform(url = url,             
                      httpheader = httpHeaders,
                      postfields = requestJSON, 
                      writefunction = reader$update)

if(status == 0) {
  # Success
  responseJSON <- reader$value()
  response <- fromJSON(responseJSON)
  for(allele in response) {
    print(paste("Allele ", allele$allele, "is in group", allele$group))
  }
}

