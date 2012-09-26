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
package fr.liglab.adele.cilia.tcp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.net.SocketFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.ISender;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
public class TCPSender implements ISender {

	private int port;

	private String hostname;

	private Socket socket;


	private void setPort(int port) throws Exception{
		this.port = port;
	}
	private void setHostname(String hostname) throws Exception {
		this.hostname = hostname;
	}

	private void checkSocket() throws Exception {
		stopSocket();
		if(socket == null){
			SocketFactory factory = SocketFactory.getDefault();
			socket = factory.createSocket(hostname, port);
		}
	}

	private void stopSocket()  {
		if(socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		socket = null;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.framework.ISender#send(fr.liglab.adele.cilia.Data)
	 */
	public boolean send(Data data) {
		try {
			checkSocket();
			Object content = data.getContent();
			if (content != null) {
				try {
					byte[] serialized = serializeObject(content);
					OutputStream os = socket.getOutputStream();
					os.write(serialized);
					os.flush();
					return true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return false;
		} catch (Exception e1) {
			e1.printStackTrace();
			return false;
		} finally{
			stopSocket();
		}
	}


	private byte[] serializeObject(Object message) throws IOException {
		// If already in the right format
		if (message instanceof byte[]) {
			return (byte[]) message;
		}
		// If not, serialize it by hand
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);

		oos.writeObject(message);

		return baos.toByteArray();
	}
}
