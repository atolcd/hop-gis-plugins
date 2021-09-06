/*
 * Library name : dxf
 * (C) 2012 Micha&euml;l Michaud
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 *
 * michael.michaud@free.fr
 *
 */

package fr.michaelm.jump.drivers.dxf;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * DxfGroup is a group containing a dxf code and a dxf value.
 * The class contains several utils to read and write groups an to format data.
 * @author Micha&euml;l Michaud
 */
public class DxfGroup {
    // Write international decimal symbol (.), not the french one (,)
    private static final DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);
    private static final DecimalFormat[] decimalFormats = new DecimalFormat[]{
        new DecimalFormat("#0", dfs),
        new DecimalFormat("#0.0", dfs),
        new DecimalFormat("#0.00", dfs),
        new DecimalFormat("#0.000", dfs),
        new DecimalFormat("#0.0000", dfs),
        new DecimalFormat("#0.00000", dfs),
        new DecimalFormat("#0.000000", dfs),
        new DecimalFormat("#0.0000000", dfs),
        new DecimalFormat("#0.00000000", dfs),
        new DecimalFormat("#0.000000000", dfs),
        new DecimalFormat("#0.0000000000", dfs),
        new DecimalFormat("#0.00000000000", dfs),
        new DecimalFormat("#0.000000000000", dfs)};

    private int code = -1;
    private String value;
    private long address;
    
    public DxfGroup(int code, String value) {
        this.code = code;
        this.value = value;
    }
    
    public DxfGroup(String code, String value) {
        try {
            this.value = value;
            this.code = Integer.parseInt(code.trim());
        } 
        catch(NumberFormatException nfe) {
            System.out.println("Error reading group: \"" + code + "=" + 
                value + "\" is not a valid code/value pair");
        }
    }

    public int getCode() {return code;}
    public void setCode(int code) {this.code = code;}
    public String getValue() {return value;}
    public int getIntValue() {return Integer.parseInt(value.trim());}
    public float getFloatValue() {return Float.parseFloat(value.trim());}
    public double getDoubleValue() {return Double.parseDouble(value.trim());}
    public void setValue(String value) {this.value = value;}
    public long getAddress() {return address;}
    private void setAddress(long address) {this.address = address;}
    
    public boolean equals(Object other){
        if (other instanceof DxfGroup &&
            code==((DxfGroup)other).getCode() && 
            value.equals(((DxfGroup)other).getValue())) {
            return true;
        }
        else return false;
    }
    
    public String toString() {
        String codeString = "    " + Integer.toString(code);
        int stringLength = codeString.length();
        codeString = codeString.substring(stringLength-(code<1000?3:4), stringLength);
        return codeString + "\r\n" + value + "\r\n";
    }
    
    public void print(int indent) {
        for (int i = 0 ; i < indent ; i++) System.out.print(" ");
        System.out.println("" + code + " = " + value);
    }
    
    public boolean isValid() {
        return code >= 0;
    }
    
    public static String int34car(int code) {
        if (code<10) return "  " + Integer.toString(code);
        else if (code<100) return " " + Integer.toString(code);
        else return Integer.toString(code);
    }
    
    public static String int6car(int value) {
        String s = "     " + Integer.toString(value);
        return s.substring(s.length()-6, s.length());
    }
    
    public static String toString(int code, String value) {
        return int34car(code) + "\r\n" + value + "\r\n";
    }

    public static String toString(int code, int value) {
        return int34car(code) + "\r\n" + int6car(value) + "\r\n";
    }

    public static String toString(int code, float value, int decimalPartLength) {
        return int34car(code) + "\r\n" +
              decimalFormats[decimalPartLength].format((double)value) + "\r\n";
    }

    public static String toString(int code, double value, int decimalPartLength) {
        return int34car(code) + "\r\n" +
              decimalFormats[decimalPartLength].format(value) + "\r\n";
    }

    public static String toString(int code, Object value) {
        if (value instanceof String) {return toString(code, (String)value);}
        else if (value instanceof Integer) {return toString(code, ((Integer)value).intValue());}
        else if (value instanceof Float) {return toString(code, ((Float)value).floatValue(), 3);}
        else if (value instanceof Double) {return toString(code, ((Double)value).doubleValue(), 6);}
        else return toString(code, value.toString());
    }

    /**
     * Read a group from the current position in a RandomAccessFime.
     * @return a DxfGroup
     */
    public static DxfGroup readGroup(RandomAccessFile raf) throws IOException {
        long pos = raf.getFilePointer();
        DxfGroup dxfGroup = new DxfGroup(raf.readLine(), raf.readLine());
        if (dxfGroup != null) dxfGroup.setAddress(pos);
        return dxfGroup;
    }

}
