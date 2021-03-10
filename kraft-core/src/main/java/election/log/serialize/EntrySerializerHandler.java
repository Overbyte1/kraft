package election.log.serialize;

import election.log.entry.Entry;

import java.util.HashMap;
import java.util.Map;

public class EntrySerializerHandler implements EntrySerializer {
    private Map<Integer, SerializableEntry> entrySerializerMap;
    private Map<Class, SerializableEntry> classEntrySerializerMap;

    private EntrySerializerHandler(){
        entrySerializerMap = new HashMap<>();
        classEntrySerializerMap = new HashMap<>();
    }

    public boolean isRegistered(Class<?> clazz) {
        return classEntrySerializerMap.get(clazz) != null;
    }

    @Override
    public byte[] entryToBytes(Entry entry) {
        SerializableEntry entrySerializer = classEntrySerializerMap.get(entry.getClass());
        return entrySerializer.entryToBytes(entry);
    }

    @Override
    public Entry bytesToEntry(byte[] bytes) {
//        int type = 0;
//        type |= bytes[0];
//        type |= (bytes[1] << 8);
//        type |= (bytes[2] << 16);
//        type |= (bytes[3] << 24);
        int type = ByteArrayConverter.readInt(bytes, 0);
        SerializableEntry entrySerializer = entrySerializerMap.get(type);
        return entrySerializer.bytesToEntry(bytes);
    }
    public void register(int type, Class<?> entryClass, SerializableEntry entrySerializer) {
        entrySerializerMap.put(type, entrySerializer);
        classEntrySerializerMap.put(entryClass, entrySerializer);
    }
    public void unregister(int type, Class<?> entryClass) {
        entrySerializerMap.remove(type);
        classEntrySerializerMap.remove(entryClass);
    }
    public static EntrySerializerHandler getInstance() {
        return SingleTon.INSTANCE;
    }

    static class SingleTon {
        private static EntrySerializerHandler INSTANCE = new EntrySerializerHandler();
    }
}
