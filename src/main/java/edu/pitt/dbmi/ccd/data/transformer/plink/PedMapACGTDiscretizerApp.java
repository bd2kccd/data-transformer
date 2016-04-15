/*
 * Copyright (C) 2016 University of Pittsburgh.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package edu.pitt.dbmi.ccd.data.transformer.plink;

import edu.pitt.dbmi.ccd.data.transformer.DataTransformer;
import edu.pitt.dbmi.ccd.data.transformer.util.Args;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * Apr 14, 2016 3:54:48 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class PedMapACGTDiscretizerApp {

    private static final Options MAIN_OPTIONS = new Options();

    static {
        Option requiredOption = new Option("f", "file", true, "Plink's ped/map filename.");
        requiredOption.setRequired(true);
        MAIN_OPTIONS.addOption(requiredOption);

        MAIN_OPTIONS.addOption("d", "delimiter", true, "Data delimiter either comma, semicolon, space, colon, or tab. Default is tab.");
        MAIN_OPTIONS.addOption(null, "include-target", false, "Include target variable in dataset. It is excluded by default.");
        MAIN_OPTIONS.addOption("o", "out", true, "Output directory.");
    }

    private static String file;
    private static char delimiter;
    private static boolean includeTargetVariable;

    private static Path dirOut;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            Args.showHelp("plink", MAIN_OPTIONS, -1);
            return;
        }

        try {
            CommandLineParser cmdParser = new DefaultParser();
            CommandLine cmd = cmdParser.parse(MAIN_OPTIONS, args);
            file = cmd.getOptionValue("file");
            delimiter = Args.getDelimiterForName(cmd.getOptionValue("delimiter", "tab"));
            includeTargetVariable = cmd.hasOption("include-target");
            dirOut = Args.getPathDir(cmd.getOptionValue("out", "."), false);
        } catch (ParseException | FileNotFoundException exception) {
            System.err.println(exception.getLocalizedMessage());
            Args.showHelp("plink", MAIN_OPTIONS, -1);
            System.exit(-127);
        }

        boolean fileNotExist = false;
        Path pedFile = Paths.get(file + ".ped");
        if (Files.notExists(pedFile)) {
            System.err.printf("Missing file '%s'.%n", pedFile.getFileName().toString());
            fileNotExist = true && fileNotExist;
        }
        Path mapFile = Paths.get(file + ".map");
        if (Files.notExists(mapFile)) {
            System.err.printf("Missing file '%s'.%n", mapFile.getFileName().toString());
            fileNotExist = true && fileNotExist;
        }
        if (fileNotExist) {
            System.exit(-128);
        }

        Path outputFile = Paths.get(dirOut.toString(), Paths.get(file).getFileName().toString() + ".txt");
        try (PrintStream writer = new PrintStream(new BufferedOutputStream(Files.newOutputStream(outputFile, StandardOpenOption.CREATE)))) {
            DataTransformer dataTransformer = new PedMapACGTDiscretizer(pedFile, mapFile, includeTargetVariable);
            dataTransformer.writeAsTabular(writer, delimiter);
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
            System.exit(-128);
        }
    }

}
