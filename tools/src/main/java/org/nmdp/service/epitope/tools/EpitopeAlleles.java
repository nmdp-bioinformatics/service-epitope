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
import java.net.URL;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import org.dishevelled.commandline.ArgumentList;
import org.dishevelled.commandline.CommandLine;
import org.dishevelled.commandline.CommandLineParseException;
import org.dishevelled.commandline.CommandLineParser;
import org.dishevelled.commandline.Switch;
import org.dishevelled.commandline.Usage;
import org.dishevelled.commandline.argument.FileArgument;
import org.dishevelled.commandline.argument.IntegerListArgument;
import org.dishevelled.commandline.argument.StringArgument;
import org.dishevelled.commandline.argument.StringListArgument;
import org.dishevelled.commandline.argument.URLListArgument;
import org.nmdp.service.epitope.tools.About;
import org.nmdp.service.epitope.resource.AlleleView;

/**
 * Epitope alleles command line tool.
 */
public final class EpitopeAlleles extends AbstractEpitopeTool {
    private final String alleles;
    private final String alleleUris;
    private final String groups;
    static final String USAGE = "epitope-alleles -u " + DEFAULT_ENDPOINT_URL + " -e HLA-DPB1*01:01:01";

    public EpitopeAlleles(final String endpointUrl, final List<String> alleles, final List<URL> alleleUris, final List<Integer> groups, final File inputFile, final File outputFile) {
        super(endpointUrl, inputFile, outputFile);
        this.alleles = (alleles == null || alleles.isEmpty()) ? null : Joiner.on(",").join(alleles);
        this.alleleUris = (alleleUris == null || alleleUris.isEmpty()) ? null : Joiner.on(",").join(alleleUris);
        this.groups = (groups == null || groups.isEmpty()) ? null : Joiner.on(",").join(groups);
    }

    @Override
    public Integer call() {
        BufferedReader reader = null;
        PrintWriter writer = null;
        try {
            reader = reader(getInputFile());
            writer = writer(getOutputFile());

            write(getEpitopeService().getAlleles(null, alleles, null, alleleUris, null, groups), writer);

            while (reader.ready()) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                write(getEpitopeService().getAlleles(line.trim(), null, null, null, null, null), writer);
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

    static void write(final List<AlleleView> results, final PrintWriter writer) throws IOException {
        for (AlleleView result : results) {
            StringBuilder sb = new StringBuilder();
            sb.append(result.getAllele());
            sb.append("\t");
            sb.append(result.getGroup());
            writer.println(sb.toString());
        }
    }

    public static void main(final String args[]) {
        Switch about = createAboutArgument();
        Switch help = createHelpArgument();
        StringArgument endpointUrl = createEndpointUrlArgument();
        StringListArgument alleles = new StringListArgument("e", "alleles", "one or more comma-separated alleles in GL String format", false);
        URLListArgument alleleUris = new URLListArgument("r", "allele-uris", "one or more comma-separated allele URIs", false);
        IntegerListArgument groups = new IntegerListArgument("g", "groups", "one or more comma-separated groups", false);
        FileArgument inputFile = new FileArgument("i", "input-file", "input file of alleles in GL String format, default stdin", false);
        FileArgument outputFile = new FileArgument("o", "output-file", "output file of tab-delimited allele views, default stdout", false);

        ArgumentList arguments = new ArgumentList(about, help, endpointUrl, alleles, alleleUris, groups, inputFile, outputFile);
        CommandLine commandLine = new CommandLine(args);

        EpitopeAlleles epitopeAlleles = null;
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

            epitopeAlleles = new EpitopeAlleles(endpointUrl.getValue(DEFAULT_ENDPOINT_URL), alleles.getValue(), alleleUris.getValue(), groups.getValue(), inputFile.getValue(), outputFile.getValue());
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
            System.exit(epitopeAlleles.call());
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
