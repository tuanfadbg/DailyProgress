package com.tuanfadbg.progress.ui.tag_manager;

import com.tuanfadbg.progress.database.tag.Tag;

public interface OnTagActionListener {
    void onDelete(Tag tag);
    void onEdit(Tag tag);
}
