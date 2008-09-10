// ByteBuilder.java
//
//$Id$
//
//de.vdheide.mp3: Access MP3 properties, ID3 and ID3v2 tags
//Copyright (C) 1999-2004 Jens Vonderheide <jens@vdheide.de>
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Library General Public
//License as published by the Free Software Foundation; either
//version 2 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//Library General Public License for more details.
//
//You should have received a copy of the GNU Library General Public
//License along with this library; if not, write to the
//Free Software Foundation, Inc., 59 Temple Place - Suite 330,
//Boston, MA  02111-1307, USA.

package de.vdheide.mp3;

import java.io.ByteArrayOutputStream;

/**
 * This does the opposite of Parser, i.e. it takes Strings or byte array, parses
 * them and adds them to a byte array.
 * <p>
 * Text encoding is set with one of the constants:
 * <ul>
 * <li>NONE: ISO-8859-1 and no encoding byte
 * <li>ISO: ISO-8859-1 and encoding byte
 * <li>UNICODE: Unicode and encoding byte
 * </ul>
 */
public class ByteBuilder {
    /**
     * Creates a new instance
     * 
     * @param encoding
     *            Encoding to use (see above)
     */
    public ByteBuilder(byte encoding) {
        arr = new ByteArrayOutputStream();
        this.encoding = encoding;
        checkEncoding();
    }

    /**
     * Creates a new instance with an estimation of the size needed. It is most
     * efficient when this estimation is the real size, but it creates no error
     * if it is not.
     * <p>
     * Text encoding is set with one of the constants: NONE: ISO-8859-1 and no
     * encoding byte ISO: ISO-8859-1 and encoding byte UNICODE: Unicode and
     * encoding byte
     * 
     * @param encoding
     *            Encoding to use (see above)
     * @param size
     *            Size estimate
     */
    public ByteBuilder(byte encoding, int size) {
        arr = new ByteArrayOutputStream(size);
        this.encoding = encoding;
        checkEncoding();
    }

    /**
     * No encoding byte, assume ISO-8859-1
     */
    public final static byte NONE = -1;

    /**
     * Encoding ISO-8859-1
     */
    public final static byte ISO = 0;

    /**
     * Encoding Unicode
     */
    public final static byte UNICODE = 1;

    /**
     * Append a single byte.
     * 
     * @param put
     *            Byte to insert
     */
    public void put(byte put) {
        arr.write(put);
    }

    /**
     * Append the contents of a byte array.
     * 
     * @param put
     *            Byte array to insert
     */
    public void put(byte[] put) {
        arr.write(put, 0, put.length);
    }

    /**
     * Append a text according to the selected encoding
     * 
     * @param put
     *            Text to write
     */
    public void put(String put) {
        // encode string
        byte[] encoded = null;

        switch (encoding) {
        case NONE:
        case ISO:
            try {
                encoded = put.getBytes("ISO8859_1");
            } catch (java.io.UnsupportedEncodingException e) {
                // cannot happen
            }
            break;
        case UNICODE:
            try {
                encoded = put.getBytes("Unicode");
            } catch (java.io.UnsupportedEncodingException e) {
                // cannot happen
            }
        }

        try {
            arr.write(encoded);
        } catch (java.io.IOException e) {
            // How can this possibly happen?
        }
    }

    /**
     * Read contents as byte array
     * 
     * @return contents
     */
    public byte[] getBytes() {
        return arr.toByteArray();
    }

    protected ByteArrayOutputStream arr;

    protected byte encoding;

    /**
     * Check the selected encoding and write encoding byte if appropriate
     */
    protected void checkEncoding() {
        if (encoding == ISO) {
            arr.write(0);
        } else if (encoding == UNICODE) {
            arr.write(1);
        }
    }
}