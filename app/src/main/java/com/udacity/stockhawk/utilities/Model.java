package com.udacity.stockhawk.utilities;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Model {
    private final JsonArray elements;
    private int index = 0;
    private Gson gson = new Gson();

    public static Model obtain() {
        return obtain(new JsonArray());
    }

    public static Model obtain(String json) {
        return obtain(new JsonParser().parse(json).getAsJsonArray());
    }

    public static Model obtain(Modelable modelable) {
        Model model = new Model(new JsonArray());
        modelable.writeToModel(model);
        return model;
    }

    public static Model obtain(JsonArray elements) {
        return new Model(elements);
    }

    private Model(JsonArray elements) {
        this.elements = elements;
    }

    //Model
    public void writeModelable(Modelable value) {
        elements.add(obtain(value).elements);
    }

    public <T> T readModelable(Modelable.Creator<T> creator) {
        return creator.createFromModel(obtain(element().getAsJsonArray()));
    }

    //Int
    public void writeInt(int value) {
        elements.add(value);
    }

    public int readInt() {
        return element().getAsInt();
    }

    //Float
    public void writeFloat(float value) {
        writeNumber(value);
    }

    public float readFloat() {
        return element().getAsFloat();
    }

    //String
    public void writeString(String value) {
        elements.add(value);
    }

    public String readString() {
        return element().getAsString();
    }

    //Number
    public void writeNumber(Number value) {
        elements.add(value);
    }

    public <T extends Number> T readNumber() {
        return (T) element().getAsNumber();
    }

    //Object
    public void writeObject(Object value) {
        elements.add(gson.toJson(value));
    }

    public <T> T readObject(Class<T> type) {
        return gson.fromJson(element().getAsString(), type);
    }

    //Long
    public void writeLong(long value) {
        elements.add(value);
    }

    public long readLong() {
        return element().getAsLong();
    }

    //--List--
    public void writeList(List<?> list) {
        writeInt(list.size());
        for (Object object : list)
            write(object);
    }

    public <T> ArrayList<T> readList(Modelable.Creator<T> creator) {
        return readList(new ArrayList<>(), creator);
    }

    public <T> ArrayList<T> readList(Class<T> type) {
        return readList(new ArrayList<>(), type);
    }

    public <T> ArrayList<T> readList(Class<T> type, Modelable.Creator<T> creator) {
        return readList(new ArrayList<>(), type, creator);
    }

    public <T, R extends Collection<T>> R readList(R collection, Modelable.Creator<T> creator) {
        return readList(collection, null, creator);
    }

    public <T, R extends Collection<T>> R readList(R collection, Class<T> type) {
        return readList(collection, type, null);
    }

    public <T, R extends Collection<T>> R readList(R collection, Class<T> type, Modelable.Creator<T> creator) {
        int size = readInt();
        for (int i = 0; i < size; i++)
            collection.add(read(type, creator));
        return collection;
    }


    //--Map--
    public void writeMap(Map<?, ?> map) {
        writeInt(map.size());
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            write(entry.getKey());
            write(entry.getValue());
        }
    }

    //One Creator One Type
    public <K, V, R extends Map<K, V>> R readMap(R map, Class<K> keyType, Modelable.Creator<V> valueCreator) {
        return readMap(map, keyType, null, null, valueCreator);
    }

    public <K, V, R extends Map<K, V>> R readMap(R map, Modelable.Creator<K> keyCreator, Class<V> valueType) {
        return readMap(map, null, keyCreator, valueType, null);
    }

    public <K, V> HashMap<K, V> readMap(Class<K> keyType, Modelable.Creator<V> valueCreator) {
        return readMap(new HashMap<>(), keyType, valueCreator);
    }

    public <K, V> HashMap<K, V> readMap(Modelable.Creator<K> keyCreator, Class<V> valueType) {
        return readMap(new HashMap<>(), keyCreator, valueType);
    }

    //Two Creators or Types
    public <K, V, R extends Map<K, V>> R readMap(R map, Modelable.Creator<K> keyCreator, Modelable.Creator<V> valueCreator) {
        return readMap(map, null, keyCreator, null, valueCreator);
    }

    public <K, V, R extends Map<K, V>> R readMap(R map, Class<K> keyType, Class<V> valueType) {
        return readMap(map, keyType, null, valueType, null);
    }

    public <K, V> HashMap<K, V> readMap(Modelable.Creator<K> keyCreator, Modelable.Creator<V> valueCreator) {
        return readMap(new HashMap<>(), keyCreator, valueCreator);
    }

    public <K, V> HashMap<K, V> readMap(Class<K> keyType, Class<V> valueType) {
        return readMap(new HashMap<>(), keyType, valueType);
    }

    //Two Creators One Type
    public <K, V, R extends Map<K, V>> R readMap(R map, Modelable.Creator<K> keyCreator, Class<V> valueType, Modelable.Creator<V> valueCreator) {
        return readMap(map, null, keyCreator, valueType, valueCreator);
    }

    public <K, V, R extends Map<K, V>> R readMap(R map, Class<K> keyType, Modelable.Creator<K> keyCreator, Modelable.Creator<V> valueCreator) {
        return readMap(map, keyType, keyCreator, null, valueCreator);
    }

    public <K, V> HashMap<K, V> readMap(Modelable.Creator<K> keyCreator, Class<V> valueType, Modelable.Creator<V> valueCreator) {
        return readMap(new HashMap<>(), keyCreator, valueType, valueCreator);
    }

    public <K, V> HashMap<K, V> readMap(Class<K> keyType, Modelable.Creator<K> keyCreator, Modelable.Creator<V> valueCreator) {
        return readMap(new HashMap<>(), keyType, keyCreator, valueCreator);
    }

    //Two Types One Creator
    public <K, V, R extends Map<K, V>> R readMap(R map, Class<K> keyType, Class<V> valueType, Modelable.Creator<V> valueCreator) {
        return readMap(map, keyType, null, valueType, valueCreator);
    }

    public <K, V, R extends Map<K, V>> R readMap(R map, Class<K> keyType, Modelable.Creator<K> keyCreator, Class<V> valueType) {
        return readMap(map, keyType, keyCreator, valueType, null);
    }

    public <K, V> HashMap<K, V> readMap(Class<K> keyType, Class<V> valueType, Modelable.Creator<V> valueCreator) {
        return readMap(new HashMap<>(), keyType, valueType, valueCreator);
    }

    public <K, V> HashMap<K, V> readMap(Class<K> keyType, Modelable.Creator<K> keyCreator, Class<V> valueType) {
        return readMap(new HashMap<>(), keyType, keyCreator, valueType);
    }


    //Full
    public <K, V, R extends Map<K, V>> R readMap(R map, Class<K> keyType, Modelable.Creator<K> keyCreator, Class<V> valueType, Modelable.Creator<V> valueCreator) {
        int size = readInt();
        for (int i = 0; i < size; i++)
            map.put(read(keyType, keyCreator), read(valueType, valueCreator));
        return map;
    }

    private void write(Object value) {
        if (value instanceof Modelable)
            writeModelable((Modelable) value);
        else if (value instanceof Enum)
            writeInt(((Enum) value).ordinal());
        else
            writeObject(value);
    }

    private <T> T read(Class<T> type, Modelable.Creator<T> creator) {
        if (creator == null) {
            if (type != null && type.isEnum())
                return type.getEnumConstants()[readInt()];
            else
                return readObject(type);
        } else
            return readModelable(creator);
    }

    private JsonElement element() {
        return elements.get(index++);
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }


    @Override
    public String toString() {
        return gson.toJson(elements);
    }
}