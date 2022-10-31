// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.fragments.list.search.filter;

import org.schabi.newpipe.extractor.search.filter.FilterItem;

public abstract class BaseItemWrapper implements SearchFilterLogic.IUiItemWrapper {

    protected final FilterItem item;

    protected BaseItemWrapper(final FilterItem item) {
        this.item = item;
    }

    @Override
    public int getItemId() {
        return item.getIdentifier();
    }
}
