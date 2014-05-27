package info.paveway.lowest.data;

import info.paveway.log.Logger;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 最低価格
 * 店データプロバイダークラス
 *
 * @version 1.0 新規作成
 *
 */
@SuppressLint("DefaultLocale")
public class ShopProvider extends AbstractBaseProvider {

    /** ロガー */
    private Logger mLogger = new Logger(ShopProvider.class);

    /** テーブル名 */
    private static final String TABLE_NAME = "history";

    /** コンテントプロバイダ識別子 */
    private static final String AUTHORITY = ShopProvider.class.getName().toLowerCase();

    /** コンテントURI */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

    /** コンテントタイプ */
    public static final String CONTENT_TYPE = CONTENT_TYPE_PREFIX + TABLE_NAME;

    /** コンテントアイテムタイプ */
    public static final String CONTENT_ITEM_TYPE = CONTENT_ITEM_TYPE_PREFIX + TABLE_NAME;

    /** 店IDカラム名 */
    public static final String SHOP_ID = BaseColumns._ID;

    /** 店名カラム名 */
    public static final String SHOP_NAME = "shop_name";

    /** テーブル生成SQL文 */
    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE " + TABLE_NAME            +
            " ("                                    +
                SHOP_ID   + " INTEGER PRIMARY KEY," +
                SHOP_NAME + " TEXT "                +
            ");";

    /**
     * 初期化処理を行う。
     */
    protected void init() {
        mLogger.d("IN");

        mTableName       = TABLE_NAME;
        mContentUri      = CONTENT_URI;
        mContentType     = CONTENT_TYPE;
        mContentItemType = CONTENT_ITEM_TYPE;
        mCreateTableSQL  = CREATE_TABLE_SQL;

        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, TABLE_NAME, ALL_ROWS);
        mUriMatcher.addURI(AUTHORITY, TABLE_NAME + "/#", ROW);

        mProjectionMap = new HashMap<String, String>();
        mProjectionMap.put(SHOP_ID,   SHOP_ID);
        mProjectionMap.put(SHOP_NAME, SHOP_NAME);

        mLogger.d("OUT(OK)");
    }
}
