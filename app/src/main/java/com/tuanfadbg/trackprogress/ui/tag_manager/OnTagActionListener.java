package com.tuanfadbg.trackprogress.ui.tag_manager;

import com.tuanfadbg.trackprogress.database.tag.Tag;

public interface OnTagActionListener {
    void onDelete(Tag tag);
    void onEdit(Tag tag);
}
