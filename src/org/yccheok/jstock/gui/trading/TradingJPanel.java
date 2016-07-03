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

import com.google.gson.internal.LinkedTreeMap;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.JScrollPane;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import static javafx.concurrent.Worker.State.FAILED;
import static javafx.concurrent.Worker.State.SUCCEEDED;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.yccheok.jstock.trading.DriveWealthAPI;

/**
 *
 * @author  Owner
 */
public class TradingJPanel extends javax.swing.JPanel {
    
    public TradingJPanel() {
        initComponents();
    }
    
    public void initComponents() {
        // Javafx login form example: 
        // http://docs.oracle.com/javafx/2/get_started/form.htm
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // Tab Pane
                tabPane = new TabPane();
                tabPane.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);

                // create Sign In Tab
                SignIn signIn = new SignIn();
                signIn.createTab();

                scene = new Scene(tabPane);
                scene.getStylesheets().add(TradingJPanel.class.getResource("trading.css").toExternalForm());
                jfxPanel.setScene(scene);
                jfxPanel.setPreferredSize(new Dimension(500, 500));
            }
        });

        jScrollPane.getViewport().add(jfxPanel);        
        jScrollPane.setPreferredSize(new Dimension(500, 500));

        this.setLayout(new java.awt.GridLayout(0, 1, 5, 5));
        this.add(this.jScrollPane);
        
        this.setVisible(true);
    }

    
    
    public class SignIn {
        public SignIn () {}

        void createTab () {
            GridPane grid = new GridPane();
            grid.setAlignment(Pos.CENTER);
            grid.setHgap(10);
            grid.setVgap(30);
            grid.setPadding(new Insets(25, 25, 25, 25));
            grid.setId("grid");

            // Drive Wealth Logo as Title
            Label titleLabel = new Label();
            Image image = new Image(getClass().getResourceAsStream("drivewealth-logo.png"));
            titleLabel.setGraphic(new ImageView(image));
            titleLabel.setAlignment(Pos.CENTER);
            grid.add(titleLabel, 0, 0);
            GridPane.setHalignment(titleLabel, HPos.CENTER);

            // username field
            TextField userField = new TextField();
            userField.setPromptText("Username");
            grid.add(userField, 0, 1);

            // password field
            PasswordField pwdField = new PasswordField();
            pwdField.setPromptText("Password");
            grid.add(pwdField, 0, 2);

            // Sign In button
            Button signInBtn = new Button("Sign in");
            signInBtn.setId("green");
            signInBtn.setMaxWidth(Double.MAX_VALUE);
            signInBtn.setMaxHeight(Double.MAX_VALUE);
            signInBtn.setTextAlignment(TextAlignment.CENTER);
            // rowspan = 2
            grid.add(signInBtn, 0, 4, 1, 2);
            GridPane.setHalignment(signInBtn, HPos.CENTER);

            // Licence
            HBox licenceHBox = new HBox(0);
            Label licenceLabel = new Label("By signing in you agree to ");

            licenceLabel.setPrefHeight(30);
            this.licenceLink.setPrefHeight(30);
            HBox.setHgrow(licenceLabel, Priority.ALWAYS);
            HBox.setHgrow(this.licenceLink, Priority.ALWAYS);

            licenceHBox.getChildren().addAll(licenceLabel, this.licenceLink);
            // HBox max size will follow max size of all children nodes
            licenceHBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE );
            grid.add(licenceHBox, 0, 6);
            GridPane.setHalignment(licenceHBox, HPos.CENTER);


            // Sign In successful msg
            final Label successText = new Label();
            successText.setWrapText(true);
            grid.add(successText, 0, 7);


            // make components auto resize
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(50);
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().setAll(cc);

            RowConstraints rr = new RowConstraints();
            rr.setVgrow(Priority.ALWAYS);
            grid.getRowConstraints().setAll(rr, rr, rr, rr, rr, rr, rr);

            // Sign In Button action
            signInBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    Map<String, String> params = new HashMap<>();

                    successText.setVisible(false);
                    String username = userField.getText();
                    String pwd = pwdField.getText();

                    if (username.isEmpty() || pwd.isEmpty()) {
                        System.out.println("Please enter username and password.");

                        String welcomeStr = "Please enter username and password";
                        successText.setTextFill(Color.FIREBRICK);
                        successText.setText(welcomeStr);
                        successText.setVisible(true);

                        return;
                    }

                    params.put("username", username);
                    params.put("password", pwd);

                    Task< Map<String, Object> > task = new Task< Map<String, Object> >() {
                        @Override protected Map<String, Object> call() throws Exception {
                            System.out.println("Drive Wealth User Sign In....\n\n ");

                            DriveWealthAPI _api = new DriveWealthAPI(params);
                            DriveWealthAPI.User user = _api.user;

                            System.out.println("DriveWealth: username: " + username
                                                + ", pwd: " + pwd
                                                + ", sessionKey: " + user.sessionKey
                                                + ", userID: " + user.userID
                                                + ", commission: " + user.commissionRate);

                            Map<String, Object> result = new HashMap<>();
                            result.put("api", _api);
                            
                            // get account info
                            String userID = _api.user.userID;
                            String accountID = _api.user.practiceAccount.accountID;
                            if (userID != null && accountID != null) {
                                Map<String, Object> accBlotter = _api.accountBlotter(userID, accountID);              
                                result.put("accBlotter", accBlotter);
                                System.out.println("calling account Blotter DONE...");
                            }

                            return result;
                        }
                    };

                    task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent t) {
                            Map<String, Object> result = task.getValue();
                            api = (DriveWealthAPI) result.get("api");

                            if (api.user != null && api.getSessionKey() != null) {
                                System.out.println("Successfully Sign In, userID: " + api.user.userID);

                                DriveWealthAPI.Account acc = api.user.practiceAccount;
                                String welcomeStr;

                                if (acc == null) {
                                    System.out.println("No practice account, prompt for creating ??");
                                    welcomeStr = "Successfully Sign In, create practice account to start trading";

                                    /*
                                    Map<String, Object> params = new HashMap<>();
                                    params.put("userID", api.user.userID);
                                    acc = api.createPracticeAccount(params);
                                    */
                                } else {
                                    String accountNo =  acc.accountNo;
                                    String nickname = acc.nickname;
                                    Double cash = acc.cash;

                                    welcomeStr = "Start trading now with " + acc.nickname + ".\n AccountNo: " + acc.accountNo
                                        + "\n AccountID: " + acc.accountID + "\n Balance: " + acc.cash;
                                }
                                successText.setText(welcomeStr);
                            } else {
                                System.out.println("Sign In failed");
                                successText.setText("Sign In failed");
                            }

                            successText.setTextFill(Color.FIREBRICK);
                            successText.setVisible(true);
                            // reenable "Sign In" button
                            signInBtn.setDisable(false);

                            // create account summary tab
                            if (result.containsKey("accBlotter")) {
                                Map<String, Object> accBlotter = (HashMap) result.get("accBlotter");
                                
                                AccBlotter summary = new AccBlotter(accBlotter);
                                summary.createTab();
                                System.out.println("Account Blotter DONE....");
                            }
                        }
                    });

                    new Thread(task).start();

                    // disable "Sign In" button
                    signInBtn.setDisable(true);
                }
            });

            this.signInTab.setTooltip(new Tooltip("Sign In"));
            this.signInTab.setText("Sign In");
            this.signInTab.setContent(grid);
            this.signInTab.setClosable(false);
            tabPane.getTabs().add(this.signInTab);

            createLicenceTab();
        }
        
        void createLicenceTab () {
            // Load Drive Wealth's licence in new Tab with browser
            final WebView browser = new WebView();
            final WebEngine webEngine = browser.getEngine();
            
            this.licenceTab.setText("Drive Wealth's Terms of Use");
            this.licenceTab.setContent(browser);
            this.licenceTab.setClosable(true);

            final ProgressIndicator progressIn = new ProgressIndicator();
            progressIn.setMaxSize(15, 15);
            licenceTab.setGraphic(progressIn);


            this.licenceLink.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    webEngine.load("https://drivewealth.com/terms-of-use/");
                    licenceLink.setDisable(true);
                    tabPane.getTabs().add(licenceTab);
                }
            });

            webEngine.getLoadWorker().stateProperty().addListener(
                new javafx.beans.value.ChangeListener<Worker.State>() {
                    public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                        if (newState == SUCCEEDED || newState == FAILED) {
                            licenceLink.setDisable(false);

                            if (progressIn.isVisible() == true) {
                                progressIn.setVisible(false);
                            }
                            if (licenceTab.getGraphic() != null) {
                                licenceTab.setGraphic(null);
                            }

                            if (newState == FAILED) {
                                System.out.println("Failed loading licence page");
                                return;
                            }

                            SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
                            selectionModel.select(licenceTab);
                        }
                        System.out.println("Loading licence page status: " + newState);
                    }
                }
            );
        }
        
        public final Tab signInTab  = new Tab();
        public final Tab licenceTab = new Tab();
        private final Hyperlink licenceLink = new Hyperlink("Drive Wealth's Terms of Use");
    }

    public class AccBlotter {
        public AccBlotter (Map<String, Object> accBlotter) {
            this.accBlotter = accBlotter;
        }

        private String formatNumber(Double number) {
            final DecimalFormat df1 = new DecimalFormat("#.00");
            return df1.format(number);
        }
        
        public void createTab() {
            LinkedTreeMap<String, Object> equity    = (LinkedTreeMap) accBlotter.get("equity");
            LinkedTreeMap<String, Object> balance   = (LinkedTreeMap) accBlotter.get("cash");

            Double positionsValue   = (Double) equity.get("equityValue");
            Double cashBalance      = (Double) balance.get("cashBalance");
            Double cashForTrade     = (Double) balance.get("cashAvailableForTrade");
            Double cashForWithdraw  = (Double) balance.get("cashAvailableForWithdrawal");
            Double accountTotal     = (Double) cashBalance + (Double) positionsValue;

            System.out.println("Table: " + positionsValue + ", " + cashBalance + ", "
                    + cashForTrade + ", " + cashForWithdraw + ", " + accountTotal);

            final ObservableList<Data> tableData = FXCollections.observableArrayList(
                new Data("Cash Available For Trading",      formatNumber(cashForTrade) ),
                new Data("Cash Available For Withdrawal",   formatNumber(cashForWithdraw) ),
                new Data("Total Cash Balance",              formatNumber(cashBalance) ),
                new Data("Total Positions Market Value",    formatNumber(positionsValue) ),
                new Data("Total Account Value",             formatNumber(accountTotal) )
            );

            // get open positions
            List<LinkedTreeMap<String, Object>> result = (List) equity.get("equityPositions");
            int cnt = 0;
            for (LinkedTreeMap<String, Object> a : result) {
                String symbol       = a.get("symbol").toString();
                Double costBasis    = (Double) a.get("costBasis");
                Double tradingQty   = (Double) a.get("availableForTradingQty");
                // spot price
                Double marketPrice  = (Double) a.get("mktPrice");
                // spot price * qty
                Double marketValue  = (Double) a.get("marketValue");
                Double PL           = (Double) a.get("unrealizedPL");
                Double dayPL        = (Double) a.get("unrealizedDayPL");
                Double dayPLPercent = (Double) a.get("unrealizedDayPLPercent");

                Map<String, Object> p = new HashMap<>();
                p.put("symbol", symbol);
                p.put("availableForTradingQty", tradingQty);
                p.put("costBasis", costBasis);
                p.put("mktPrice", marketPrice);
                p.put("marketValue", marketValue);
                p.put("unrealizedPL", PL);
                p.put("unrealizedDayPL", dayPL);
                p.put("unrealizedDayPLPercent", dayPLPercent);

                this.positions.add(p);

                System.out.println("[" + cnt + "] Position: symbol: " + a.get("symbol")
                        + ", instrumentID: " + a.get("instrumentID")
                        + ", openQty: " + a.get("openQty")
                        + ", costBasis: " + a.get("costBasis"));
                cnt++;
            }

            // build UI
            TableColumn fieldCol = new TableColumn<Data, String>("Account Summary");
            fieldCol.setCellValueFactory(new PropertyValueFactory("field"));
            
            TableColumn valueCol = new TableColumn<Data, String>();
            valueCol.setCellValueFactory(new PropertyValueFactory("value"));
            valueCol.getStyleClass().add( "right-align");
            
            accTable.setEditable(false);
            accTable.setItems(tableData);
            accTable.getColumns().setAll(fieldCol, valueCol);

            // limit accTable height, based on row number
            accTable.setFixedCellSize(30);
            accTable.prefHeightProperty().bind(Bindings.size(accTable.getItems()).multiply(accTable.getFixedCellSize()).add(30));
            
            // manually fix table width, any better way??
            accTable.setMaxWidth(400);
            accTable.setPrefWidth(400);
            accTable.setMinWidth(400);
            accTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            
            System.out.println("table row: " + accTable.getItems().size() + ", cell size: " + accTable.getFixedCellSize());
            System.out.println("Table field width: " + fieldCol.prefWidthProperty().getValue()
                    + ", value width: " + valueCol.prefWidthProperty().getValue());

            final VBox vBox = new VBox();
            vBox.setSpacing(5);
            vBox.setPadding(new Insets(10, 0, 0, 10));
            vBox.getChildren().addAll(accTable);
            vBox.setPrefWidth(500);

            // add account summary tab
            accTab.setText("Account Summary (Practice Account)");
            accTab.setClosable(false);
            accTab.setContent(vBox);
            tabPane.getTabs().add(accTab);
            
            // select tab
            SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
            selectionModel.select(accTab);
        }

        public class Data {
            private final SimpleStringProperty field;
            private final SimpleStringProperty value;

            private Data(String sField, String sValue) {
                this.field = new SimpleStringProperty(sField);
                this.value = new SimpleStringProperty(sValue);
            }

            public String getField() {
                return field.get();
            }
            public void setField(String sField) {
                field.set(sField);
            }
            
            public String getValue() {
                return value.get();
            }
            public void setValue(String sValue) {
                value.set(sValue);
            }
        }

        private final Map<String, Object> accBlotter;
        private final List<Map<String, Object>> positions = new ArrayList<>();

        public  final Tab accTab  = new Tab();
        private final TableView accTable = new TableView();
    }
    
    private final JScrollPane jScrollPane = new javax.swing.JScrollPane();
    private final JFXPanel jfxPanel = new JFXPanel();
    private Scene scene;
    private TabPane tabPane;

    public DriveWealthAPI api;
}
