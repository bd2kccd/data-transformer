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
package edu.pitt.dbmi.ccd.data.transformer.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 *
 * Apr 14, 2016 12:04:14 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class Args {

    private Args() {
    }

    public static Path getPathDir(String dir, boolean required) throws FileNotFoundException {
        Path path = Paths.get(dir);

        if (Files.exists(path)) {
            if (!Files.isDirectory(path)) {
                throw new FileNotFoundException(String.format("'%s' is not a directory.%n", dir));
            }
        } else {
            if (required) {
                throw new FileNotFoundException(String.format("Directory '%s' does not exist.%n", dir));
            }
        }

        return path;
    }

    public static String getOptionValue(String[] args, Option option) {
        Set<String> options = new HashSet<>();
        if (option.getOpt() != null) {
            options.add(option.getOpt());
        }
        if (option.getLongOpt() != null) {
            options.add(option.getLongOpt());
        }

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--") || arg.startsWith("-")) {
                if (arg.startsWith("--")) {
                    arg = arg.substring(2, arg.length());
                } else if (arg.startsWith("-")) {
                    arg = arg.substring(1, arg.length());
                }

                if (options.contains(arg)) {
                    i++;
                    if (i < args.length) {
                        return args[i];
                    }
                }
            }
        }

        return null;
    }

    public static String[] removeOption(String[] args, Option option) {
        Set<String> options = new HashSet<>();
        if (option.getOpt() != null) {
            options.add(option.getOpt());
        }
        if (option.getLongOpt() != null) {
            options.add(option.getLongOpt());
        }

        List<String> cmd = new LinkedList<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--") || arg.startsWith("-")) {
                if (arg.startsWith("--")) {
                    arg = arg.substring(2, arg.length());
                } else if (arg.startsWith("-")) {
                    arg = arg.substring(1, arg.length());
                }

                if (options.contains(arg)) {
                    if (option.hasArg()) {
                        i++;
                    }
                    continue;
                }
            }
            cmd.add(args[i]);
        }

        return cmd.toArray(new String[cmd.size()]);
    }

    public static char getDelimiterForName(String delimiter) {
        delimiter = (delimiter == null) ? "" : delimiter.toLowerCase();
        switch (delimiter) {
            case "comma":
            case ",":
                return ',';
            case "semicolon":
            case ";":
                return ';';
            case "space":
            case " ":
                return ' ';
            case "colon":
            case ":":
                return ':';
            case "tab":
            case "\t":
            default:
                return '\t';
        }
    }

    public static void showHelp(String type, Options options, int width) {
        StringBuilder sb = new StringBuilder("java -jar");
        try {
            JarFile jarFile = new JarFile(Args.class.getProtectionDomain().getCodeSource().getLocation().getPath(), true);
            Manifest manifest = jarFile.getManifest();
            Attributes attributes = manifest.getMainAttributes();
            String artifactId = attributes.getValue("Implementation-Title");
            String version = attributes.getValue("Implementation-Version");
            sb.append(String.format(" %s-%s.jar", artifactId, version));
        } catch (IOException exception) {
            sb.append(" causal-cmd.jar");
        }
        sb.append(" --type ");
        sb.append(type);

        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(width);
        formatter.printHelp(sb.toString(), options, true);
    }

}
