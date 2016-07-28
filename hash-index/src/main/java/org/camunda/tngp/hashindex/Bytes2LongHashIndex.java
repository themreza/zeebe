package org.camunda.tngp.hashindex;

import static uk.co.real_logic.agrona.BitUtil.SIZE_OF_LONG;

import org.camunda.tngp.hashindex.store.IndexStore;
import org.camunda.tngp.hashindex.types.ByteArrayKeyHandler;
import org.camunda.tngp.hashindex.types.LongValueHandler;

import uk.co.real_logic.agrona.DirectBuffer;

public class Bytes2LongHashIndex extends HashIndex<ByteArrayKeyHandler, LongValueHandler>
{
    public Bytes2LongHashIndex(
            final IndexStore indexStore,
            final int indexSize,
            final int blockLength,
            final int keyLength)
    {
        super(indexStore, ByteArrayKeyHandler.class, LongValueHandler.class, indexSize, blockLength, keyLength, SIZE_OF_LONG);
    }

    public Bytes2LongHashIndex(IndexStore indexStore)
    {
        super(indexStore, ByteArrayKeyHandler.class, LongValueHandler.class);
    }

    public long get(byte[] key, long missingValue, long dirtyValue)
    {
        checkKeyLength(key.length);
        keyHandler.setKey(key);

        if (dirtyKeys.getAtCurrentKey(-1) > 0)
        {
            return dirtyValue;
        }

        valueHandler.theValue = missingValue;
        get();
        return valueHandler.theValue;
    }

    public long get(DirectBuffer buffer, int offset, int length, long missingValue, long dirtyValue)
    {
        checkKeyLength(length);
        keyHandler.setKey(buffer, offset, length);

        if (dirtyKeys.getAtCurrentKey(-1) > 0)
        {
            return dirtyValue;
        }

        valueHandler.theValue = missingValue;
        get();
        return valueHandler.theValue;
    }

    public boolean put(byte[] key, long value)
    {
        checkKeyLength(key.length);

        keyHandler.setKey(key);
        valueHandler.theValue = value;
        return put();
    }

    public boolean put(DirectBuffer buffer, int offset, int length, long value)
    {
        checkKeyLength(length);

        keyHandler.setKey(buffer, offset, length);
        valueHandler.theValue = value;
        return put();
    }

    public long remove(byte[] key, long missingValue)
    {
        checkKeyLength(key.length);

        keyHandler.setKey(key);
        valueHandler.theValue = missingValue;
        remove();
        return valueHandler.theValue;
    }

    public long remove(DirectBuffer buffer, int offset, int length, long missingValue)
    {
        checkKeyLength(length);

        keyHandler.setKey(buffer, offset, length);
        valueHandler.theValue = missingValue;
        remove();
        return valueHandler.theValue;
    }

    public void markDirty(byte[] key)
    {
        this.keyHandler.setKey(key);
        dirtyKeys.incrementAtCurrentKey();
    }

    public void markDirty(DirectBuffer buffer, int offset, int length)
    {
        keyHandler.setKey(buffer, offset, length);
        dirtyKeys.incrementAtCurrentKey();
    }

    public void resolveDirty(byte[] key)
    {
        this.keyHandler.setKey(key);
        dirtyKeys.decrementAtCurrentKey();
    }

    public void resolveDirty(DirectBuffer buffer, int offset, int length)
    {
        keyHandler.setKey(buffer, offset, length);
        dirtyKeys.decrementAtCurrentKey();
    }

    protected void checkKeyLength(int providedKeyLength)
    {
        if (providedKeyLength > recordKeyLength())
        {
            throw new IllegalArgumentException("Illegal byte array length: expected at most " + recordKeyLength() + ", got " + providedKeyLength);
        }
    }
}
