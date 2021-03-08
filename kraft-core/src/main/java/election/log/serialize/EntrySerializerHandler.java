package election.log.serialize;

import election.log.entry.Entry;

import java.util.Map;

public class EntrySerializerHandler implements EntrySerializer {
    private Map<Integer, EntrySerializer> entrySerializerMap;
    private Map<Class, EntrySerializer> classEntrySerializerMap;

    private EntrySerializerHandler(){}

    @Override
    public byte[] entryToBytes(Entry entry) {
        EntrySerializer entrySerializer = classEntrySerializerMap.get(entry.getClass());
        return entrySerializer.entryToBytes(entry);
    }

    @Override
    public Entry bytesToEntry(byte[] bytes) {
        int type = 0;
        type |= bytes[0];
        type |= (bytes[1] << 8);
        type |= (bytes[2] << 16);
        type |= (bytes[3] << 24);
        EntrySerializer entrySerializer = entrySerializerMap.get(type);
        return entrySerializer.bytesToEntry(bytes);
    }
    public void register(int type, Class<?> entryClass, EntrySerializer entrySerializer) {
        entrySerializerMap.put(type, entrySerializer);
        classEntrySerializerMap.put(entryClass, entrySerializer);
    }
    public void unregister(int type, Class<?> entryClass, EntrySerializer entrySerializer) {
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
