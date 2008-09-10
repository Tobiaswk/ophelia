// Bytes.java
//
//$Id$
//
//de.vdheide.uitls: A collection of helper classes and methods.
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

package de.vdheide.utils;

/**
 * Takes an array of bytes (e.g. read from an InputStream) and converts it into
 * a number, assuming MSB order.
 */
public class Bytes {
    /**
     * Create a new object from an array of bytes and convert to number. Result
     * can be read by <code>getValue()</code>.
     * 
     * @param in
     *            Array to convert.
     */
    public Bytes(byte[] in) {
        this(in, 0, in.length);
    }

    /**
     * Create a new object from an array of bytes and convert to a number.
     * Result can be read by <code>getValue()</code>.
     * 
     * @param in
     *            Array to convert.
     * @param start
     *            First index in <code>in</code> to read
     * @param length
     *            Number of bytes to read
     */
    public Bytes(byte[] in, int start, int length) {
        value = byteArrayToLong(in, start, length);
    }

    /**
     * Create a new object from a long value und convert to array of bytes.
     * Result can be read with <code>getBytes()</code>.
     * 
     * @param in
     *            Value to convert
     */
    public Bytes(long in) {
        bytes = longToByteArray(in, -1);
    }

    /**
     * Create a new object from a long value und convert to array of bytes.
     * Result can be read with <code>getBytes()</code>.
     * 
     * @param in
     *            Value to convert
     * @param len
     *            Length of resulting byte array
     */
    public Bytes(long in, int len) {
        bytes = longToByteArray(in, len);
    }

    /**
     * @return long representation of input
     */
    public long getValue() {
        return value;
    }

    /**
     * @return bytes representation of input
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * Convert <code>length</code> bytes from an array of bytes starting at
     * position <code>start</code> to a long value.
     * 
     * @param in
     *            Array of bytes to convert
     * @param start
     *            First index in <code>in</code> to read
     * @param length
     *            Number of bytes to read
     * @return converted value
     */
    public static long byteArrayToLong(byte[] in, int start, int length) {
        long value = 0;

        for (int i = start; i < (start + length); i++) {
            // move each byte (length-pos-1)*8 bits to the left and add them
            value += (long) ((in[i] & 0xff) << ((length - i + start - 1) * 8));
        }

        return value;
    }

    /**
     * Convert long value to array of bytes.
     * 
     * @param in
     *            Long value to convert
     * @param len
     *            Length of resulting byte array. <code>-1</code> for minimum
     *            length needed.
     * @return Newly created array of bytes with enough elements to hold the
     *         input First entry contains the MSB.
     */
    public static byte[] longToByteArray(long in, int len) {
        if (len == -1) {
            // get length of result array (log2 n bits => log2 n / 8 Bytes)
            len = (int) (Math.ceil(Math
                    .ceil(Math.log(in) / Math.log(2)) / 8));
        }

        byte[] res = new byte[len];

        long act = in;
        for (int i = 0; i < len; i++) {
            // move now handled byte to the right
            res[i] = (byte) (act >> ((len - i - 1) * 8));

            // and remove all bytes to the left
            res[i] = (byte) (res[i] & 0xff);
        }

        return res;
    }

    private long value;

    private byte[] bytes;

}