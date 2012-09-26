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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.AbstractCollector;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class TCPCollector extends AbstractCollector implements Runnable {

	private int port;

	private ServerSocket socket;

	private Thread thread;
	
	private void setPort(int port) throws Exception{
		this.port = port;
		if(started){
			stop();
			start();
		}
	}
	
	private volatile boolean started = false;

	public void start() throws Exception {
		if(socket != null){
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
	}

	public void stop() throws Exception {
		started = false;
		socket.close();
	}

	public void run(){
		while(started){
			try {
				Socket s = socket.accept();
				Object obj = readObject(new BufferedInputStream(s.getInputStream()));
				Data data = new Data(obj, "tcp-object");
				notifyDataArrival(data);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
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
