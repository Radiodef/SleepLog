package com.radiodef.sleeplog.util;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.transformation.*;

import java.util.*;
import java.util.function.*;

public class MappedList<T, U> extends TransformationList<U, T> {
    private final ObjectProperty<Function<? super T, ? extends U>> func;
    
    public MappedList(ObservableList<? extends T> source, Function<? super T, ? extends U> func) {
        super(source);
        this.func = new SimpleObjectProperty<>(Objects.requireNonNull(func, "func"));
    }
    
    @Override
    public int getSourceIndex(int i) {
        return i;
    }
    
    @Override
    public int getViewIndex(int i) {
        return i;
    }
    
    @Override
    public U get(int i) {
        return func.get().apply(getSource().get(i));
    }
    
    @Override
    public int size() {
        return getSource().size();
    }
    
    @Override
    protected void sourceChanged(ListChangeListener.Change<? extends T> change) {
        fireChange(new MappedChange<>(this, change));
    }
    
    private static final class MappedChange<T, U> extends ListChangeListener.Change<U> {
        private final ListChangeListener.Change<? extends T> change;
        private final Function<? super T, ? extends U> func;
        
        private MappedChange(MappedList<T, U> list,
                             ListChangeListener.Change<? extends T> change) {
            super(list);
            Objects.requireNonNull(list, "list");
            Objects.requireNonNull(change, "change");
            
            this.func = list.func.get();
            this.change = change;
        }
        
        @Override
        public boolean next() {
            return change.next();
        }
        
        @Override
        public void reset() {
            change.reset();
        }
        
        @Override
        public int getFrom() {
            return change.getFrom();
        }
        
        @Override
        public int getTo() {
            return change.getTo();
        }
        
        @Override
        public List<U> getRemoved() {
            var src = change.getRemoved();
            if (src.isEmpty())
                return Collections.emptyList();
            
            List<U> result = new ArrayList<>(src.size());
            
            for (var t : src)
                result.add(func.apply(t));
            
            return Collections.unmodifiableList(result);
        }
        
        @Override
        protected int[] getPermutation() {
            throw new AssertionError();
        }
    }
}
