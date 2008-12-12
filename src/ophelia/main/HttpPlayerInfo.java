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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JOptionPane;

/**
 * this is just a skeleton-class for http-support in the future - just an idea
 * @author Tobias W. Kjeldsen
 */
public class HttpPlayerInfo implements Observer {

    private ServerSocket servSocket;
    private final int PORT = Settings.getInstance().getHttpPlayerInfoPort();
    /* data the socket outputs */
    private String playerInfo;

    public HttpPlayerInfo() {
        try {
            this.servSocket = new ServerSocket(PORT);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getStackTrace());
        }
        try {
            Socket client;
            while (true) {
                new Thread(new ClientHandler(servSocket.accept()));
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "HTTP request from client failed! " + ex.getMessage());
        }
    }

    public void update(Observable o, Object arg) {
        StringBuffer out = new StringBuffer();

    }

    class ClientHandler implements Runnable {

        private Socket client;

        public ClientHandler(Socket client) {
            this.client = client;
        }

        public void run() {
        }
    }
}
