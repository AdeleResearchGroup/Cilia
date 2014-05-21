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
import hello.world.example.data.MyData;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 */
@IOAdapter(name = "gui-adapter", namespace = "hello.world.example",
        in_port = @Port(name = "in", dataType = MyData.class),
        out_port = @Port(name = "out", dataType = MyData.class)
)
public class GuiAdapter extends AbstractIOAdapter {

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
        frame.setPreferredSize(new Dimension(400, 70));
        frame.setSize(400, 70);
        frame.setLocationRelativeTo(null);

        textField = new JTextField();
        reponse = new JLabel();
        collectBtn = new JButton("Send to mediator");
        collectBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String msg = textField.getText();
                String resultData = "";
                if (msg != null & msg.trim().length() > 0) {
                    //create a complex data.
                    MyData content = createComplexData(textField.getText());
                    //create a Data wrapper data.
                    Data<MyData> data = new Data<MyData>(content, "guiconsole-data");
                    //remove last message text.
                    textField.setText("");
                    Data<MyData> result = null;
                    try {
                        //call and wait for response.
                        result = invokeChain(data);
                    } catch (TimeoutException e1) {
                        e1.printStackTrace();
                    }
                    analyzeResponse(result);
                }
            }
        });

        frame.setLayout(new BorderLayout());

        frame.add(textField, BorderLayout.CENTER);
        frame.add(collectBtn, BorderLayout.EAST);
        frame.add(reponse, BorderLayout.PAGE_END);

        frame.getRootPane().setDefaultButton(collectBtn);
    }

    public void receiveData(Data data) {
        super.receiveData(data);
    }

    public Data dispatchData(Data data) {
        return super.dispatchData(data);
    }

    /**
     * Create a complex data to be sent to the chain.
     *
     * @param value the value used to create the data
     * @return the MyData object.
     */
    public MyData createComplexData(String value) {
        MyData data = new MyData(value);
        String user = System.getenv("USER") != null ? System.getenv("USER") : "unknown";
        data.put("user.name", user);
        return data;
    }

    /**
     * Analyse the result and put it in the gui label.
     *
     * @param result
     */
    public void analyzeResponse(Data<MyData> result) {
        String resultData = null;
        if (result != null) {
            MyData response = result.getContent();
            System.out.println("Creation data in: " + response.getCreationDate());
            System.out.println(result);
            resultData = String.valueOf(response.get("Response"));
        }
        reponse.setText(resultData);
    }
}
