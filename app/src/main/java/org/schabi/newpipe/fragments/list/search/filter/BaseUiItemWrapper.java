// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.fragments.list.search.filter;

import android.view.View;

import org.schabi.newpipe.extractor.search.filter.FilterItem;

public abstract class BaseUiItemWrapper implements SearchFilterLogic.IUiItemWrapper {
    protected final FilterItem item;
    protected final int groupId;
    protected View view;

    protected BaseUiItemWrapper(final FilterItem item,
                                final int groupId,
                                final View view) {
        this.item = item;
        this.groupId = groupId;
        this.view = view;
    }

    @Override
    public void setVisible(final boolean visible) {
        if (visible) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemId() {
        return item.getIdentifier();
    }

    @Override
    public int getGroupId() {
        return this.groupId;
    }
}
