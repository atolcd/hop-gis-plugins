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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The LAYER item in the TABLES section There is a static reader to read the
 * item in a DXF file and a toString method able to write it in a DXF form
 * 
 * @author Micha&euml;l Michaud
 */
public class DxfTABLE_LAYER_ITEM extends DxfTABLE_ITEM {
    private int colorNumber;
    private String lineType;

    public DxfTABLE_LAYER_ITEM(String name, int flags) {
        super(name, flags);
        this.colorNumber = 0;
        this.lineType = "DEFAULT";
    }

    public DxfTABLE_LAYER_ITEM(String name, int flags, int colorNumber, String lineType) {
        super(name, flags);
        this.colorNumber = colorNumber;
        this.lineType = lineType;
    }

    public String getLineType() {
        return lineType;
    }

    public void setLineType(String lineType) {
        this.lineType = lineType;
    }

    public int getcolorNumber() {
        return colorNumber;
    }

    public void setColorNumber(int colorNumber) {
        this.colorNumber = colorNumber;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Map readTable(RandomAccessFile raf) throws IOException {
        DxfTABLE_LAYER_ITEM item = new DxfTABLE_LAYER_ITEM("DEFAULT", 0);
        Map table = new LinkedHashMap();
        DxfGroup group = null;
        while (group != null && !group.equals(ENDTAB)) {
            group = DxfGroup.readGroup(raf);
            if (group.equals(LAYER)) {
                item = new DxfTABLE_LAYER_ITEM("DEFAULT", 0);
            } else if (group.getCode() == 2) {
                // System.out.println("\t\t" + group.getValue());
                item.setName(group.getValue());
                table.put(item.getName(), item);
            } else if (group.getCode() == 5) {
            } // tag appeared in version 13 of DXF
            else if (group.getCode() == 100) {
            } // tag appeared in version 13 of DXF
            else if (group.getCode() == 70) {
                item.setFlags(group.getIntValue());
            } else if (group.getCode() == 62) {
                item.setColorNumber(group.getIntValue());
            } else if (group.getCode() == 6) {
                item.setLineType(group.getValue());
            } else {
            }
        }
        return table;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(DxfGroup.toString(62, colorNumber));
        sb.append(DxfGroup.toString(6, lineType));
        return sb.toString();
    }

}
