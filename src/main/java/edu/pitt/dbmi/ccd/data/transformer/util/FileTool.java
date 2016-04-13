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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

/**
 *
 * Apr 12, 2016 4:14:59 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class FileTool {

    private static final byte NEW_LINE = '\n';

    private static final byte CARRIAGE_RETURN = '\r';

    private FileTool() {
    }

    public static int countNumberOfLines(Path file) throws IOException {
        int count = 0;
        try (FileChannel fc = new RandomAccessFile(file.toFile(), "r").getChannel()) {
            MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            byte prevChar = NEW_LINE;
            while (buffer.hasRemaining()) {
                byte currentChar = buffer.get();
                if (currentChar == CARRIAGE_RETURN) {
                    currentChar = NEW_LINE;
                }

                if (currentChar == NEW_LINE && prevChar != NEW_LINE) {
                    count++;
                }

                prevChar = currentChar;
            }

            // cases where file has no newline at the end of the file
            if (prevChar != NEW_LINE) {
                count++;
            }
        }

        return count;
    }

}
