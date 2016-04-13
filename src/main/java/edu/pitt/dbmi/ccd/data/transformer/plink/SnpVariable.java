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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Apr 12, 2016 1:58:18 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class SnpVariable {

    public static final String MISSING_VALUE_ENCODING = "-1";

    private final String name;

    private final Map<String, String> encodedAlleles;

    private int encoding;

    private boolean targetVariable;

    public SnpVariable(String name) {
        this.name = name;
        this.encodedAlleles = new HashMap<>();
        this.encoding = 0;
        this.targetVariable = false;
    }

    @Override
    public String toString() {
        return "SnpVariable{" + "name=" + name + ", encodedAlleles=" + encodedAlleles + ", encoding=" + encoding + ", targetVariable=" + targetVariable + '}';
    }

    public String getName() {
        return name;
    }

    public int getEncoding() {
        return encoding;
    }

    public void setEncoding(int encoding) {
        this.encoding = encoding;
    }

    public boolean isTargetVariable() {
        return targetVariable;
    }

    public void setTargetVariable(boolean targetVariable) {
        this.targetVariable = targetVariable;
    }

    public String getEncodedValue(String value) {
        return encodedAlleles.get(value);
    }

    public void setValue(String value) {
        if (!encodedAlleles.containsKey(value)) {
            if (MISSING_VALUE_ENCODING.equals(value)) {
                encodedAlleles.put(value, null);
            } else {
                encodedAlleles.put(value, Integer.toString(++encoding));
            }
        }
    }

    public void adjustMissingValue() {
        if (encodedAlleles.containsKey(MISSING_VALUE_ENCODING)) {
            encodedAlleles.put(MISSING_VALUE_ENCODING, Integer.toString(++encoding));
        }
    }

}
