// ID3v2Frame.java
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 
 * This class contains one ID3v2 frame.
 * 
 * Note: ID3v2 frame does not now anything about unsynchronization. That is up
 * to higher level objects (i.e. ID3v2)
 */
public class ID3v2Frame {

    /** ******** Constructors ********* */

    /**
     * Creates a new ID3v2 frame
     * 
     * @param id
     *            Frame id
     * @param content
     *            Frame content. Must not be unsynchronized!
     * @param tag_alter_preservation
     *            True if frame should be discarded if frame id is unknown to
     *            software and tag is altered
     * @param file_alter_preservation
     *            Same as <code>tag_alter_preservation</code>, but applies if
     *            file (excluding tag) is altered
     * @param read_only
     *            True if frame should not be changed
     * @param compression_type
     *            Use contant from this class:
     *            <code>ID3v2Frame.NO_COMPRESSION</code>:
     *            <code>content</code> is not compressed and should not be
     *            compressed. <code>ID3v2Frame.IS_COMPRESSED</code>:
     *            <code>content</code> is already compressed
     *            <code>ID3v2Frame.DO_COMPRESS</code>:<code>content</code>
     *            is not compressed, but should be Compression can also be
     *            switched on/off with <code>setCompression</code>
     * @param encryption_id
     *            Encryption method or 0 if not encrypted (not completely
     *            supported, encryption must be done externally)
     * @param group
     *            Group of frames this frame belongs to or 0 if frame does not
     *            belong to any group
     * @exception ID3v2DecompressionException
     *                If content is compressed and decompresson fails
     */
    public ID3v2Frame(String id, byte[] content,
            boolean tag_alter_preservation,
            boolean file_alter_preservation, boolean read_only,
            byte compression_type, byte encryption_id, byte group)
            throws ID3v2DecompressionException {
        this.id = id;
        this.content = content;
        this.tag_alter_preservation = tag_alter_preservation;
        this.file_alter_preservation = file_alter_preservation;
        this.read_only = read_only;
        this.compression = (compression_type == DO_COMPRESS || compression_type == IS_COMPRESSED);
        this.encryption_id = encryption_id;
        this.group = group;

        if (compression_type == DO_COMPRESS) {
            // compress content
            decompressed_length = this.content.length;
            compressContent();
        } else if (compression_type == IS_COMPRESSED) {
            // decompress content
            compressed_content = content;
            decompressContent();
            decompressed_length = this.content.length;
        } else {
            // no compression
            decompressed_length = this.content.length;
            compressed_content = this.content;
        }
    }

    /**
     * Creates a new ID3v2 frame from a stream. Stream position must be set to
     * first byte of frame. Note: Encryption/Deencryption is not supported, so
     * content of encrypted frames will be returned encrypted. It is up to the
     * higher level routines to decompress it. Note^2: Compression/decompression
     * supports only GZIP.
     * 
     * @param in
     *            Stream to read from
     * @exception ID3v2DecompressionException
     *                If input is compressed and decompression fails
     * @exception IOException
     *                If I/O error occurs
     */
    public ID3v2Frame(InputStream in) throws IOException,
            ID3v2DecompressionException {
        //// read header
        byte[] head = new byte[10];
        in.read(head);

        // check if id is valid (no real check for errors, as you will see)
        if (head[0] == 0) {
            // id may not start with a 0
            // you may call it "error", I call it "padding"
            // so do not raise an exception, just inform user of this
            // instance by setting id = ID_INVALID
            this.id = ID_INVALID;
            return;
        }

        // decode id
        StringBuffer tmp = new StringBuffer(4);
        for (int i = 0; i < 4; i++) {
            tmp.append((char) (head[i] & 0xff));
        }
        this.id = tmp.toString();

        // decode size (needed to read content)
        int length = (int) (new de.vdheide.utils.Bytes(head, 4, 4))
                .getValue();

        // deocde flags
        if (((head[8] & 0xff) & FLAG_TAG_ALTER_PRESERVATION) > 0) {
            tag_alter_preservation = true;
        }
        if (((head[8] & 0xff) & FLAG_FILE_ALTER_PRESERVATION) > 0) {
            file_alter_preservation = true;
        }
        if (((head[8] & 0xff) & FLAG_READ_ONLY) > 0) {
            read_only = true;
        }
        if (((head[9] & 0xff) & FLAG_COMPRESSION) > 0) {
            compression = true;
        }
        boolean encryption = false;
        if (((head[9] & 0xff) & FLAG_ENCRYPTION) > 0) {
            encryption = true;
        }
        boolean grouping = false;
        if (((head[9] & 0xff) & FLAG_GROUPING) > 0) {
            grouping = true;
        }

        // additional bytes if present
        if (compression == true) {
            // read decompressed size
            byte[] decomp_byte = new byte[4];
            in.read(decomp_byte);
            decompressed_length = (int) (new de.vdheide.utils.Bytes(
                    decomp_byte)).getValue();

            // substract 4 bytes from length to get actual content length
            length -= 4;
        }

        if (encryption == true) {
            // read encryption type
            encryption_id = (byte) in.read();
            length--;
        }

        if (grouping == true) {
            // read group id
            group = (byte) in.read();

            // substract 1 byte from length to get actual content length
            length--;
        } else {
            group = 0;
        }

        //// read content
        content = new byte[length];
        in.read(content);

        // decompress if necessary
        if (compression == true) {
            compressed_content = new byte[content.length];
            System.arraycopy(content, 0, compressed_content, 0,
                    content.length);
            //	  compressed_content = content;
            decompressContent();
        }
    }

    /** ******** Public contants ********* */

    /**
     * No compression present or requested
     */
    public final static byte NO_COMPRESSION = 0;

    /**
     * Compression present
     */
    public final static byte IS_COMPRESSED = 1;

    /**
     * Compression requested
     */
    public final static byte DO_COMPRESS = 2;

    /**
     * Invalid ID
     */
    public final static String ID_INVALID = null;

    /**
     * Get the ID of this frame.
     * 
     * @return ID of frame
     */
    public String getID() {
        return id;
    }

    /**
     * Set the ID of this frame
     * 
     * @param id
     *            ID to set
     */
    public void setID(String id) {
        this.id = id;
    }

    /**
     * Determine whether the frame should be preserved if the tag is altered and
     * the frame ID is unknown to the altering application.
     * 
     * @return true if the frame should be preserved
     */
    public boolean getTagAlterPreservation() {
        return tag_alter_preservation;
    }

    /**
     * Determine whether the frame should be preserved if the tag is altered and
     * the frame ID is unknown to the altering application.
     * 
     * @param tag_alter_preservation
     *            true if the frame should be preserved
     */
    public void setTagAlterPreservation(boolean tag_alter_preservation) {
        this.tag_alter_preservation = tag_alter_preservation;
    }

    /**
     * Determine whether the frame should be preserved if the file but not the
     * frame is altered and the frame ID is unknown to the altering application.
     * 
     * @return true if the frame should be preserved
     */
    public boolean getFileAlterPreservation() {
        return file_alter_preservation;
    }

    /**
     * Determine whether the frame should be preserved if the file but not the
     * frame is altered and the frame ID is unknown to the altering application.
     * 
     * @param file_alter_preservation
     *            true if the frame should be preserved
     */
    public void setFileAlterPreservation(boolean file_alter_preservation) {
        this.file_alter_preservation = file_alter_preservation;
    }

    /**
     * Determine whether the frame is read-only
     * 
     * @return true if the frame is read-only
     */
    public boolean getReadOnly() {
        return read_only;
    }

    /**
     * Determine whether the frame is read-only
     * 
     * @param read_only
     *            true if the frame is read-only
     */
    public void setReadOnly(boolean read_only) {
        this.read_only = read_only;
    }

    /**
     * Determine whether the frame is compressed
     * 
     * @return true if the frame is compressed
     */
    public boolean getCompression() {
        return compression;
    }

    /**
     * Determine whether the frame should be compressed
     * 
     * @param compression
     *            true if the frame should be compressed
     */
    public void setCompression(boolean compression) {
        this.compression = compression;
    }

    /**
     * @return Encrytion ID or 0 if not encrypted
     */
    public byte getEncryptionID() {
        return encryption_id;
    }

    /**
     * Set the encryption ID to use.
     * 
     * @param encryption_id
     *            encryption ID to use or 0 for no encryption
     */
    public void setEncryption(byte encryption_id) {
        this.encryption_id = encryption_id;
    }

    /**
     * Get the group ID this frame belongs to
     * 
     * @return group ID or 0 if no group
     */
    public byte getGroup() {
        return group;
    }

    /**
     * Set the group ID this frame belongs to
     * 
     * @param group
     *            group ID or 0 if no group
     */
    public void setGroup(byte group) {
        this.group = group;
    }

    /**
     * Calculates the number of bytes necessary to store a byte representation
     * of this frame.
     * 
     * @return Number of bytes necessary for this frame
     */
    public int getLength() {
        // header: frame id (4 bytes), size (4 bytes), flags (2 bytes)
        // + content length
        int length = 10;

        // if compression is set, add 4 bytes for decompressed size
        if (compression == true) {
            length += 4;
        }

        // if encryption is set, add one byte for encryption id
        if (encryption_id != 0) {
            length++;
        }

        // if group is set, add one byte for group identifier
        if (group != 0) {
            length++;
        }

        // content
        if (compression == true) {
            length += compressed_content.length;
        } else {
            length += content.length;
        }

        return length;
    }

    /**
     * Returns content (decompressed)
     * 
     * @return Decompressed content
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Returns an array of bytes representing this frame
     * 
     * @return Content taking into account any flags
     */
    public byte[] getBytes() {
        // get length, this is used more than once, so store it
        int length = getLength();
        byte[] ret = new byte[length];

        //// write header
        // write id
        for (int i = 0; i < 4; i++) {
            if (id.length() < i - 1) {
                // this should not happen, all ids are 4 chars long...
                ret[i] = 0;
            } else {
                ret[i] = (byte) id.charAt(i);
            }
        }

        // write size
        byte[] size_byte = (new de.vdheide.utils.Bytes(length - 10, 4))
                .getBytes();
        System.arraycopy(size_byte, 0, ret, 4, 4);

        // write flags
        byte flag1 = 0;
        if (tag_alter_preservation == true) {
            flag1 = (byte) (flag1 | FLAG_TAG_ALTER_PRESERVATION);
        }
        if (file_alter_preservation == true) {
            flag1 += (byte) (flag1 | FLAG_FILE_ALTER_PRESERVATION);
        }
        if (read_only == true) {
            flag1 += (byte) (flag1 | FLAG_READ_ONLY);
        }
        ret[8] = flag1;

        byte flag2 = 0;
        if (compression == true) {
            flag2 += (byte) (flag2 | FLAG_COMPRESSION);
        }
        if (encryption_id != 0) {
            flag2 += (byte) (flag2 | FLAG_ENCRYPTION);
        }
        if (group > 0) {
            flag2 += (byte) (flag2 | FLAG_GROUPING);
        }
        ret[9] = flag2;

        short content_offset = 10; // first byte used for content

        // decompressed size, if compressed
        if (compression == true) {
            byte[] decomp_byte = (new de.vdheide.utils.Bytes(length, 4))
                    .getBytes();
            System.arraycopy(decomp_byte, 0, ret, content_offset, 4);

            content_offset += 4;
        }

        // encryption id if set
        if (encryption_id != 0) {
            ret[content_offset] = encryption_id;
            content_offset++;
        }

        // group id if set
        if (group > 0) {
            ret[content_offset] = group;
            content_offset++;
        }

        // content
        if (compression == true) {
            compressContent();
            System.arraycopy(compressed_content, 0, ret,
                    content_offset, compressed_content.length);
        } else {
            System.arraycopy(content, 0, ret, content_offset,
                    content.length);
        }

        return ret;
    }

    /**
     * Check whether this frame is equal to another frame.
     * 
     * Two frames are considered equal if the ids, the content and all flags are
     * equal.
     * 
     * @param other
     *            the other frame
     * @return true if both frames are equal
     */
    public boolean equals(Object other) {

        if (this == other) {
            return true;
        }

        if (!(other instanceof ID3v2Frame)) {
            return false;
        }

        ID3v2Frame otherFrame = (ID3v2Frame) other;

        // content equality cannot be tested with a simple
        // comparison (would compare identity of the byte arrays)
        if (!this.id.equals(otherFrame.id)
                || this.tag_alter_preservation != otherFrame.tag_alter_preservation
                || this.file_alter_preservation != otherFrame.file_alter_preservation
                || this.read_only != otherFrame.read_only
                || this.compression != otherFrame.compression
                || this.encryption_id != otherFrame.encryption_id
                || this.group != otherFrame.group
                || (this.content == null && otherFrame.content != null)
                || (this.content != null && otherFrame.content == null)
                || (this.content.length != otherFrame.content.length)) {
            return false;
        }

        if (this.content == null && otherFrame.content == null) {
            return true;
        }

        boolean equal = true;
        for (int i = 0; i < this.content.length; i++) {
            if (this.content[i] != otherFrame.content[i]) {
                equal = false;
                break;
            }
        }

        return equal;
    }

    /** ******** Private variables ********* */

    private String id = null;

    private boolean tag_alter_preservation = false;

    private boolean file_alter_preservation = false;

    private boolean read_only = false;

    private byte encryption_id = 0;

    private int decompressed_length = 0;

    private boolean compression = false;

    private byte group = 0;

    private boolean uses_unsynch = false;

    private byte[] content; // decompressed

    private byte[] compressed_content; // compressed

    private final static byte FLAG_TAG_ALTER_PRESERVATION = (byte) (1 << 7);

    private final static byte FLAG_FILE_ALTER_PRESERVATION = (byte) (1 << 6);

    private final static byte FLAG_READ_ONLY = (byte) (1 << 5);

    private final static byte FLAG_COMPRESSION = (byte) (1 << 7);

    private final static byte FLAG_ENCRYPTION = (byte) (1 << 6);

    private final static byte FLAG_GROUPING = (byte) (1 << 5);

    /** ******** Private methods ********* */

    /**
     * Compresses content
     */
    private void compressContent() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            GZIPOutputStream gout = new GZIPOutputStream(bout);

            // write (compress)
            gout.write(content, 0, content.length);
            gout.close();

            // write into compressed_content
            compressed_content = bout.toByteArray();

            // did compression really reduce size?
            if (content.length <= compressed_content.length) {
                compression = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            // how should this happen? We are writing to memory...
        }

    }

    /**
     * Decompresses content
     * 
     * @throws ID3v2DecompressionException
     */
    private void decompressContent() throws ID3v2DecompressionException {
        ByteArrayInputStream bin = new ByteArrayInputStream(
                compressed_content);

        try {
            GZIPInputStream gin = new GZIPInputStream(bin);

            // GZIPInputStream does not tell the array size needed to store the
            // decompressed array, so we write it byte by byte into a
            // ByteArrayOutputStream
            ByteArrayOutputStream bout = new ByteArrayOutputStream();

            int res = 0;
            while ((res = gin.read()) != -1) {
                bout.write(res);
            }

            content = bout.toByteArray();
        } catch (IOException e) {
            //throw new ID3v2DecompressionException(e.getMessage());
            throw new ID3v2DecompressionException();
        }
    }
}