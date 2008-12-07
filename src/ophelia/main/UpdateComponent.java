/* 
Ophelia, the cat! is a lightweight mediaplayer written in Java. The main goal 
is(and was) to create a very light and fast mediaplayer 
with the most wanted features.

Copyright (C) 2008 Tobias W. Kjeldsen; tobias@wkjeldsen.dk

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ophelia.main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author Tobias W. Kjeldsen
 */
public class UpdateComponent {

    private final String versionCheckURL = "http://wkjeldsen.dk/ophelia/Ophelia.version";

    public boolean isNewVersion() {
        try {
            URL url = new URL(versionCheckURL);
            URLConnection urlc = url.openConnection();
            DataInputStream in = new DataInputStream(urlc.getInputStream()); // To download
            BufferedReader read = new BufferedReader(new InputStreamReader(in));
            if (!read.readLine().equals(Settings.getInstance().getOpheliaVersion())) {
                return true;
            }
            read.close();
        } catch (Exception ex) {
            return false;
        }
        return false;
    }
}
