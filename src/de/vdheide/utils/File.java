// File.java
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

package de.vdheide.utils;

/**
 * Various methods for file manipulation and access.
 * 
 * @author Jens Vonderheide <jens@vdheide.de>
 */
public class File {
    private File() {
    }

    /**
     * Copy from <tt>source</tt> to <tt>destination</tt>
     * 
     * @param source
     *            Source file
     * @param dest
     *            Destination file (no directory!)
     * @throws java.io.IOException
     */
    public static void copy(String source, String dest)
            throws java.io.IOException {
        java.io.FileInputStream in = null;
        java.io.FileOutputStream out = null;

        try {
            // Create streams
            in = new java.io.FileInputStream(source);
            out = new java.io.FileOutputStream(dest);

            // copy bytes. Use an array for increased performance
            byte[] buf = new byte[20480];
            int bytesRead;
            while (-1 != (bytesRead = in.read(buf, 0, buf.length))) {
                out.write(buf, 0, bytesRead);
            }
        } catch (java.io.IOException e) {
            throw e;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (java.io.IOException e2) {
            }
        }
    }

    /**
     * Create a uniquely named temporary file in the form XXXnnnnn.tmp.
     * 
     * @param id
     *            a string prepended on the file generated. Should you fail to
     *            delete it later, the id will help identify where it came from.
     *            null and "" also allowed.
     * 
     * @param near
     *            Directory to create file in. Can also be a file, then
     *            temporary file is created in same directory.
     *            <p>
     *            If null, one of these locations is used (sorted by
     *            preference):
     *            <ol>
     *            <li>/tmp</li>
     *            <li>/var/tmp
     *            <li>c:/temp</li>
     *            <li>c:/windows/temp</li>
     *            <li>/</li>
     *            <li>current directory</li>
     *            </ol>
     * 
     * @return a temporary File with a unique name in the form XXXnnnnn.tmp.
     * @throws java.io.IOException
     */
    public static java.io.File getTempFile(String id, java.io.File near)
            throws java.io.IOException {
        String prepend = (id != null) ? id : "";

        // Find location for temp file
        String temp_loc = null;

        if (near != null) {
            if (near.isDirectory()) {
                temp_loc = near.getAbsolutePath();
            } else {
                java.io.File tmp = new java.io.File(near
                        .getAbsolutePath());
                temp_loc = tmp.getParent();
            }
        }

        if (near == null
                || (near != null && checkTempLocation(temp_loc) == false)) {
            String pathSep = System.getProperty("path.separator");
            String fileSep = System.getProperty("file.separator");
            if (checkTempLocation(fileSep + "tmp") == true) {
                temp_loc = fileSep + "tmp";
            } else if (checkTempLocation(fileSep + "var" + fileSep
                    + "tmp") == true) {
                temp_loc = fileSep + "var" + fileSep + "tmp";
            } else if (checkTempLocation("c" + pathSep + fileSep
                    + "temp") == true) {
                temp_loc = "c" + pathSep + fileSep + "temp";
            } else if (checkTempLocation("c" + pathSep + fileSep
                    + "windows" + fileSep + "temp") == true) {
                temp_loc = "c" + pathSep + fileSep + "windows"
                        + fileSep + "temp";
            } else if (checkTempLocation(fileSep) == true) {
                temp_loc = fileSep;
            } else if (checkTempLocation(".") == true) {
                temp_loc = ".";
            } else {
                // give up
                throw new java.io.IOException(
                        "Could not find directory for temporary file");
            }
        }

        java.util.Random wheel = new java.util.Random(); // seeded from the
        // clock
        java.io.File tempFile = null;
        do {
            // generate random a number 10,000 .. 99,999
            int unique = (wheel.nextInt() & Integer.MAX_VALUE) % 90000 + 10000;
            tempFile = new java.io.File(temp_loc, prepend
                    + Integer.toString(unique) + ".tmp");
        } while (tempFile.exists());

        // We "finally" found a name not already used. Nearly always the first
        // time.
        // Quickly stake our claim to it by opening/closing it to create it.
        // In theory somebody could have grabbed it in that tiny window since
        // we checked if it exists, but that is highly unlikely.
        new java.io.FileOutputStream(tempFile).close();

        // debugging peek at the name generated.
        if (false) {
            System.out.println(tempFile.getCanonicalPath());
        }

        return tempFile;
    }

    /**
     * Checks if directory chosen exists and is writable
     * 
     * @param dir
     *            Directory to check
     * @return true if the directoy exists and is writable
     */
    private static boolean checkTempLocation(String dir) {
        java.io.File test = new java.io.File(dir);

        if (test.isDirectory() && test.canWrite()) {
            return true;
        } else {
            return false;
        }
    }

}