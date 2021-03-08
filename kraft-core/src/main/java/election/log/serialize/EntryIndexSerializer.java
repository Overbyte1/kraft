package election.log.serialize;

import election.log.store.EntryIndexItem;

public interface EntryIndexSerializer {
    byte[] entryIndexToBytes(EntryIndexItem entryIndexItem);
    EntryIndexItem bytesToEntryIndexItem(byte[] bytes);
}
