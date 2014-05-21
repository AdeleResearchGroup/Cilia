/*
 * Copyright Adele Team LIG
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 */
package fr.liglab.adele.cilia.tcp;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.AbstractCollector;
import fr.liglab.adele.cilia.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ServerSocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class TCPCollector extends AbstractCollector implements Runnable {

    private int port;

    private ServerSocket socket;

    private Thread thread;

    private static final Logger log = LoggerFactory.getLogger(Const.LOGGER_APPLICATION);

    private void setPort(int port) throws Exception {
        this.port = port;
        if (started) {
            stop();
            start();
        }
    }

    private volatile boolean started = false;

    public void start() throws Exception {
        if (socket != null) {
            this.stop();
        }
        ServerSocketFactory serverSocketFactory = ServerSocketFactory
                .getDefault();
        // Now have the factory create the server socket. This is the last
        // change from the original program.
        socket = serverSocketFactory.createServerSocket(port);
        started = true;
        thread = new Thread(this);
        thread.start();
        log.debug("[TCPCollector] started");
    }

    public void stop() throws Exception {
        log.debug("[TCPCollector] started");
        started = false;
        socket.close();
    }

    public void run() {
        while (started) {
            try {
                Socket s = socket.accept();
                Object obj = readObject(new BufferedInputStream(s.getInputStream()));
                Data data = new Data(obj, "tcp-object");
                log.trace("[TCPCollector] message arrive ");
                notifyDataArrival(data);
            } catch (IOException e) {
                log.error("[TCPCollector] message arrive error", e);
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                log.error("[TCPCollector] message arrive error", e);
                e.printStackTrace();
            }

        }
    }

    private Object readObject(InputStream is) throws IOException, ClassNotFoundException {
        byte buffer[] = new byte[4096];
        byte complete[];
        int bytesRead;
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        while ((bytesRead = is.read(buffer)) > 0) {
            byteBuffer.write(buffer, 0, bytesRead);
        }
        byteBuffer.flush();
        complete = byteBuffer.toByteArray();
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(complete));
        return in.readObject();

    }
}
