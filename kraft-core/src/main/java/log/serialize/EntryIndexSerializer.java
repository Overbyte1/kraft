package log.serialize;

import log.store.EntryIndexItem;

public interface EntryIndexSerializer {
    byte[] entryIndexToBytes(EntryIndexItem entryIndexItem);
    EntryIndexItem bytesToEntryIndexItem(byte[] bytes);
}
