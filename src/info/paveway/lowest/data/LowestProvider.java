package info.paveway.lowest.data;

import info.paveway.log.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

/**
 * 最低価格記録アプリ
 * コンテントプロバイダークラス
 *
 * @version 1.0 新規作成
 */
public class LowestProvider extends ContentProvider {

    /** ロガー */
    private Logger mLogger = new Logger(LowestProvider.class);

    /** Authority */
    public static final String AUTHORITY = LowestProvider.class.getName().toLowerCase(Locale.getDefault());

    /** ベースコンテントURI文字列 */
    private static final String BASE_CONTENT_URI = "content://" + AUTHORITY + "/";

    /** カテゴリ */
    public static final Uri CATEGORY_CONTENT_URI = Uri.parse(BASE_CONTENT_URI + TableName.CATEGORY);

    /** 商品コンテントURI */
    public static final Uri GOODS_CONTENT_URI = Uri.parse(BASE_CONTENT_URI + TableName.GOODS);

    /** 店コンテントURI */
    public static final Uri SHOP_CONTENT_URI = Uri.parse(BASE_CONTENT_URI + TableName.SHOP);

    /** 価格コンテントURI */
    public static final Uri PRICE_CONTENT_URI = Uri.parse(BASE_CONTENT_URI + TableName.PRICE);

    /** コンテントタイプ */
    protected static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + AUTHORITY;

    /** コンテントアイテムタイプ */
    protected static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item" + AUTHORITY;

    /** テーブル名 */
    private class TableName {
        /** カテゴリ */
        private static final String CATEGORY = "category";

        /** 商品 */
        private static final String GOODS = "goods";

        /** 店 */
        private static final String SHOP = "shop";

        /** 価格 */
        private static final String PRICE = "price";
    }

    /** カテゴリテーブル項目 */
    public class CategoryTable {
        /** カテゴリID */
        public static final String ID = BaseColumns._ID;

        /** カテゴリ名 */
        public static final String NAME = "name";

        /** 更新日時 */
        public static final String UPDATE_TIME = "update_time";
    }

    /** 商品テーブル項目 */
    public class GoodsTable {
        /** 商品ID */
        public static final String ID = BaseColumns._ID;

        /** 商品名 */
        public static final String NAME = "name";

        /** カテゴリID */
        public static final String CATEGORY_ID = "category_id";

        /** カテゴリ名 */
        public static final String CATEGORY_NAME = "category_name";

        /** メモ */
        public static final String MEMO = "memo";

        /** 更新日時 */
        public static final String UPDATE_TIME = "update_time";
    }

    /** 店テーブル項目 */
    public class ShopTable {
        /** 店ID */
        public static final String ID = BaseColumns._ID;

        /** 店名 */
        public static final String NAME = "name";

        /** 更新日時 */
        public static final String UPDATE_TIME = "update_time";
    }

    /** 価格テーブル項目 */
    public class PriceTable {
        /** 価格ID */
        public static final String ID = BaseColumns._ID;

        /** カテゴリID */
        public static final String CATEGORY_ID = "category_id";

        /** カテゴリ名 */
        public static final String CATEGORY_NAME = "category_name";

        /** 商品ID */
        public static final String GOODS_ID = "goods_id";

        /** 商品名 */
        public static final String GOODS_NAME = "goods_name";

        /** 店ID */
        public static final String SHOP_ID = "shop_id";

        /** 店名 */
        public static final String SHOP_NAME = "shop_name";

        /** 数量 */
        public static final String QUANTITY = "quantity";

        /** 価格 */
        public static final String PRICE = "price";

        /** メモ */
        public static final String MEMO = "memo";

        /** 更新日時 */
        public static final String UPDATE_TIME = "update_time";
    }

    /** URIマッチャーコード:カテゴリテーブル全行選択 */
    private static final int CATEGORY_ALL_ROWS = 1;

    /** URIマッチャーコード:カテゴリテーブル個別行選択 */
    private static final int CATEGORY_ROW = 2;

    /** URIマッチャーコード:商品テーブル全行選択 */
    private static final int GOODS_ALL_ROWS = 3;

    /** URIマッチャーコード:商品テーブル個別行選択 */
    private static final int GOODS_ROW = 4;

    /** URIマッチャーコード:店テーブル全行選択 */
    private static final int SHOP_ALL_ROWS = 5;

    /** URIマッチャーコード:店テーブル個別行選択 */
    private static final int SHOP_ROW = 6;

    /** URIマッチャーコード:価格テーブル全行選択 */
    private static final int PRICE_ALL_ROWS = 7;

    /** URIマッチャーコード:価格テーブル個別行選択 */
    private static final int PRICE_ROW = 8;

    /** URIマッチャー */
    private static UriMatcher mUriMatcher;
    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, TableName.CATEGORY,        CATEGORY_ALL_ROWS);
        mUriMatcher.addURI(AUTHORITY, TableName.CATEGORY + "/#", CATEGORY_ROW);
        mUriMatcher.addURI(AUTHORITY, TableName.GOODS,           GOODS_ALL_ROWS);
        mUriMatcher.addURI(AUTHORITY, TableName.GOODS    + "/#", GOODS_ROW);
        mUriMatcher.addURI(AUTHORITY, TableName.SHOP,            SHOP_ALL_ROWS);
        mUriMatcher.addURI(AUTHORITY, TableName.SHOP     + "/#", SHOP_ROW);
        mUriMatcher.addURI(AUTHORITY, TableName.PRICE,           PRICE_ALL_ROWS);
        mUriMatcher.addURI(AUTHORITY, TableName.PRICE    + "/#", PRICE_ROW);
    }

    /** カテゴリプロジェクションマップ */
    private static Map<String, String> mCategoryProjectionMap;
    static {
        mCategoryProjectionMap = new HashMap<String, String>();
        mCategoryProjectionMap.put(CategoryTable.ID,          CategoryTable.ID);
        mCategoryProjectionMap.put(CategoryTable.NAME,        CategoryTable.NAME);
        mCategoryProjectionMap.put(CategoryTable.UPDATE_TIME, CategoryTable.UPDATE_TIME);
    }

    /** 商品プロジェクションマップ */
    private static Map<String, String> mGoodsProjectionMap;
    static {
        mGoodsProjectionMap = new HashMap<String, String>();
        mGoodsProjectionMap.put(GoodsTable.ID,            GoodsTable.ID);
        mGoodsProjectionMap.put(GoodsTable.NAME,          GoodsTable.NAME);
        mGoodsProjectionMap.put(GoodsTable.CATEGORY_ID,   GoodsTable.CATEGORY_ID);
        mGoodsProjectionMap.put(GoodsTable.CATEGORY_NAME, GoodsTable.CATEGORY_NAME);
        mGoodsProjectionMap.put(GoodsTable.MEMO,          GoodsTable.MEMO);
        mGoodsProjectionMap.put(GoodsTable.UPDATE_TIME,   GoodsTable.UPDATE_TIME);
    }

    /** 店プロジェクションマップ */
    private static Map<String, String> mShopProjectionMap;
    static {
        mShopProjectionMap = new HashMap<String, String>();
        mShopProjectionMap.put(ShopTable.ID,          ShopTable.ID);
        mShopProjectionMap.put(ShopTable.NAME,        ShopTable.NAME);
        mShopProjectionMap.put(ShopTable.UPDATE_TIME, ShopTable.UPDATE_TIME);
    }

    /** 価格プロジェクションマップ */
    private static Map<String, String> mPriceProjectionMap;
    static {
        mPriceProjectionMap = new HashMap<String, String>();
        mPriceProjectionMap.put(PriceTable.ID,            PriceTable.ID);
        mPriceProjectionMap.put(PriceTable.CATEGORY_ID,   PriceTable.CATEGORY_ID);
        mPriceProjectionMap.put(PriceTable.CATEGORY_NAME, PriceTable.CATEGORY_NAME);
        mPriceProjectionMap.put(PriceTable.GOODS_ID,      PriceTable.GOODS_ID);
        mPriceProjectionMap.put(PriceTable.GOODS_NAME,    PriceTable.GOODS_NAME);
        mPriceProjectionMap.put(PriceTable.SHOP_ID,       PriceTable.SHOP_ID);
        mPriceProjectionMap.put(PriceTable.SHOP_NAME,     PriceTable.SHOP_NAME);
        mPriceProjectionMap.put(PriceTable.QUANTITY,      PriceTable.QUANTITY);
        mPriceProjectionMap.put(PriceTable.PRICE,         PriceTable.PRICE);
        mPriceProjectionMap.put(PriceTable.MEMO,          PriceTable.MEMO);
        mPriceProjectionMap.put(PriceTable.UPDATE_TIME,   PriceTable.UPDATE_TIME);
    }

    /** コンテントリゾルバー */
    private ContentResolver mResolver;

    /** データヘルパー */
    private DataHelper mDataHelper;

    /**
     * 生成した時に呼び出される。
     *
     * @return 処理結果
     */
    @Override
    public boolean onCreate() {
        mLogger.d("IN");

        Context context = getContext();
        mResolver = context.getContentResolver();
        mDataHelper = new DataHelper(context);

        mLogger.d("OUT(OK) result=[true]");
        return true;
    }

    /**
     * バッチ処理
     *
     * @param operations 操作
     * @return 結果
     * @throws OperationApplicationException 操作例外
     */
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        mLogger.d("IN");

        // 書き込み用データベースを取得する。
        SQLiteDatabase db = mDataHelper.getWritableDatabase();

        // トランザクションを開始する。
        db.beginTransaction();
        try {
            // バッチ処理を行う。
            ContentProviderResult[] results = super.applyBatch(operations);

            // トランザクション成功とする。
            db.setTransactionSuccessful();

            // 結果を返却する。
            mLogger.d("OUT(OK)");
            return results;
        } finally {
            // トランザクションを終了する。
            db.endTransaction();
        }
    }

    /**
     * クエリーを行う。
     *
     * @param uri クエリーを行うテーブルのURI
     * @param projection セレクトする項目名の配列
     * @param selection 選択条件
     * @param selectionArgs 選択条件の値の配列
     * @param sortOrder ソート順
     * @return 検索結果のカーソル
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        mLogger.d("IN uri=[" + uri + "] selection=[" + selection + "]");

        // クエリービルダーを生成する。
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        // URIにより処理を判別する。
        switch (mUriMatcher.match(uri)) {
        // カテゴリテーブルの個別行選択の場合
        case CATEGORY_ROW:
            // 検索条件を付加する。
            qb.appendWhere(CategoryTable.ID + "=" + getId(uri));
        // カテゴリテーブルの全行選択の場合
        case CATEGORY_ALL_ROWS:
            // テーブルを設定する。
            qb.setTables(TableName.CATEGORY);
            // プロジェクションマップを設定する。
            qb.setProjectionMap(mCategoryProjectionMap);
            break;

        // 商品テーブルの個別行選択の場合
        case GOODS_ROW:
            // 検索条件を付加する。
            qb.appendWhere(GoodsTable.ID + "=" + getId(uri));
        // 商品テーブルの全行選択の場合
        case GOODS_ALL_ROWS:
            // テーブルを設定する。
            qb.setTables(TableName.GOODS);
            // プロジェクションマップを設定する。
            qb.setProjectionMap(mGoodsProjectionMap);
            break;

        // 店テーブルの個別行選択の場合
        case SHOP_ROW:
            // 検索条件を付加する。
            qb.appendWhere(ShopTable.ID + "=" + getId(uri));
        // 店テーブルの全行選択の場合
        case SHOP_ALL_ROWS:
            // テーブルを設定する。
            qb.setTables(TableName.SHOP);
            // プロジェクションマップを設定する。
            qb.setProjectionMap(mShopProjectionMap);
            break;

        // 価格テーブルの個別行選択の場合
        case PRICE_ROW:
            // 検索条件を付加する。
            qb.appendWhere(PriceTable.ID + "=" + getId(uri));
        // 価格テーブルの全行選択の場合
        case PRICE_ALL_ROWS:
            // テーブルを設定する。
            qb.setTables(TableName.PRICE);
            // プロジェクションマップを設定する。
            qb.setProjectionMap(mPriceProjectionMap);
            break;

        // 上記以外
        default:
            // エラーで終了する。
            mLogger.e("OUT(NG) Unknown URI=[" + uri + "]");
            throw new IllegalArgumentException();
        }

        // 読み出し用データベースを取得する。
        SQLiteDatabase db = mDataHelper.getReadableDatabase();

        // クエリーを実行する。
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // クエリーの実行を通知する。
        c.setNotificationUri(mResolver, uri);

        mLogger.d("OUT(OK)");
        return c;
    }

    /**
     * データを新規登録する。
     *
     * @param uri 新規登録するテーブルのURI
     * @param values 新規登録するデータ
     * @return 新規登録したデータのURI
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        mLogger.d("IN uri=[" + uri + "]");

        String tableName = null;
        // URIにより処理を判別する。
        switch (mUriMatcher.match(uri)) {
        // カテゴリテーブルの全行選択の場合
        case CATEGORY_ALL_ROWS:
            tableName = TableName.CATEGORY;
            break;

        // 商品テーブルの全行選択の場合
        case GOODS_ALL_ROWS:
            tableName = TableName.GOODS;
            break;

        // 店テーブルの全行選択の場合
        case SHOP_ALL_ROWS:
            tableName = TableName.SHOP;
            break;

        // 価格テーブルの全行選択の場合
        case PRICE_ALL_ROWS:
            tableName = TableName.PRICE;
            break;

        // 上記以外
        default:
            // エラーで終了する。
            mLogger.e("OUT(NG) Unknown URI=[" + uri + "]");
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // 書き込み用データベースを取得する。
        SQLiteDatabase db = mDataHelper.getWritableDatabase();

        // データを新規登録する。
        long rowId = db.insert(tableName, null, values);
        mLogger.d("rowId=[" + rowId + "]");

        // データを新規登録できた場合
        if (rowId > 0) {
            // 新規登録したデータのURIを取得する。
            Uri returnUri = ContentUris.withAppendedId(uri, rowId);

            // データの新規登録を通知する。
            mResolver.notifyChange(returnUri, null);
            mLogger.d("OUT(OK) result=[" + returnUri + "]");
            return returnUri;
        }

        // データを新規登録できなかった場合、エラーで終了する。
        mLogger.e("OUT(NG) Failed to insert row into uri=[" + uri + "]");
        throw new SQLException("Failed to insert row into uri=[" + uri + "]");
    }

    /**
     * データを更新する。
     *
     * @param uri 更新するテーブルのURI
     * @param values 更新するデータ
     * @param selection 選択条件
     * @param selectionArgs 選択条件の値の配列
     * @return 更新したデータ数
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        mLogger.d("IN uri=[" + uri + "] selection=[" + selection + "]");

        String tableName = null;
        // URIにより処理を判別する。
        switch (mUriMatcher.match(uri)) {
        // カテゴリテーブルの個別行選択の場合
        case CATEGORY_ROW:
            selection = createSelection(uri, selection);
        // カテゴリテーブルの全行選択の場合
        case CATEGORY_ALL_ROWS:
            tableName = TableName.CATEGORY;
            break;

        // 商品テーブルの個別行選択の場合
        case GOODS_ROW:
            selection = createSelection(uri, selection);
        // 商品テーブルの全行選択の場合
        case GOODS_ALL_ROWS:
            tableName = TableName.GOODS;
            break;

        // 店テーブルの個別行選択の場合
        case SHOP_ROW:
            selection = createSelection(uri, selection);
        // 店テーブルの全行選択の場合
        case SHOP_ALL_ROWS:
            tableName = TableName.SHOP;
            break;

        // 価格テーブルの個別行選択の場合
        case PRICE_ROW:
            selection = createSelection(uri, selection);
        // 価格テーブルの全行選択の場合
        case PRICE_ALL_ROWS:
            tableName = TableName.PRICE;
            break;

        // 上記以外
        default:
            // エラーで終了する。
            mLogger.e("OUT(NG) Unknown URI=[" + uri + "]");
            throw new IllegalArgumentException("Unknown URI=[" + uri + "]");
        }

        // 書き込み用データベースを取得する。
        SQLiteDatabase db = mDataHelper.getWritableDatabase();

        // データを更新する。
        int count = db.update(tableName, values, selection, selectionArgs);

        // データ更新を通知する。
        mResolver.notifyChange(uri, null);

        mLogger.d("OUT(OK) count=[" + count + "]");
        return count;
    }

    /**
     * データを削除する。
     *
     * @param uri 削除するテーブルのURI
     * @param selection 選択条件
     * @param selectionArgs 選択条件の値の配列
     * @return 削除したデータ数
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        mLogger.d("IN uri=[" + uri + "] selection=[" + selection + "]");

        String tableName = null;

        // URIにより処理を判別する。
        switch (mUriMatcher.match(uri)) {
        // カテゴリテーブルの個別行選択の場合
        case CATEGORY_ROW:
            selection = createSelection(uri, selection);
        // カテゴリテーブルの全行選択の場合
        case CATEGORY_ALL_ROWS:
            tableName = TableName.CATEGORY;
            break;

        // 商品テーブルの個別行選択の場合
        case GOODS_ROW:
            selection = createSelection(uri, selection);
        // 商品テーブルの全行選択の場合
        case GOODS_ALL_ROWS:
            tableName = TableName.GOODS;
            break;

        // 店テーブルの個別行選択の場合
        case SHOP_ROW:
            selection = createSelection(uri, selection);
        // 店テーブルの全行選択の場合
        case SHOP_ALL_ROWS:
            tableName = TableName.SHOP;
            break;

        // 価格テーブルの個別行選択の場合
        case PRICE_ROW:
            selection = createSelection(uri, selection);
        // 価格テーブルの全行選択の場合
        case PRICE_ALL_ROWS:
            tableName = TableName.PRICE;
            break;

        // 上記以外
        default:
            // エラーで終了する。
            mLogger.e("OUT(NG) Unknown URI=[" + uri + "]");
            throw new IllegalArgumentException("Unknown URI=[" + uri + "]");
        }

        // 書き込み用データベースを取得する。
        SQLiteDatabase db = mDataHelper.getWritableDatabase();

        // データを削除する。
        int count = db.delete(tableName, selection, selectionArgs);

        // データ削除を通知する。
        mResolver.notifyChange(uri, null);

        // 削除した行数を返却する。
        mLogger.d("OUT(OK) count=[" + count + "]");
        return count;
    }

    @Override
    public String getType(Uri uri) {
        mLogger.d("IN uri=[" + uri + "]");

        // URIにより処理を判別する。
        switch (mUriMatcher.match(uri)) {
        // 全行選択の場合
        case CATEGORY_ALL_ROWS:
        case GOODS_ALL_ROWS:
        case SHOP_ALL_ROWS:
        case PRICE_ALL_ROWS:
            mLogger.d("OUT(OK) result=[" + CONTENT_TYPE + "]");
            return CONTENT_TYPE;

        // 個別行選択の場合
        case CATEGORY_ROW:
        case GOODS_ROW:
        case SHOP_ROW:
        case PRICE_ROW:
            mLogger.d("OUT(OK) result=[" + CONTENT_ITEM_TYPE + "]");
            return CONTENT_ITEM_TYPE;

        // 上記以外
        default:
            // エラーで終了する。
            mLogger.e("OUT(NG) Unknown URI=[" + uri + "]");
            throw new IllegalArgumentException("Unknown URI=[" + uri + "]");
        }
    }

    /**
     * IDを返却する。
     *
     * @param uri URI
     * @return ID
     */
    private String getId(Uri uri) {
        mLogger.d("IN uri=[" + uri + "]");

        String id = uri.getPathSegments().get(1);

        mLogger.d("OUT(OK) result=[" + id + "]");
        return id;
    }

    /**
     * 選択条件のSQL文字列を生成する。
     *
     * @param uri URI
     * @param 選択条件
     * @return 選択条件のSQL文字列
     */
    private String createSelection(Uri uri, String selection) {
        mLogger.d("IN uri=[" + uri + "] selection=[" + selection + "]");

        String id = getId(uri);
        String result = BaseColumns._ID + "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");

        mLogger.d("OUT(OK) result=[" + result + "]");
        return result;
    }

    /**************************************************************************/
    /**
     * データヘルパークラス
     *
     */
    private class DataHelper extends SQLiteOpenHelper {

        /** ロガー */
        private Logger mLogger = new Logger(DataHelper.class);

        /** データベースバージョン */
        private static final int DB_VERSION = 1;

        /** カテゴリテーブル生成SQL */
        private static final String CREATE_CATEGORY_TABLE_SQL =
                "CREATE TABLE IF NOT EXISTS " + TableName.CATEGORY +
                " (" +
                    CategoryTable.ID          + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    CategoryTable.NAME        + " TEXT, "    +
                    CategoryTable.UPDATE_TIME + " INTEGER"   +
                ");";

        /** 商品テーブル生成SQL */
        private static final String CREATE_GOODS_TABLE_SQL =
                "CREATE TABLE IF NOT EXISTS " + TableName.GOODS +
                " (" +
                    GoodsTable.ID            + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    GoodsTable.NAME          + " TEXT, "    +
                    GoodsTable.CATEGORY_ID   + " INTEGER, " +
                    GoodsTable.CATEGORY_NAME + " TEXT, "    +
                    GoodsTable.MEMO          + " TEXT, "    +
                    GoodsTable.UPDATE_TIME   + " INTEGER"   +
                ");";

        /** 店テーブル生成SQL */
        private static final String CREATE_SHOP_TABLE_SQL =
                "CREATE TABLE IF NOT EXISTS " + TableName.SHOP +
                " (" +
                    ShopTable.ID          + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ShopTable.NAME        + " TEXT, "    +
                    ShopTable.UPDATE_TIME + " INTEGER"   +
                ");";

        /** 価格テーブル生成SQL */
        private static final String CREATE_PRICE_TABLE_SQL =
                "CREATE TABLE IF NOT EXISTS " + TableName.PRICE +
                " (" +
                    PriceTable.ID            + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    PriceTable.CATEGORY_ID   + " INTEGER, " +
                    PriceTable.CATEGORY_NAME + " TEXT, "    +
                    PriceTable.GOODS_ID      + " INTEGER, " +
                    PriceTable.GOODS_NAME    + " TEXT, "    +
                    PriceTable.SHOP_ID       + " INTEGER, " +
                    PriceTable.SHOP_NAME     + " TEXT, "    +
                    PriceTable.QUANTITY      + " REAL, "    +
                    PriceTable.PRICE         + " INTEGER, " +
                    PriceTable.MEMO          + " TEXT, "    +
                    PriceTable.UPDATE_TIME   + " INTEGER"   +
                ");";

        /**
         * コンストラクタ
         * スーパークラスのコンストラクタを呼び出す。
         */
        public DataHelper(Context context) {
            // スーパークラスのコンストラクタを呼び出す。
            super(context, null, null, DB_VERSION);
            mLogger.d("IN");
            mLogger.d("OUT(OK)");
        }

        /**
         * 生成した時に呼び出される。
         *
         * @param db データベース
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            mLogger.d("IN");

            // テーブルを生成する。
            db.execSQL(CREATE_CATEGORY_TABLE_SQL);
            db.execSQL(CREATE_GOODS_TABLE_SQL);
            db.execSQL(CREATE_SHOP_TABLE_SQL);
            db.execSQL(CREATE_PRICE_TABLE_SQL);

            mLogger.d("OUT(OK)");
        }

        /**
         * アップグレードした時に呼び出される。
         *
         * @param db データベース
         * @param oldVersion 旧バージョン
         * @param newVersion 新バージョン
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            mLogger.d("IN");

            // 生成した時と同様の処理を行う。
            onCreate(db);

            mLogger.d("OUT(OK)");
        }
    }
}
