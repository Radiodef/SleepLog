package com.radiodef.sleeplog.app;

import com.radiodef.sleeplog.util.*;

import java.util.*;
import java.util.function.*;

import javafx.scene.control.*;
import javafx.scene.input.*;

final class MenuBars implements Supplier<MenuBar> {
    static final String MENU_BAR_ID = "menu-bar";
    
    private final SleepLogApp app;
    
    MenuBars(SleepLogApp app) {
        this.app = Objects.requireNonNull(app);
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
            // TODO: find out what the correct key is
//            exitItem.setAccelerator(KeyCombination.valueOf("Shortcut+X"));
            
            exitItem.setOnAction(e -> app.exit());
            
            fileMenu.getItems().add(exitItem);
            menuBar.getMenus().add(fileMenu);
        }
        
        var windowMenu = new Menu(Tools.isMac() ? "_Window" : "_View");
        windowMenu.setMnemonicParsing(true);
        
        var tableItem = new CheckMenuItem("Database _Table View");
        tableItem.setMnemonicParsing(true);
        tableItem.setAccelerator(KeyCombination.valueOf("Shortcut+T"));
        
        tableItem.setSelected(app.getTableViewStage().isShowing());
        tableItem.selectedProperty().addListener((p, o, val) -> app.setTableViewVisible(val));
        
        windowMenu.getItems().add(tableItem);
        menuBar.getMenus().add(windowMenu);
        
        return menuBar;
    }
}
