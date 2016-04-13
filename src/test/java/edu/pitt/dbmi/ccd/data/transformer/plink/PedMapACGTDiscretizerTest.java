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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * Apr 12, 2016 3:51:33 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class PedMapACGTDiscretizerTest {

    public PedMapACGTDiscretizerTest() {
    }

    @Test
    public void test() throws IOException {
        Path pedFile = Paths.get("test", "data", "plink", "extra.ped");
        Path mapFile = Paths.get("test", "data", "plink", "extra.map");
        boolean includeTargetVariable = true;

        DataTransformer transformer = new PedMapACGTDiscretizer(pedFile, mapFile, includeTargetVariable);

        char delimiter = ',';
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream writer = new PrintStream(baos)) {
            transformer.writeAsTabular(writer, delimiter);
        }
        String content = baos.toString(Charset.defaultCharset().name());

        int expected = 3384;
        int actual = content.length();
        Assert.assertEquals(expected, actual);
    }

}
