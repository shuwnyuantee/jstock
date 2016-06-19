/*
 * JStock - Free Stock Market Software
 * Copyright (C) 2015 Yan Cheng Cheok <yccheok@yahoo.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.yccheok.jstock.gui.trading;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.yccheok.jstock.trade.DriveWealthAPI;

/**
 *
 * @author  Owner
 */
public class TradingJPanel extends javax.swing.JPanel {
    
    /** Creates new form TradingJPanel */
    public TradingJPanel() {  // Rectangle rec) {
        this.height = 500;  // rec.height;
        this.width = 500;   // rec.width;
        
        System.out.println("width: " + this.width + ", height: " + this.height);

        //this.setBounds(rec);
        initComponents();
    }
    
    public void initComponents() {
        // Javafx login form example: 
        // http://docs.oracle.com/javafx/2/get_started/form.htm
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                grid = new GridPane();
                grid.setAlignment(Pos.CENTER);
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(25, 25, 25, 25));

                scene = new Scene(grid, width, height);
                
                scene.getStylesheets().add(TradingJPanel.class.getResource("trading.css").toExternalForm());
                
                jfxPanel.setScene(scene);

                Text scenetitle = new Text("Drive Wealth Login");
                scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
                grid.add(scenetitle, 0, 0, 2, 1);

                Label userName = new Label("User Name:");
                grid.add(userName, 0, 1);

                TextField userTextField = new TextField();
                grid.add(userTextField, 1, 1);

                Label pw = new Label("Password:");
                grid.add(pw, 0, 2);

                PasswordField pwBox = new PasswordField();
                grid.add(pwBox, 1, 2);

                Button signInBtn = new Button("Sign in");
                HBox hbBtn = new HBox(10);

                hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
                hbBtn.getChildren().add(signInBtn);
                grid.add(hbBtn, 1, 4);
                
                // to display msg when Sign In successful
                final Text successText = new Text();
                grid.add(successText, 1, 6);
                
                signInBtn.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        Map<String, String> params = new HashMap<>();
                        params.put("username", userTextField.getText());
                        params.put("password", pwBox.getText());
                        
                        Task<DriveWealthAPI> task = new Task<DriveWealthAPI>() {
                            @Override protected DriveWealthAPI call() throws Exception {
                                System.out.println("Drive Wealth User Sign In....\n\n ");

                                // sleep in ms
                                //Thread.sleep(5000);
                                
                                DriveWealthAPI _api = new DriveWealthAPI(params);
                                DriveWealthAPI.User user = _api.user;

                                System.out.println("DriveWealth: username: " + userTextField.getText()
                                                    + ", pwd: " + pwBox.getText()
                                                    + ", sessionKey: " + user.sessionKey
                                                    + ", userID: " + user.userID
                                                    + ", commission: " + user.commissionRate);
                                return _api;
                            }
                        };
                        
                        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                            @Override
                            public void handle(WorkerStateEvent t) {
                                api = task.getValue();
                                
                                System.out.println("Successfully Sign In, userID: " + api.user.userID);
                                
                                // reenable "Sign In" button
                                signInBtn.setDisable(false);
                                
                                successText.setFill(Color.FIREBRICK);
                                successText.setText("Successfully Sign In, userID: " + api.user.userID);
                            }
                        });
                        
                        new Thread(task).start();
                        
                        // disable "Sign In" button
                        signInBtn.setDisable(true);
                    }
                });
            }
        });

        this.add(jfxPanel, BorderLayout.CENTER);
        this.setVisible(true);
    }
    
    private final JFXPanel jfxPanel = new JFXPanel();
    private GridPane grid = new GridPane();
    private Scene scene;
    DriveWealthAPI api;

    int width;
    int height;
    
    //private final Rectangle fullSize;
    //private final double sceneWidth;
    //private final double sceneHeight;

}
