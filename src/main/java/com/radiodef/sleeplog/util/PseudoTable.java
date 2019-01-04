package com.radiodef.sleeplog.util;

import com.radiodef.sleeplog.app.*;

import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.beans.value.*;

import java.util.*;
import java.util.function.*;
import java.lang.reflect.*;

import org.apache.commons.lang3.*;
import org.apache.commons.lang3.reflect.*;

@SuppressWarnings({"unused", "WeakerAccess"})
public class PseudoTable<R> extends BorderPane {
    private static final String PSEUDO_TABLE = "pseudo-table";
    private static final String PANE_CLASS = PSEUDO_TABLE + "-pane";
    private static final String COLS_HBOX_CLASS = PSEUDO_TABLE + "-columns-hbox";
    private static final String COL_VBOX_CLASS = PSEUDO_TABLE + "-column-vbox";
    private static final String CELL_CLASS = PSEUDO_TABLE + "-cell";
    private static final String HEADER_CLASS = PSEUDO_TABLE + "-header";
    
    private final ObjectProperty<Class<R>> rowClass;
    
    private final ObservableList<Column<R, ?>> columns = FXCollections.observableArrayList();
    private final ObservableList<R> data = FXCollections.observableArrayList();
    
    private final HBox columnsBox = new HBox();
    
    public PseudoTable(Class<R> rowClass) {
        this.rowClass = new SimpleObjectProperty<>(Objects.requireNonNull(rowClass, "rowClass"));
        
        getStyleClass().add(PANE_CLASS);
        columnsBox.getStyleClass().add(COLS_HBOX_CLASS);
        
        columns.addListener(Tools.listChangeListener(c -> layoutComponents()));
        data.addListener(Tools.listChangeListener(c -> layoutComponents()));
        
        setCenter(columnsBox);
    }
    
    public ReadOnlyObjectProperty<Class<R>> rowClassProperty() {
        return rowClass;
    }
    
    public ObservableList<Column<R, ?>> getColumns() {
        return columns;
    }
    
    @SafeVarargs
    public final void addColumns(Column<R, ?>... columns) {
        this.columns.addAll(columns);
    }
    
    public ObservableList<R> getData() {
        return data;
    }
    
    private void layoutComponents() {
        Log.enter();
        columnsBox.getChildren().clear();
        
        for (var col : columns) {
            var box = col.node.get();
            box.getChildren().clear();
            columnsBox.getChildren().add(box);
        }
        
        for (var col : columns) {
            col.clearListeners();
            col.addHeader();
            
            for (var elem : data) {
                col.addCell(elem);
            }
        }
    }
    
    public <C> Column<R, C> createColumn(Class<C> colClass,
                                         String label,
                                         String property) {
        return createColumn(colClass, label, property, null);
    }
    
    public <C> Column<R, C> createColumn(Class<C> colClass,
                                         String label,
                                         String property,
                                         Function<? super C, String> format) {
        return new Column<>(rowClass.get(), colClass, label, property, format);
    }
    
    public static class Column<R, C> {
        private final ObjectProperty<Class<R>> rowClass;
        private final ObjectProperty<Class<C>> colClass;
        
        private final ObjectProperty<String> label;
        private final ObjectProperty<String> property;
        private final ObjectProperty<VBox> node;
        
        private final ObjectProperty<Function<? super C, String>> format;
        
        private final ObjectProperty<Method> getter;
        
        private final WeakHashMap<Property<C>, ChangeListener<C>> listeners;
        
        public Column(Class<R> rowClass,
                      Class<C> colClass,
                      String label,
                      String property,
                      Function<? super C, String> format) {
            this.rowClass = new SimpleObjectProperty<>(Objects.requireNonNull(rowClass, "rowClass"));
            this.colClass = new SimpleObjectProperty<>(Objects.requireNonNull(colClass, "colClass"));
            
            this.label = new SimpleObjectProperty<>(label);
            this.property = new SimpleObjectProperty<>(property);
            
            this.getter = new SimpleObjectProperty<>();
            getter.bind(
                Bindings.createObjectBinding(() -> {
                    if (!ObjectUtils.allNotNull(this.rowClass.get(), this.colClass.get(), this.property.get())) {
                        return null;
                    }
                    
                    var name = this.property.get() + "Property";
                    var method = this.rowClass.get().getMethod(name);
                    var type = method.getGenericReturnType();
                    var expect = TypeUtils.parameterize(Property.class, this.colClass.get());
                    
                    if (!TypeUtils.isAssignable(type, expect)) {
                        throw new IllegalArgumentException(name + " with type " + type);
                    }
                    var params = method.getParameterCount();
                    if (params != 0) {
                        throw new IllegalArgumentException(name + " with " + params + " parameters");
                    }
                    
                    return method;
                },
                this.rowClass,
                this.colClass,
                this.property)
            );
            
            if (format == null)
                format = String::valueOf;
            this.format = new SimpleObjectProperty<>(format);
            
            var box = new VBox();
            box.getStyleClass().add(COL_VBOX_CLASS);
            HBox.setHgrow(box, Priority.ALWAYS);
            
            this.node = new SimpleObjectProperty<>(box);
            
            this.listeners = new WeakHashMap<>();
        }
        
        public ReadOnlyObjectProperty<Class<R>> rowClassProperty() {
            return rowClass;
        }
        
        public ReadOnlyObjectProperty<Class<C>> colClassProperty() {
            return colClass;
        }
        
        public ReadOnlyObjectProperty<String> labelProperty() {
            return label;
        }
        
        public ReadOnlyObjectProperty<VBox> nodeProperty() {
            return node;
        }
        
        public ReadOnlyObjectProperty<Function<? super C, String>> formatProperty() {
            return format;
        }
        
        private void clearListeners() {
            listeners.forEach(Property::removeListener);
            listeners.clear();
        }
        
        private void addHeader() {
            var label = new Label(StringUtils.defaultString(this.label.get()));
            label.getStyleClass().add(HEADER_CLASS);
            
            node.get().getChildren().add(label);
        }
        
        private void addCell(R row) {
            Property<C> prop = getProperty(row);
            if (prop == null) {
                return;
            }
            
            var label = new Label(toString(prop.getValue()));
            ChangeListener<C> listener = (a, b, val) -> label.setText(toString(val));
            
            prop.addListener(listener);
            listeners.put(prop, listener);
            
            var cell = new BorderPane(label);
            cell.getStyleClass().add(CELL_CLASS);
            
            node.get().getChildren().add(cell);
        }
        
        @SuppressWarnings("unchecked")
        private Property<C> getProperty(R row) {
            var method = this.getter.get();
            
            if (method == null) {
                return null;
            }
            
            try {
                return (Property<C>) method.invoke(row, ArrayUtils.EMPTY_OBJECT_ARRAY);
            } catch (RuntimeException x) {
                throw x;
            } catch (Exception x) {
                throw new IllegalStateException(x);
            }
        }
        
        private String toString(C value) {
            return (value == null) ? "null" : format.get().apply(value);
        }
    }
}