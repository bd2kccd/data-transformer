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
import edu.pitt.dbmi.ccd.data.transformer.util.FileTool;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Discretization of Plink's dataset. The SNP alleles must be alphabetic in form
 * of A,C,G,T.
 *
 * Apr 12, 2016 1:27:25 PM
 *
 * @see <a href="http://pngu.mgh.harvard.edu/~purcell/plink/">Plink</a>
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class PedMapACGTDiscretizer implements DataTransformer {

    private static final Map<String, String> SNP_ALLELE_CODES = new HashMap<>();

    static {
        // missing
        SNP_ALLELE_CODES.put("00", "-1");

        // genotype
        SNP_ALLELE_CODES.put("AA", "0");
        SNP_ALLELE_CODES.put("AC", "1");
        SNP_ALLELE_CODES.put("AG", "2");
        SNP_ALLELE_CODES.put("AT", "3");
        SNP_ALLELE_CODES.put("CC", "4");
        SNP_ALLELE_CODES.put("CG", "5");
        SNP_ALLELE_CODES.put("CT", "6");
        SNP_ALLELE_CODES.put("GG", "7");
        SNP_ALLELE_CODES.put("GT", "8");
        SNP_ALLELE_CODES.put("TT", "9");

        // D = deletion and I = insertion
        SNP_ALLELE_CODES.put("DD", "10");
        SNP_ALLELE_CODES.put("DI", "11");
        SNP_ALLELE_CODES.put("II", "12");

        // reversed genotype
        SNP_ALLELE_CODES.put("CA", "1");
        SNP_ALLELE_CODES.put("GA", "2");
        SNP_ALLELE_CODES.put("TA", "3");
        SNP_ALLELE_CODES.put("GC", "5");
        SNP_ALLELE_CODES.put("TC", "6");
        SNP_ALLELE_CODES.put("TG", "8");
        SNP_ALLELE_CODES.put("ID", "11");
    }

    /**
     * Plink's PED file.
     *
     * @see
     * <a href="http://pngu.mgh.harvard.edu/~purcell/plink/data.shtml#ped">PED</a>
     */
    protected Path pedFile;

    /**
     * Plink's MAP file.
     *
     * @see
     * <a href="http://pngu.mgh.harvard.edu/~purcell/plink/data.shtml#map">MAP</a>
     */
    protected Path mapFile;

    protected boolean includeTargetVariable;

    public PedMapACGTDiscretizer(Path pedFile, Path mapFile, boolean includeTargetVariable) {
        this.pedFile = pedFile;
        this.mapFile = mapFile;
        this.includeTargetVariable = includeTargetVariable;
    }

    @Override
    public void writeAsTabular(PrintStream output, char delimiter) throws IOException {
        SnpVariable[] snpVariables = readInSnpVariables();
        discretizeVariables(snpVariables);

        // write header
        int lastIndex = snpVariables.length - 1;
        for (int i = 0; i < lastIndex; i++) {
            output.print(snpVariables[i].getName());
            output.print(delimiter);
        }
        output.println(snpVariables[lastIndex].getName());

        try (FileChannel fc = new RandomAccessFile(pedFile.toFile(), "r").getChannel()) {
            MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

            StringBuilder dataBuilder = new StringBuilder();
            String phenotype = "";
            int col = 0;
            int variableIndex = 0;
            byte currentChar = -1;
            byte prevChar = NEW_LINE;
            while (buffer.hasRemaining()) {
                currentChar = buffer.get();
                if (currentChar == CARRIAGE_RETURN) {
                    currentChar = NEW_LINE;
                }

                if (currentChar <= BLANK_SPACE) {
                    if (prevChar > BLANK_SPACE) {
                        col++;
                        if (col == 6) {
                            phenotype = dataBuilder.toString().trim();
                            dataBuilder.delete(0, dataBuilder.length());
                        } else if (col > 6) {
                            if (dataBuilder.length() == 2) {
                                String value = dataBuilder.toString();
                                dataBuilder.delete(0, dataBuilder.length());

                                output.print(snpVariables[variableIndex].getEncodedValue(SNP_ALLELE_CODES.get(value)));
                                if (variableIndex < lastIndex) {
                                    output.print(delimiter);
                                } else {
                                    output.println();
                                }
                                variableIndex++;
                            }
                        } else {
                            dataBuilder.delete(0, dataBuilder.length());
                        }

                        if (currentChar == NEW_LINE) {
                            if (variableIndex == lastIndex) {
                                output.println(phenotype);
                            }
                            col = 0;
                            variableIndex = 0;
                        }
                    }
                } else {
                    dataBuilder.append((char) currentChar);
                }

                prevChar = currentChar;
            }
            String value = dataBuilder.toString();
            dataBuilder.delete(0, dataBuilder.length());
            if (value.length() > 0) {
                if (value.length() == 2) {
                    output.print(snpVariables[variableIndex].getEncodedValue(SNP_ALLELE_CODES.get(value)));
                    if (variableIndex < lastIndex) {
                        output.print(delimiter);
                    } else {
                        output.println();
                    }
                    variableIndex++;
                }
            }
            if (variableIndex == lastIndex) {
                output.println(phenotype);
            }
        }
    }

    private void discretizeVariables(SnpVariable[] snpVariables) throws IOException {
        try (FileChannel fc = new RandomAccessFile(pedFile.toFile(), "r").getChannel()) {
            MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

            StringBuilder dataBuilder = new StringBuilder();
            int col = 0;
            int row = 0;
            int variableIndex = 0;
            byte currentChar = -1;
            byte prevChar = NEW_LINE;
            while (buffer.hasRemaining()) {
                currentChar = buffer.get();
                if (currentChar == CARRIAGE_RETURN) {
                    currentChar = NEW_LINE;
                }

                if (currentChar <= BLANK_SPACE) {
                    if (prevChar > BLANK_SPACE) {
                        col++;
                        if (col > 6) {
                            if (dataBuilder.length() == 2) {
                                String value = dataBuilder.toString();
                                dataBuilder.delete(0, dataBuilder.length());

                                String encodedValue = SNP_ALLELE_CODES.get(value);
                                if (encodedValue == null) {
                                    throw new IOException(String.format("Unknow value '%s' at line %d column %d.", encodedValue, row + 1, col));
                                } else {
                                    snpVariables[variableIndex].setValue(encodedValue);
                                    variableIndex++;
                                }
                            }
                        } else {
                            dataBuilder.delete(0, dataBuilder.length());
                        }
                    }
                    if (currentChar == NEW_LINE) {
                        col = 0;
                        variableIndex = 0;
                        row++;
                    }
                } else {
                    dataBuilder.append((char) currentChar);
                }

                prevChar = currentChar;
            }
        }

        for (SnpVariable snpVariable : snpVariables) {
            snpVariable.adjustMissingValue();
        }
    }

    private SnpVariable[] readInSnpVariables() throws IOException {
        int numOfVariables = includeTargetVariable ? FileTool.countNumberOfLines(mapFile) + 1 : FileTool.countNumberOfLines(mapFile);
        SnpVariable[] snpVariables = new SnpVariable[numOfVariables];
        int index = 0;

        try (FileChannel fc = new RandomAccessFile(mapFile.toFile(), "r").getChannel()) {
            MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

            StringBuilder dataBuilder = new StringBuilder();
            int col = 0;
            byte currentChar = -1;
            byte prevChar = NEW_LINE;
            while (buffer.hasRemaining()) {
                currentChar = buffer.get();
                if (currentChar == CARRIAGE_RETURN) {
                    currentChar = NEW_LINE;
                }

                if (currentChar <= BLANK_SPACE) {
                    if (prevChar > BLANK_SPACE) {
                        String value = dataBuilder.toString();
                        dataBuilder.delete(0, dataBuilder.length());
                        col++;

                        if (col == 2) {
                            snpVariables[index++] = new SnpVariable(value);
                        }
                    }
                    if (currentChar == NEW_LINE) {
                        col = 0;
                    }
                } else {
                    dataBuilder.append((char) currentChar);
                }

                prevChar = currentChar;
            }
        }

        if (includeTargetVariable) {
            SnpVariable targetVariable = new SnpVariable("phenotype");
            targetVariable.setTargetVariable(true);
            snpVariables[index++] = targetVariable;
        }

        return snpVariables;
    }

}
