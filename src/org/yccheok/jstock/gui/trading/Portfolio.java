/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yccheok.jstock.gui.trading;

import com.google.gson.internal.LinkedTreeMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import static javafx.geometry.Orientation.VERTICAL;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.yccheok.jstock.trading.AccountModel;
import org.yccheok.jstock.trading.OpenPosModel;
import org.yccheok.jstock.trading.OrderModel;
import org.yccheok.jstock.trading.PortfolioService;
import org.yccheok.jstock.trading.DriveWealthAPI;

/**
 *
 * @author shuwnyuan
 */
public class Portfolio {
    public Portfolio (DriveWealthAPI api) {
        this.api = api;
        startBackgroundService(api);
    }

    private void startBackgroundService (DriveWealthAPI api) {
        PortfolioService service = new PortfolioService(api);
        
        // start immediately
        service.setDelay(Duration.seconds(0));
        // run every 10 sec
        service.setPeriod(Duration.seconds(10));
        
        service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(final WorkerStateEvent workerStateEvent) {
                Map<String, Object> result = (Map<String, Object>) workerStateEvent.getSource().getValue();
                
                // First run calls account Blotter & get instruments
                // Following run just call get market data to get latest market price
                
                if (result.containsKey("accBlotter") && result.containsKey("instruments")) {
                    accBlotter = (Map<String, Object>) result.get("accBlotter");
                    instruments = (Map<String, Map>) result.get("instruments");

                    initOpenPosTableData();
                    initOrderTableData();
                    initAccData();
                } else if (result.containsKey("marketPrices")) {
                    marketPrices = (Map) result.get("marketPrices");
                    updateOpenPosPrice();
                    updateOrderPrice();
                    updateAccData();
                }
            }
        });
        
        service.start();
    }
    
    private void initOpenPosTableData () {
        LinkedTreeMap<String, Object> equity = (LinkedTreeMap) this.accBlotter.get("equity");
        List<LinkedTreeMap<String, Object>> positions = (List) equity.get("equityPositions");
        
        for (LinkedTreeMap<String, Object> pos : positions) {
            Map<String, Object> ins = this.instruments.get(pos.get("symbol").toString());

            Map<String, Object> data = new HashMap<>();
            data.put("name",            ins.get("name"));
            data.put("symbol",          pos.get("symbol"));
            data.put("instrumentID",    pos.get("instrumentID"));
            data.put("units",           pos.get("availableForTradingQty"));
            data.put("averagePrice",    pos.get("avgPrice"));
            data.put("costBasis",       pos.get("costBasis"));
            data.put("marketPrice",     pos.get("mktPrice"));
            data.put("marketValue",     pos.get("marketValue"));
            data.put("unrealizedPL",    pos.get("unrealizedPL"));
            
            this.posList.add(new OpenPosModel(data));
        }

        this.posTable.setItems(this.posList);
        this.posTable.prefHeightProperty().bind(Bindings.size(this.posTable.getItems()).multiply(this.posTable.getFixedCellSize()).add(30));
    }
    
    private void initOrderTableData () {
        List<LinkedTreeMap<String, Object>> orders = (List) this.accBlotter.get("orders");

        for (LinkedTreeMap<String, Object> ord : orders) {
            Map<String, Object> ins = this.instruments.get(ord.get("symbol").toString());

            Map<String, Object> data = new HashMap<>();
            data.put("name",        ins.get("name"));
            data.put("marketPrice", ins.get("lastTrade"));
            data.put("symbol",      ord.get("symbol"));
            data.put("units",       ord.get("orderQty"));
            data.put("side",        ord.get("side"));
            data.put("orderType",   ord.get("orderType"));

            if (ord.containsKey("limitPrice")) {
                data.put("limitPrice", ord.get("limitPrice"));
            }
            if (ord.containsKey("stopPrice")) {
                data.put("stopPrice", ord.get("stopPrice"));
            }
            
            this.ordList.add(new OrderModel(data));
        }
        
        this.ordTable.setItems(this.ordList);
        this.ordTable.prefHeightProperty().bind(Bindings.size(this.ordTable.getItems()).multiply(this.ordTable.getFixedCellSize()).add(30));
    }
    
    private void initAccData () {
        this.acc = new AccountModel(this.accBlotter, this.posList);

        this.shareAmount.textProperty().bind(this.acc.equity);
        this.profitAmount.textProperty().bind(this.acc.unrealizedPL);
        this.cashAmount.textProperty().bind(this.acc.cashForTrade);
        this.totalAmount.textProperty().bind(this.acc.accountTotal);

        this.profitAmount.getStyleClass().add(this.acc.unrealizedPLCss());
        this.cashAmount.getStyleClass().add(this.acc.cashForTradeCss());
        this.totalAmount.getStyleClass().add(this.acc.accountTotalCss());
        this.shareAmount.getStyleClass().add(this.acc.equityValueCss());
    }
    
    private void updateOpenPosPrice () {
        for (OpenPosModel pos : this.posList) {
            final String symbol = pos.getSymbol();
            final Double price = this.marketPrices.get(symbol);
            pos.updateMarketPrice(price);
        }
    }
    
    private void updateOrderPrice () {
        for (OrderModel ord : this.ordList) {
            final String symbol = ord.getSymbol();
            final Double price = this.marketPrices.get(symbol);
            ord.updateMarketPrice(price);
        }
    }
    
    private void updateAccData () {
        this.acc.update(this.posList);

        this.profitAmount.getStyleClass().clear();
        this.profitAmount.getStyleClass().add(acc.unrealizedPLCss());
    }
    
    public Tab createTab() {
        final VBox vBox = new VBox();
        vBox.setSpacing(5);
        vBox.setPadding(new Insets(5, 10, 5, 10));  // Insets: top, right, bottom, left
        vBox.setPrefWidth(1000);

        // Account Summary
        initAccSummary();
        vBox.getChildren().add(this.accBorderPane);
        
        // Open Positions
        initOpenPosTable();
        
        VBox vboxOpenPos = new VBox(5);
        vboxOpenPos.setPadding(new Insets(5, 5, 5, 5));  // Insets: top, right, bottom, left

        final Label posLabel = new Label("Current Investments");
        vboxOpenPos.getChildren().addAll(posLabel, this.posTable);

        // Pending orders
        initOrderTable();
        
        VBox vboxOrder = new VBox(5);
        vboxOrder.setPadding(new Insets(5, 5, 5, 5));  // Insets: top, right, bottom, left

        final Label ordLabel = new Label("Pending Orders");
        vboxOrder.getChildren().addAll(ordLabel, this.ordTable);

        // Up Down partition
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(VERTICAL);
        splitPane.setDividerPositions(0.6);
        splitPane.getItems().addAll(vboxOpenPos, vboxOrder);
        splitPane.setPrefHeight(500);
        vBox.getChildren().add(splitPane);
        
        vboxOpenPos.prefWidthProperty().bind(splitPane.widthProperty());
        vboxOrder.prefWidthProperty().bind(splitPane.widthProperty());

        // add Portfolio tab
        this.accTab.setText("Portfolio (Practice Account)");
        this.accTab.setClosable(false);
        this.accTab.setContent(vBox);

        return this.accTab;
    }
    
    private void initAccSummary () {
        // Left content
        HBox leftHbox = new HBox(8);
        
        // Stocks on hand value
        Label shareText = new Label("Share:");
        
        // Unrealized PL
        Label profitText = new Label("Paper Profit:");
        profitText.setPadding(new Insets(0, 0, 0, 10));

        leftHbox.getChildren().addAll(shareText, this.shareAmount, profitText, this.profitAmount);
        
        // Right content
        HBox rightHbox = new HBox(8);
        
        // Cash for trading
        Label cashText = new Label("Cash to Invest:");

        // Total: Cash balance + Stocks
        Label totalText = new Label("Total:");
        totalText.setPadding(new Insets(0, 0, 0, 10));
        
        rightHbox.getChildren().addAll(cashText, this.cashAmount, totalText, this.totalAmount);
        
        this.accBorderPane.setPadding(new Insets(5, 0, 10, 0));    // Insets: top, right, bottom, left
        this.accBorderPane.setLeft(leftHbox);
        this.accBorderPane.setRight(rightHbox);
        this.accBorderPane.setId("accBorderPane");
    }

    private void initOpenPosTable () {
        // Open Positions table
        TableColumn<OpenPosModel, String> symbolCol = new TableColumn<>("Stock");
        symbolCol.setCellValueFactory(new PropertyValueFactory("symbol"));
        symbolCol.getStyleClass().add("left");

        TableColumn<OpenPosModel, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory("name"));
        nameCol.getStyleClass().add("left");

        TableColumn<OpenPosModel, String> unitsCol = new TableColumn<>("Units");
        unitsCol.setCellValueFactory(new PropertyValueFactory("units"));
        unitsCol.getStyleClass().add("right");

        TableColumn<OpenPosModel, String> avgPriceCol = new TableColumn<>("Average Purchase Price");
        avgPriceCol.setCellValueFactory(new PropertyValueFactory("averagePrice"));
        avgPriceCol.getStyleClass().add("right");

        TableColumn<OpenPosModel, String> mktPriceCol = new TableColumn<>("Current Price");
        mktPriceCol.setCellValueFactory(new PropertyValueFactory("marketPrice"));
        mktPriceCol.getStyleClass().add("right");

        TableColumn<OpenPosModel, String> costCol = new TableColumn<>("Purchase Value");
        costCol.setCellValueFactory(new PropertyValueFactory("costBasis"));
        costCol.getStyleClass().add("right");

        TableColumn<OpenPosModel, String> mktValueCol = new TableColumn<>("Current Value");
        mktValueCol.setCellValueFactory(new PropertyValueFactory("marketValue"));
        mktValueCol.getStyleClass().add("right");
        
        TableColumn<OpenPosModel, String> plCol = new TableColumn<>("Gain/Loss Value");
        plCol.setCellValueFactory(new PropertyValueFactory("unrealizedPL"));
        plCol.getStyleClass().add("right");

        symbolCol.setSortable(false);
        nameCol.setSortable(false);
        unitsCol.setSortable(false);
        avgPriceCol.setSortable(false);
        mktPriceCol.setSortable(false);
        costCol.setSortable(false);
        mktValueCol.setSortable(false);
        plCol.setSortable(false);
        
        this.posTable.getColumns().setAll(symbolCol, nameCol, unitsCol, avgPriceCol, costCol, mktPriceCol, mktValueCol, plCol);

        this.posTable.setEditable(false);
        this.posTable.setItems(this.posList);

        // limit Table height, based on row number
        this.posTable.setFixedCellSize(20);
        this.posTable.prefHeightProperty().bind(Bindings.size(this.posTable.getItems()).multiply(this.posTable.getFixedCellSize()).add(30));

        // set all columns having equal width
        this.posTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void initOrderTable () {
        // Pending Orders table
        TableColumn symbolCol = new TableColumn("Stock");
        symbolCol.setCellValueFactory(new PropertyValueFactory("symbol"));
        symbolCol.getStyleClass().add("left");

        TableColumn nameCol = new TableColumn("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory("name"));
        nameCol.getStyleClass().add("left");

        TableColumn unitsCol = new TableColumn("Units");
        unitsCol.setCellValueFactory(new PropertyValueFactory("units"));
        unitsCol.getStyleClass().add("right");

        TableColumn mktPriceCol = new TableColumn("Current Price");
        mktPriceCol.setCellValueFactory(new PropertyValueFactory("marketPrice"));
        mktPriceCol.getStyleClass().add("right");
        
        TableColumn typeCol = new TableColumn("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory("type"));
        typeCol.getStyleClass().add("left");

        TableColumn sideCol = new TableColumn("Side");
        sideCol.setCellValueFactory(new PropertyValueFactory("side"));
        sideCol.getStyleClass().add("left");
        
        TableColumn limitCol = new TableColumn("Limit Price");
        limitCol.setCellValueFactory(new PropertyValueFactory("limitPrice"));
        limitCol.getStyleClass().add("right");

        TableColumn stopCol = new TableColumn("Stop Price");
        stopCol.setCellValueFactory(new PropertyValueFactory("stopPrice"));
        stopCol.getStyleClass().add("right");
        
        symbolCol.setSortable(false);
        nameCol.setSortable(false);
        unitsCol.setSortable(false);
        mktPriceCol.setSortable(false);
        typeCol.setSortable(false);
        sideCol.setSortable(false);
        limitCol.setSortable(false);
        stopCol.setSortable(false);

        this.ordTable.getColumns().setAll(symbolCol, nameCol, unitsCol, mktPriceCol, typeCol, sideCol, limitCol, stopCol);

        this.ordTable.setEditable(false);
        this.ordTable.setItems(this.ordList);
        
        // limit Table height, based on row number
        this.ordTable.setFixedCellSize(20);
        this.ordTable.prefHeightProperty().bind(Bindings.size(this.ordTable.getItems()).multiply(this.ordTable.getFixedCellSize()).add(30));

        // set all columns having equal width
        this.ordTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private final DriveWealthAPI api;
    
    private Map<String, Object> accBlotter;
    private Map<String, Map> instruments;
    private Map<String, Double> marketPrices;

    private ObservableList<OpenPosModel> posList = FXCollections.observableArrayList();
    private ObservableList<OrderModel> ordList = FXCollections.observableArrayList();
    
    public  final Tab accTab  = new Tab();
    
    private final BorderPane accBorderPane = new BorderPane();
    private final Label shareAmount = new Label();
    private final Label profitAmount = new Label();
    private final Label cashAmount = new Label();
    private final Label totalAmount = new Label();
    
    private final TableView posTable = new TableView();
    private final TableView ordTable = new TableView();
    
    private AccountModel acc;
}
    