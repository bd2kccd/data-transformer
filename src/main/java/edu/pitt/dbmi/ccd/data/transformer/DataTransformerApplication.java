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
package edu.pitt.dbmi.ccd.data.transformer;

import edu.pitt.dbmi.ccd.data.transformer.plink.PedMapACGTDiscretizerApp;
import edu.pitt.dbmi.ccd.data.transformer.util.Args;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 *
 * Apr 14, 2016 11:22:30 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class DataTransformerApplication {

    public static final Options MAIN_OPTIONS = new Options();

    private static final Option typeOption = new Option("t", "type", true, "Choose one of the following: plink.");

    static {
        typeOption.setRequired(true);
        MAIN_OPTIONS.addOption(typeOption);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            showHelp();
            return;
        }

        String type = Args.getOptionValue(args, typeOption);
        if (type == null) {
            showHelp();
        } else {
            args = Args.removeOption(args, typeOption);
            switch (type) {
                case "plink":
                    PedMapACGTDiscretizerApp.main(args);
                    break;
                default:
                    System.err.printf("Unknown type: %s%n", type);
                    showHelp();
            }
        }
    }

    private static void showHelp() {
        String cmdLineSyntax = "java -jar ";
        try {
            JarFile jarFile = new JarFile(DataTransformerApplication.class.getProtectionDomain().getCodeSource().getLocation().getPath(), true);
            Manifest manifest = jarFile.getManifest();
            Attributes attributes = manifest.getMainAttributes();
            String artifactId = attributes.getValue("Implementation-Title");
            String version = attributes.getValue("Implementation-Version");
            cmdLineSyntax += String.format("%s-%s.jar", artifactId, version);
        } catch (IOException exception) {
            cmdLineSyntax += "data-transformer.jar";
        }

        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(-1);
        formatter.printHelp(cmdLineSyntax, MAIN_OPTIONS, true);
    }

}
