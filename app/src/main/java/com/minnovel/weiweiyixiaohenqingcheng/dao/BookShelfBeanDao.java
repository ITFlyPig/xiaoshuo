package com.minnovel.weiweiyixiaohenqingcheng.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.minnovel.weiweiyixiaohenqingcheng.bean.BookShelfBean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "BOOK_SHELF_BEAN".
*/
public class BookShelfBeanDao extends AbstractDao<BookShelfBean, String> {

    public static final String TABLENAME = "BOOK_SHELF_BEAN";

    /**
     * Properties of entity BookShelfBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property NoteUrl = new Property(0, String.class, "noteUrl", true, "NOTE_URL");
        public final static Property DurChapter = new Property(1, int.class, "durChapter", false, "DUR_CHAPTER");
        public final static Property DurChapterPage = new Property(2, int.class, "durChapterPage", false, "DUR_CHAPTER_PAGE");
        public final static Property FinalDate = new Property(3, long.class, "finalDate", false, "FINAL_DATE");
        public final static Property Tag = new Property(4, String.class, "tag", false, "TAG");
    }


    public BookShelfBeanDao(DaoConfig config) {
        super(config);
    }
    
    public BookShelfBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"BOOK_SHELF_BEAN\" (" + //
                "\"NOTE_URL\" TEXT PRIMARY KEY NOT NULL ," + // 0: noteUrl
                "\"DUR_CHAPTER\" INTEGER NOT NULL ," + // 1: durChapter
                "\"DUR_CHAPTER_PAGE\" INTEGER NOT NULL ," + // 2: durChapterPage
                "\"FINAL_DATE\" INTEGER NOT NULL ," + // 3: finalDate
                "\"TAG\" TEXT);"); // 4: tag
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"BOOK_SHELF_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, BookShelfBean entity) {
        stmt.clearBindings();
 
        String noteUrl = entity.getNoteUrl();
        if (noteUrl != null) {
            stmt.bindString(1, noteUrl);
        }
        stmt.bindLong(2, entity.getDurChapter());
        stmt.bindLong(3, entity.getDurChapterPage());
        stmt.bindLong(4, entity.getFinalDate());
 
        String tag = entity.getTag();
        if (tag != null) {
            stmt.bindString(5, tag);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, BookShelfBean entity) {
        stmt.clearBindings();
 
        String noteUrl = entity.getNoteUrl();
        if (noteUrl != null) {
            stmt.bindString(1, noteUrl);
        }
        stmt.bindLong(2, entity.getDurChapter());
        stmt.bindLong(3, entity.getDurChapterPage());
        stmt.bindLong(4, entity.getFinalDate());
 
        String tag = entity.getTag();
        if (tag != null) {
            stmt.bindString(5, tag);
        }
    }

    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }    

    @Override
    public BookShelfBean readEntity(Cursor cursor, int offset) {
        BookShelfBean entity = new BookShelfBean( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // noteUrl
            cursor.getInt(offset + 1), // durChapter
            cursor.getInt(offset + 2), // durChapterPage
            cursor.getLong(offset + 3), // finalDate
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4) // tag
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, BookShelfBean entity, int offset) {
        entity.setNoteUrl(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setDurChapter(cursor.getInt(offset + 1));
        entity.setDurChapterPage(cursor.getInt(offset + 2));
        entity.setFinalDate(cursor.getLong(offset + 3));
        entity.setTag(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
     }
    
    @Override
    protected final String updateKeyAfterInsert(BookShelfBean entity, long rowId) {
        return entity.getNoteUrl();
    }
    
    @Override
    public String getKey(BookShelfBean entity) {
        if(entity != null) {
            return entity.getNoteUrl();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(BookShelfBean entity) {
        return entity.getNoteUrl() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
