package com.enderio.core.common.util.blockiterators;

import java.util.Iterator;

import com.enderio.core.common.util.BlockCoord;

public abstract class AbstractBlockIterator implements Iterable<BlockCoord>, Iterator<BlockCoord> {

    protected BlockCoord base;

    protected AbstractBlockIterator(BlockCoord base) {
        this.base = base;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("You can't remove blocks silly!");
    }

    @Override
    public Iterator<BlockCoord> iterator() {
        return this;
    }
}
