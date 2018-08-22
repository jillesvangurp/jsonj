package com.github.jsonj;

import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * Performant array list based map for small number of entries. Get performs linearly for number of entries however, it
 * uses vastly less memory and it is actually fast enough for small numbers of entries.
 */
public class SimpleStringKeyMap<V> implements Map<String, V>, Serializable {

    private static final long serialVersionUID = 7650009698202273725L;
    private boolean immutable=false;

    String[] keysArr=new String[3];
    private final ArrayList<V> values = new ArrayList<>();

    public void makeImmutable() {
        immutable=true;
    }

    public boolean isMutable() {
        return !immutable;
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean isEmpty() {
        return values.size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return containsKey((String)key);
    }

    public boolean containsKey(String key) {
        for(int i=0; i<values.size();i++) {
            String k = keysArr[i];
            if(key.equals(k)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        for(V e: values) {
            if(e.equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        return getWithKey((String)key);
    }

    public V getWithKey(String key) {
        int index = getIndex(key);
        if(index>=0) {
            return values.get(index);
        } else {
            return null;
        }
    }

    private int getIndex(String key) {
        int j=0;
        for(int i=0; i<values.size();i++) {
            String k = keysArr[i];
            if(key.equals(k)) {
                return j;
            }
            j++;
        }
        return -1;
    }

    @Override
    public V put(String key, V value) {
        if(immutable) {
            throw new IllegalStateException("object is immutable");
        }
        Validate.notNull(key);
        int index = getIndex(key);
        if(index >=0) {
            values.set(index, value);
        } else {
            if(values.size()+1<keysArr.length) {
                // dynamically grow array
                keysArr = Arrays.copyOf(keysArr, keysArr.length + 3);
            }
            keysArr[values.size()] = key;
            values.add(value);
        }
        return value;
    }

    @Override
    public V remove(Object key) {
        if(immutable) {
            throw new IllegalStateException("object is immutable");
        }

        int index = getIndex((String)key);
        if(index >=0) {
            System.arraycopy(keysArr,index+1,keysArr,index,keysArr.length-1-index);
            return values.remove(index);
        } else {
            return null;
        }
    }

    @Override
    public void putAll(java.util.Map<? extends String, ? extends V> m) {
        if(immutable) {
            throw new IllegalStateException("object is immutable");
        }

        for (Entry<? extends String, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    };

    @Override
    public void clear() {
        keysArr = new String[5];
        values.clear();
    }

    @Override
    public Set<String> keySet() {
        Set<String> result = new HashSet<>();
        for(int i=0; i<values.size();i++) {
            result.add(keysArr[i]);
        }
        return result;
    }

    @Override
    public Collection<V> values() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public Set<Entry<String, V>> entrySet() {

        return new EntrySet(this);
    }

    private class EntrySet implements Set<Entry<String, V>> {

        private final SimpleStringKeyMap<V> owner;

        public EntrySet(SimpleStringKeyMap<V> simpleMap) {
            this.owner = simpleMap;
        }

        @Override
        public int size() {
            return owner.size();
        }

        @Override
        public boolean isEmpty() {
            return owner.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            @SuppressWarnings("unchecked")
            Entry<String, JsonElement> e = (Entry<String, JsonElement>) o;
            int index = owner.getIndex(e.getKey());
            if(index >= 0) {
                return values.get(index).equals(e.getValue());
            } else {
                return false;
            }
        }

        @Override
        public Iterator<Entry<String, V>> iterator() {
            return new Iterator<Map.Entry<String,V>>() {
                int startSize=size();
                int index=0;

                @Override
                public boolean hasNext() {
                    if(size() != startSize) {
                        throw new ConcurrentModificationException();
                    }
                    return index < values.size();
                }

                @Override
                public Entry<String, V> next() {
                    if(hasNext()) {
                        EntryImpl next = new EntryImpl(keysArr[index], values.get(index));
                        index++;
                        return next;
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                @Override
                public void remove() {
                    if(immutable) {
                        throw new IllegalStateException("object is immutable");
                    }

                    if(owner.size() == 0) {
                        throw new NoSuchElementException("The entry set is empty");
                    }
                    if(index == 0) {
                        throw new IllegalStateException("next has not been called yet");
                    }
                    int removeIndex = index-1;
                    System.arraycopy(keysArr,removeIndex+1,keysArr,removeIndex,keysArr.length-1-removeIndex);
                    values.remove(removeIndex);
                    index--;
                    startSize--;
                }
            };
        }

        @Override
        public Object[] toArray() {
            @SuppressWarnings("unchecked")
            Entry<String,V>[] result = new Entry[owner.size()];
            int i=0;
            for(Entry<String,V> e: this) {
                result[i] = e;
                i++;
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T[] toArray(T[] a) {
            return (T[]) toArray();
        }

        @Override
        public boolean add(Entry<String, V> e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            for(Object o: c) {
                if(!contains(o)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean addAll(Collection<? extends java.util.Map.Entry<String, V>> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
    }

    private class EntryImpl implements Entry<String, V> {
        private final String key;
        private final V value;

        public EntryImpl(String key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException("Entries are immutable");
        }

        @Override
        public boolean equals(Object obj) {
            return Objects.deepEquals(this, obj);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key,value);
        }

        @Override
        public String toString() {
            return key +":"+value;
        }
    }
}
