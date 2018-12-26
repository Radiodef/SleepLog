package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.util.*;

import java.util.*;
import java.util.function.*;

import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.stage.*;

final class MenuBars implements Supplier<MenuBar> {
    private static final String MENU_BAR_ID = "menu-bar";
    private static final String TABLE_VIEW_ID = "table-view-item";
    private static final String GRAPH_VIEW_ID = "graph-view-item";
    
    private final SleepLogApp app;
    
    private final List<MenuBar> bars;
    
    MenuBars(SleepLogApp app) {
        this.app = Objects.requireNonNull(app);
        this.bars = new ArrayList<>();
    }
    
    @Override
    public MenuBar get() {
        var menuBar = new MenuBar();
        menuBar.setId(MENU_BAR_ID);
        
        if (Tools.isMac()) {
            // TODO: figure out what this does on other systems
            menuBar.setUseSystemMenuBar(true);
            
        } else {
            var fileMenu = new Menu("_File");
            fileMenu.setMnemonicParsing(true);
            
            var exitItem = new MenuItem("E_xit");
            exitItem.setMnemonicParsing(true);
            
            if (Tools.isWindows()) {
                exitItem.setAccelerator(KeyCombination.valueOf("Alt+F4"));
            } // else TODO: learn linux standard?
            
            exitItem.setOnAction(e -> app.exit());
            
            fileMenu.getItems().add(exitItem);
            menuBar.getMenus().add(fileMenu);
        }
        
        var windowMenu = new Menu(Tools.isMac() ? "_Window" : "_View");
        windowMenu.setMnemonicParsing(true);
        
        var tableItem = new CheckMenuItem("Database _Table View");
        tableItem.setId(TABLE_VIEW_ID);
        tableItem.setMnemonicParsing(true);
        tableItem.setAccelerator(KeyCombination.valueOf("Shortcut+T"));
        
        bindItemToStage(tableItem, app::getTableViewStage);
        
        var graphItem = new CheckMenuItem("_Graphs View");
        graphItem.setId(GRAPH_VIEW_ID);
        graphItem.setMnemonicParsing(true);
        graphItem.setAccelerator(KeyCombination.valueOf("Shortcut+G"));
        
        bindItemToStage(graphItem, app::getGraphViewStage);
        
        windowMenu.getItems().addAll(tableItem, graphItem);
        menuBar.getMenus().add(windowMenu);
        
        bars.add(menuBar);
        return menuBar;
    }
    
    private void bindItemToStage(CheckMenuItem item, Supplier<Stage> supplier) {
        item.setSelected(supplier.get().isShowing());
        
        Consumer<Boolean> setStageVisible = visible -> {
            var stage = supplier.get();
            
            if (visible) {
                stage.show();
                stage.toFront();
            } else {
                stage.hide();
            }
            
            bars.stream()
                .flatMap(bar -> bar.getMenus().stream().flatMap(menu -> menu.getItems().stream()))
                .filter(item1 -> (item1 != item) && Objects.equals(item.getId(), item1.getId()))
                .forEach(item1 -> ((CheckMenuItem) item1).setSelected(visible));
        };
        
        item.selectedProperty().addListener((p, o, val) -> setStageVisible.accept(val));
        
        supplier.get().setOnCloseRequest(e -> setStageVisible.accept(false));
    }
}
