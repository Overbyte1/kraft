package log.serialize;

import junit.framework.TestCase;
import log.entry.EmptyEntry;
import log.entry.Entry;

public class EmptyEntrySerializerTest extends TestCase {

    public void testEntryToBytes() {
        EmptyEntry entry = new EmptyEntry(5678, 1);
        EmptyEntrySerializer serializer = new EmptyEntrySerializer();
        byte[] bytes = serializer.entryToBytes(entry);
        Entry entry1 = serializer.bytesToEntry(bytes);
        assertEquals(entry, entry1);
    }

}