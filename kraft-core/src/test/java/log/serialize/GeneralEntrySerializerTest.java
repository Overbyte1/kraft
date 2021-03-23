package log.serialize;

import junit.framework.TestCase;
import log.entry.Entry;
import log.entry.GeneralEntry;

public class GeneralEntrySerializerTest extends TestCase {

    public void testEntryToBytes() {
        GeneralEntry entry = new GeneralEntry(43267, 54322, new byte[]{1,3,5,2,0});
        GeneralEntrySerializer serializer = new GeneralEntrySerializer();
        byte[] bytes = serializer.entryToBytes(entry);
        Entry entry1 = serializer.bytesToEntry(bytes);
        assertEquals(entry, entry1);
    }
}