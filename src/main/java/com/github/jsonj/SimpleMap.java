package com.github.jsonj;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.jillesvangurp.efficientstring.EfficientString;

/**
 * Performant array list based map for small number of entries. Get performs linearly for number of entries however, it
 * uses vastly less memory and it is actually fast enough for small numbers of entries.
 */
public class SimpleMap<K,V> implements Map<K,V>, Serializable {

    private static final long serialVersionUID = 320985071159254522L;

    private final ArrayList<K> keys = new ArrayList<>();
    private final ArrayList<V> values = new ArrayList<>();

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean isEmpty() {
        return keys.size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        for(K k:keys) {
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
        int index = getIndex(key);
        if(index>=0) {
            return values.get(index);
        } else {
            return null;
        }
    }

    private int getIndex(Object key) {
        int i=0;
        for(K k:keys) {
            if(key.equals(k)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public V put(K key, V value) {
        Validate.notNull(key);
        int index = getIndex(key);
        if(index >=0) {
            values.set(index, value);
        } else {
            keys.add(key);
            values.add(value);
        }
        return value;
    }

    @Override
    public V remove(Object key) {
        int index = getIndex(key);
        if(index >=0) {
            keys.remove(index);
            return values.remove(index);
        } else {
            return null;
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for(Map.Entry<? extends K, ? extends V> e: m.entrySet()) {
            put(e.getKey(),e.getValue());
        }
    }

    @Override
    public void clear() {
        keys.clear();
        values.clear();
    }

    @Override
    public Set<K> keySet() {
        return Sets.newHashSet(keys);
    }

    @Override
    public Collection<V> values() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {

        return new EntrySet(this);
    }

    private class EntrySet implements Set<Entry<K, V>> {

        private final SimpleMap<K,V> owner;

        public EntrySet(SimpleMap<K,V> simpleMap) {
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
            Entry<EfficientString, JsonElement> e = (Entry<EfficientString, JsonElement>) o;
            int index = owner.getIndex(e.getKey());
            if(index >=0) {
                return values.get(index).equals(e.getValue());
            } else {
                return false;
            }
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new Iterator<Map.Entry<K,V>>() {
                int startSize=size();
                int index=0;

                @Override
                public boolean hasNext() {
                    if(size() != startSize) {
                        throw new ConcurrentModificationException();
                    }
                    return index < keys.size();
                }

                @Override
                public Entry<K, V> next() {
                    if(hasNext()) {
                        EntryImpl next = new EntryImpl(keys.get(index), values.get(index));
                        index++;
                        return next;
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                @Override
                public void remove() {
                    if(owner.size() == 0) {
                        throw new NoSuchElementException("The entry set is empty");
                    }
                    if(index == 0) {
                        throw new IllegalStateException("next has not been called yet");
                    }
                    keys.remove(index-1);
                    values.remove(index-1);
                    index--;
                    startSize--;
                }
            };
        }

        @Override
        public Object[] toArray() {
            @SuppressWarnings("unchecked")
            Entry<K,V>[] result = new Entry[owner.size()];
            int i=0;
            for(Entry<K,V> e: this) {
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
        public boolean add(Entry<K, V> e) {
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
        public boolean addAll(Collection<? extends java.util.Map.Entry<K, V>> c) {
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

    private class EntryImpl implements Entry<K, V> {
        private final K key;
        private final V value;

        public EntryImpl(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
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
    }
}
