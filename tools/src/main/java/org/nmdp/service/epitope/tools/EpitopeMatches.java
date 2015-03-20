/*

    epitope-service  T-cell epitope group matching service for HLA-DPB1 locus.
    Copyright (c) 2014-2015 National Marrow Donor Program (NMDP)
    
    This library is free software; you can redistribute it and/or modify it
    under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation; either version 3 of the License, or (at
    your option) any later version.
    
    This library is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; with out even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
    License for more details.
    
    You should have received a copy of the GNU Lesser General Public License
    along with this library;  if not, write to the Free Software Foundation,
    Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA.
    
    > http://www.gnu.org/licenses/lgpl.html

*/

package org.nmdp.service.epitope.tools;

import static org.dishevelled.compress.Readers.reader;
import static org.dishevelled.compress.Writers.writer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.dishevelled.commandline.ArgumentList;
import org.dishevelled.commandline.CommandLine;
import org.dishevelled.commandline.CommandLineParseException;
import org.dishevelled.commandline.CommandLineParser;
import org.dishevelled.commandline.Switch;
import org.dishevelled.commandline.Usage;
import org.dishevelled.commandline.argument.FileArgument;
import org.dishevelled.commandline.argument.StringArgument;
import org.nmdp.service.epitope.tools.About;
import org.nmdp.service.epitope.domain.DetailRace;
import org.nmdp.service.epitope.resource.MatchRequest;
import org.nmdp.service.epitope.resource.MatchResponse;

import com.google.common.collect.ImmutableList;

/**
 * Epitope match probabilities command line tool.
 */
public final class EpitopeMatches extends AbstractEpitopeTool {
    private final String recipient;
    private final String recipientRace;
    private final String donor;
    private final String donorRace;
    private final String token;
    static final String USAGE = "epitope-matches -u " + DEFAULT_ENDPOINT_URL + " \\" + "\n" + "  -r HLA-DPB1*01:01:01+HLA-DPB1*01:01:02 -c CAU -d HLA-DPB1*02:01:02+HLA-DPB1*02:01:03 -n CAU";

    public EpitopeMatches(
    		final String endpointUrl, 
    		final String recipient, 
    		final String recipientRace, 
    		final String donor, 
    		final String donorRace, 
    		final String token, 
    		final File inputFile, 
    		final File outputFile) 
    {
        super(endpointUrl, inputFile, outputFile);
        this.recipient = recipient;
        this.recipientRace = recipientRace;
        this.donor = donor;
        this.donorRace = donorRace;
        this.token = token;
    }

    public DetailRace getDetailRace(String s) {
    	return s == null || s.equals("") ? null : DetailRace.valueOf(s);
    }
    
    @Override
    public Integer call() {
        BufferedReader reader = null;
        PrintWriter writer = null;
        try {
            reader = reader(getInputFile());
            writer = writer(getOutputFile());

            String r = recipient;
            DetailRace rr = getDetailRace(recipientRace);
            String d = donor;
            DetailRace dr = getDetailRace(donorRace);
            String t = token;
            if (r == null || rr == null || d == null || dr == null) {
                int lineNumber = 0;
                while (reader.ready()) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    String[] tokens = line.split("\t");
                    if (tokens.length < 4) {
                        throw new IOException("invalid input format at line " + lineNumber);
                    }
                    r = tokens[0];
                    rr = getDetailRace(tokens[1]);
                    d = tokens[2];
                    dr = getDetailRace(tokens[3]);
                    if (tokens.length == 5 && tokens[4] != null && !tokens[4].equals("")) { 
                    	t = tokens[4];
                    }
                    MatchRequest request = new MatchRequest(r, rr, d, dr, t);
                    write(getEpitopeService().getMatches(ImmutableList.of(request)), writer);
                    lineNumber++;
                }
            } else {
                MatchRequest request = new MatchRequest(r, rr, d, dr, token);
                write(getEpitopeService().getMatches(ImmutableList.of(request)), writer);
            }
            return 0;
        }
        catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
        finally {
            try {
                reader.close();
            }
            catch (Exception e) {
                // ignore
            }
            try {
                writer.close();
            }
            catch (Exception e) {
                // ignore
            }
        }
    }

    static void write(final List<MatchResponse> results, final PrintWriter writer) throws IOException {
        for (MatchResponse result : results) {
            StringBuilder sb = new StringBuilder();
            boolean delim = false;
            if (result.getRecipient() != null) {
	            if (delim) sb.append("\t");
	            delim = true;
	            sb.append(result.getRecipient());
            }
            if (result.getRecipientRace() != null) {
	            if (delim) sb.append("\t");
	            delim = true;
	            sb.append(result.getRecipientRace());
            }
            if (result.getDonor() != null) {
	            if (delim) sb.append("\t");
	            delim = true;
	            sb.append(result.getDonor());
            }
            if (result.getDonorRace() != null) {
	            if (delim) sb.append("\t");
	            delim = true;
	            sb.append(result.getDonorRace());
            }
            if (result.getToken() != null) {
	            if (delim) sb.append("\t");
	            delim = true;
            	sb.append(result.getToken());
            }
            if (result.getMatchGrade() != null) {
	            if (delim) sb.append("\t");
	            delim = true;
            	sb.append(result.getMatchGrade());
            }
            if (result.getMatchProbability() != null) {
	            if (delim) sb.append("\t");
	            delim = true;
	            sb.append(result.getMatchProbability());
            }
            if (result.getPermissiveMismatchProbability() != null) {
	            if (delim) sb.append("\t");
	            delim = true;
	            sb.append(result.getPermissiveMismatchProbability());
            }
            if (result.getHvgNonPermissiveMismatchProbability() != null) {
	            if (delim) sb.append("\t");
	            delim = true;
	            sb.append(result.getHvgNonPermissiveMismatchProbability());
            }
            if (result.getGvhNonPermissiveMismatchProbability() != null) {
	            if (delim) sb.append("\t");
	            delim = true;
	            sb.append(result.getGvhNonPermissiveMismatchProbability());
            }
            if (result.getUnknownProbability() != null) {
	            if (delim) sb.append("\t"); 
	            delim = true;
            	sb.append(result.getUnknownProbability());
            }
            writer.println(sb.toString());
        }
    }

    public static void main(final String args[]) {
        Switch about = createAboutArgument();
        Switch help = createHelpArgument();
        StringArgument endpointUrl = createEndpointUrlArgument();
        StringArgument recipient = new StringArgument("r", "recipient", "recipient genotype list", false);
        StringArgument recipientRace = new StringArgument("c", "recipient-race", "recipient race code", false);
        StringArgument donor = new StringArgument("d", "donor", "donor genotype list", false);
        StringArgument donorRace = new StringArgument("n", "donor-race", "donor race code", false);
        StringArgument token = new StringArgument("t", "token", "identifier token", false);
        FileArgument inputFile = new FileArgument("i", "input-file", 
        		"tab delim input file (recip gl, recip race, donor gl, donor race, token), (token optional), (default stdin)", false);
        FileArgument outputFile = new FileArgument("o", "output-file", 
        		"tab delim output file (rcpgl, rcp race, dnr gl, dnr race, tok, match grade, match prob, perm prob, hvg prob, gvh prob, unknown prob) (default stdout)", false);

        ArgumentList arguments = new ArgumentList(about, help, endpointUrl, recipient, recipientRace, 
        		donor, donorRace, token, inputFile, outputFile);
        CommandLine commandLine = new CommandLine(args);

        EpitopeMatches epitopeMatches = null;
        try
        {
            CommandLineParser.parse(commandLine, arguments);
            if (about.wasFound()) {
                About.about(System.out);
                System.exit(0);
            }
            if (help.wasFound()) {
                Usage.usage(USAGE, null, commandLine, arguments, System.out);
                System.exit(0);
            }
            epitopeMatches = new EpitopeMatches(
            		endpointUrl.getValue(DEFAULT_ENDPOINT_URL), 
            		recipient.getValue(), 
            		recipientRace.getValue(), 
            		donor.getValue(), 
            		donorRace.getValue(), 
            		token.getValue(), 
            		inputFile.getValue(), 
            		outputFile.getValue());
        }
        catch (CommandLineParseException e) {
            if (about.wasFound()) {
                About.about(System.out);
                System.exit(0);
            }
            if (help.wasFound()) {
                Usage.usage(USAGE, null, commandLine, arguments, System.out);
                System.exit(0);
            }
            Usage.usage(USAGE, e, commandLine, arguments, System.err);
            System.exit(-1);
        }
        catch (IllegalArgumentException e) {
            Usage.usage(USAGE, e, commandLine, arguments, System.err);
            System.exit(-1);
        }
        try {
            System.exit(epitopeMatches.call());
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
