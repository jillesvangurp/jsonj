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
 * Alternative implementation of JsonObject that until 2.37 was the default implementation. Like the current implementation, it uses two lists
 * of the keys and values. However, it wraps the keys with an EfficientString, which allows us to use an int as a reference to the key. This is far
 * more memory efficient for relatively small numnber of keys and large amounts of objects.
 */
public class SimpleIntKeyMap<V> implements Map<Integer, V>, Serializable {

    private static final long serialVersionUID = 7650009698202273725L;
    private boolean immutable=false;

    int[] keysArr=new int[3];
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
        return containsIntKey((Integer)key);
    }

    public boolean containsIntKey(int key) {
        for(int i=0; i<values.size();i++) {
            int k = keysArr[i];
            if(key == k) {
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
        return getWithIntKey((Integer)key);
    }

    public V getWithIntKey(int key) {
        int index = getIndex(key);
        if(index>=0) {
            return values.get(index);
        } else {
            return null;
        }
    }

    private int getIndex(int key) {
        int j=0;
        for(int i=0; i<values.size();i++) {
            int k = keysArr[i];
            if(key == k) {
                return j;
            }
            j++;
        }
        return -1;
    }

    @Override
    public V put(Integer key, V value) {
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

        int index = getIndex((Integer)key);
        if(index >=0) {
            System.arraycopy(keysArr,index+1,keysArr,index,keysArr.length-1-index);
            return values.remove(index);
        } else {
            return null;
        }
    }

    @Override
    public void putAll(java.util.Map<? extends Integer, ? extends V> m) {
        if(immutable) {
            throw new IllegalStateException("object is immutable");
        }

        for (Entry<? extends Integer, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    };

    @Override
    public void clear() {
        keysArr = new int[5];
        values.clear();
    }

    @Override
    public Set<Integer> keySet() {
        Set<Integer> result = new HashSet<>();
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
    public Set<Entry<Integer, V>> entrySet() {

        return new EntrySet(this);
    }

    private class EntrySet implements Set<Entry<Integer, V>> {

        private final SimpleIntKeyMap<V> owner;

        public EntrySet(SimpleIntKeyMap<V> simpleMap) {
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
            Entry<Integer, JsonElement> e = (Entry<Integer, JsonElement>) o;
            int index = owner.getIndex(e.getKey());
            if(index >=0) {
                return values.get(index).equals(e.getValue());
            } else {
                return false;
            }
        }

        @Override
        public Iterator<Entry<Integer, V>> iterator() {
            return new Iterator<Map.Entry<Integer,V>>() {
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
                public Entry<Integer, V> next() {
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
            Entry<Integer,V>[] result = new Entry[owner.size()];
            int i=0;
            for(Entry<Integer,V> e: this) {
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
        public boolean add(Entry<Integer, V> e) {
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
        public boolean addAll(Collection<? extends java.util.Map.Entry<Integer, V>> c) {
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

    private class EntryImpl implements Entry<Integer, V> {
        private final int key;
        private final V value;

        public EntryImpl(int key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Integer getKey() {
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
