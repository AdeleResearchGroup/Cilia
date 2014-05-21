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
package fr.liglab.adele.cilia.components.adapters;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.AbstractIOAdapter;
import fr.liglab.adele.cilia.util.TimeoutException;
import org.osgi.framework.BundleContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class GuiAdapter extends AbstractIOAdapter {

    /**
     * @param context
     */
    public GuiAdapter(BundleContext context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    JFrame frame;
    JTextField textField;
    JLabel reponse;
    JButton collectBtn;
    private String name;

    public void start() {
        frame = new JFrame(name);

        initComponents();
        frame.setVisible(true);
    }

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
                    Data data = new Data(textField.getText(), "guiconsole-data");
                    textField.setText("");
                    Data result = null;
                    try {
                        result = invokeChain(data);
                    } catch (TimeoutException e1) {
                        e1.printStackTrace();
                    }
                    System.out.println(result);
                    if (result != null) {
                        resultData = String.valueOf(result.getContent());
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

    public void receiveData(Data data) {
        super.receiveData(data);
    }

    public Data dispatchData(Data data) {
        return super.dispatchData(data);
    }

}
