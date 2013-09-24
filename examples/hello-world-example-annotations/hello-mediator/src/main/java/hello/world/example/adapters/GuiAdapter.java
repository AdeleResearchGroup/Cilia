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
package hello.world.example.adapters;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.annotations.IOAdapter;
import fr.liglab.adele.cilia.annotations.Port;
import fr.liglab.adele.cilia.framework.AbstractIOAdapter;
import fr.liglab.adele.cilia.util.TimeoutException;
import hello.world.example.data.ContentData;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 *
 */
@IOAdapter(name = "gui-adapter", namespace = "hello.world.example",
        in_port = @Port(name= "in", type = "*"),
        out_port = @Port(name = "out", type = "*")
)
public class GuiAdapter extends AbstractIOAdapter{

    JFrame frame;
    JTextField textField;
    JLabel reponse;
    JButton collectBtn;

    /**
	 * @param context
	 */
	public GuiAdapter(BundleContext context) {
		super(context);
	}



    /**
     * Use iPOJO annotations.
     * Callback to be called when the component goes to the valid state.
     */
    @Validate
	public void start() {
		frame = new JFrame("hello-world-example");
		initComponents();
		frame.setVisible(true);
	}

    /**
     * Use iPOJO annotations.
     * Callback to be called when the component goes to the invalid state.
     */
    @Invalidate
	public void stop() {
		if (frame != null) {
			frame.dispose();
			frame = null;			
		}		
	}
	
	
	private void initComponents() {
		frame.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setPreferredSize(new Dimension(400,70));
		frame.setSize(400, 70);
		frame.setLocationRelativeTo(null);
		
		textField = new JTextField();
		reponse = new JLabel();
		collectBtn = new JButton("Send to mediator");
		collectBtn.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				String msg = textField.getText();
				String resultData = "";
				if (msg != null & msg.trim().length()>0) {
					Data data = new Data(createComplexData(textField.getText()), "guiconsole-data");
					textField.setText("");
					Data result = null;
					try {
						result = invokeChain(data);
					} catch (TimeoutException e1) {
						e1.printStackTrace();
					}
					System.out.println(result);
					if (result != null) {
						ContentData response = (ContentData)(result.getContent());
                        resultData = String.valueOf(response.get("Response"));
					}
					reponse.setText(String.valueOf(resultData));

				}
			}
		});
		
		frame.setLayout(new BorderLayout());
		
		frame.add(textField, BorderLayout.CENTER);
		frame.add(collectBtn, BorderLayout.EAST);
		frame.add(reponse, BorderLayout.PAGE_END);

       frame.getRootPane().setDefaultButton(collectBtn);
	}
	
	public void receiveData(Data data){
		super.receiveData(data);
	}

	public Data dispatchData(Data data) {
		return super.dispatchData(data);
	}

    public ContentData createComplexData(String value){
        ContentData data = new ContentData(value);
        System.out.println("User ! " + System.getenv("USER"));
        String user = System.getenv("USER") != null ? System.getenv("USER") : "unknown" ;
        data.put("user.name", user);
        return data;
    }

}
