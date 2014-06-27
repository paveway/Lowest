package info.paveway.lowest;

import info.paveway.log.Logger;
import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.data.LowestProvider;
import info.paveway.lowest.data.LowestProvider.ShopTable;
import info.paveway.lowest.data.ShopData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/**
 * 最低価格記録アプリ
 * 店リスト画面クラス
 *
 * @version 1.0 新規作成
 */
public class ShopListActivity extends AbstractBaseActivity implements OnUpdateListener {

    /** ロガー */
    private Logger mLogger = new Logger(ShopListActivity.class);

    /** 店名 */
    private EditText mShopNameValue;

    /** 店データリスト */
    private List<ShopData> mShopDataList;

    /** 店リストアダプタ */
    private ArrayAdapter<String> mShopListAdapter;

    /**
     * 生成された時に呼び出される。
     *
     * @param savedInstanceState 保存した時のインスタンスの状態
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mLogger.d("IN");

        // スーパークラスのメソッドを呼び出す。
        super.onCreate(savedInstanceState);

        // レイアウトを設定する。
        setContentView(R.layout.activity_shop_list);

        // 店データリストを取得する。
        getShopDataList();

        // 表示する店名のリストを生成する。
        List<String> shopNameList = new ArrayList<String>();
        for (ShopData shopData : mShopDataList) {
            shopNameList.add(shopData.getName());
        }

        // 店リストビューを設定する。
        mShopListAdapter =
                new ArrayAdapter<String>(
                        ShopListActivity.this, android.R.layout.simple_list_item_1, shopNameList);
        ListView shopListView = (ListView)findViewById(R.id.shopListView);
        shopListView.setAdapter(mShopListAdapter);
        shopListView.setOnItemClickListener(new ShopListOnItemClickListener());
        shopListView.setOnItemLongClickListener(new ShopListOnItemLongClickListener());

        mShopNameValue = (EditText)findViewById(R.id.shopNameValue);

        // リスナーを設定する。
        ((Button)findViewById(R.id.addShopButton)).setOnClickListener(new ButtonOnClickListener());

        mLogger.d("OUT(OK)");
    }

    /**
     * リスタートしたときに呼び出される。
     */
    @Override
    protected void onRestart() {
        mLogger.d("IN");

        // スーパークラスのメソッドを呼び出す。
        super.onRestart();

        // 店リストを更新する。
        updateShopList();

        mLogger.d("OUT(OK)");
    }

    /**
     * 店リストを更新する。
     */
    private void updateShopList() {
        mLogger.d("IN");

        // 店リストアダプタをクリアする。
        mShopListAdapter.clear();

        // 店データリストを取得する。
        getShopDataList();

        // 店リストアダプタに店名を再設定する。
        for (ShopData shopData : mShopDataList) {
            mShopListAdapter.add(shopData.getName());
        }

        // 店リストアダプタを更新する。
        mShopListAdapter.notifyDataSetChanged();

        mLogger.d("OUT(OK)");
    }

    /**
     * 店データリストを取得する。
     *
     */
    private void getShopDataList() {
        mLogger.d("IN");

        // 店データリストを生成する。
        mShopDataList = new ArrayList<ShopData>();

        // 店データのカーソルを取得する。
        Cursor c = mResolver.query(LowestProvider.SHOP_CONTENT_URI, null, null, null, null);
        try {
            // カーソルが取得できた場合
            if (null != c) {
                // データがある場合
                if (c.moveToFirst()) {
                    do {
                        // 店データを生成し、店データリストに追加する。
                        ShopData shopData = new ShopData();
                        shopData.setId(        c.getInt(   c.getColumnIndex(ShopTable.ID)));
                        shopData.setName(      c.getString(c.getColumnIndex(ShopTable.NAME)));
                        shopData.setUpdateTime(c.getLong(  c.getColumnIndex(ShopTable.UPDATE_TIME)));

                        // 店データリストに追加する。
                        mShopDataList.add(shopData);
                    } while (c.moveToNext());
                }
            }
        } finally {
            if (null != c) {
                c.close();
            }
        }

        mLogger.d("OUT(OK)");
    }

    /**
     * 更新された時に呼び出される。
     */
    @Override
    public void onUpdate() {
        mLogger.d("IN");

        // 店リストを更新する。
        updateShopList();

        mLogger.d("OUT(OK)");
    }

    /**************************************************************************/
    /**
     * ボタンクリックリスナークラス
     *
     */
    private class ButtonOnClickListener implements OnClickListener {

        /** ロガー */
        private Logger mLogger = new Logger(ButtonOnClickListener.class);

        /**
         * ボタンがクリックされた時に呼び出される。
         *
         * @param v クリックされたボタン
         */
        @Override
        public void onClick(View v) {
            mLogger.d("IN");

            // クリックされたボタンにより処理を判別する。
            switch (v.getId()) {
            // 追加ボタンの場合
            case R.id.addShopButton:
                // 店名を取得する。
                String shopName = mShopNameValue.getText().toString();

                // 店名が未入力の場合
                if (StringUtil.isNullOrEmpty(shopName)) {
                    // 終了する。
                    toast("店名が未入力です");
                    return;
                }

                // 登録済みか確認する。
                String selection = ShopTable.NAME + " = ?";
                String[] selectionArgs = {shopName};
                Cursor c = mResolver.query(LowestProvider.SHOP_CONTENT_URI, null, selection, selectionArgs, null);
                boolean existFlg = false;
                try {
                    // カーソルがある場合
                    if (null != c) {
                        // データがある場合
                        if (c.moveToFirst()) {
                            // 登録済みとする。
                            existFlg = true;
                        }
                    }
                } finally {
                    if (null != c) {
                        c.close();
                    }
                }

                // 登録済みの場合
                if (existFlg) {
                    // 終了する。
                    toast("登録済みです");
                    return;
                }

                long updateTime = new Date().getTime();
                ContentValues values = new ContentValues();
                values.put(ShopTable.NAME, shopName);
                values.put(ShopTable.UPDATE_TIME, updateTime);
                Uri result = mResolver.insert(LowestProvider.SHOP_CONTENT_URI, values);
                if (null == result) {
                    toast("登録に失敗しました");
                    return;
                }

                // 店リストを更新する。
                updateShopList();
                break;
            }

            mLogger.d("OUT(OK)");
        }
    }

    /**************************************************************************/
    /**
     * 店リストアイテムクリックリスナークラス
     *
     */
    private class ShopListOnItemClickListener implements OnItemClickListener {

        /** ロガー */
        private Logger mLogger = new Logger(ShopListOnItemClickListener.class);

        /**
         * リストアイテムがクリックされた時に呼び出される。
         *
         * @param parent 親のビュー
         * @param view 対象のビュー
         * @param position リストの位置
         * @param id 対象のビューのID
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mLogger.d("IN");

            // 呼び出し元画面に戻る。
            Intent intent = new Intent();
            intent.putExtra(ExtraKey.SHOP_DATA, mShopDataList.get(position));
            setResult(RESULT_OK, intent);
            finish();

            mLogger.d("OUT(OK)");
        }
    }

    /**************************************************************************/
    /**
     * 店リストアイテムロングクリックリスナークラス
     *
     */
    private class ShopListOnItemLongClickListener implements OnItemLongClickListener {

        /** ロガー */
        private Logger mLogger = new Logger(ShopListOnItemLongClickListener.class);

        /**
         * リストアイテムがロングクリックされた時に呼び出される。
         *
         * @param parent 親のビュー
         * @param view 対象のビュー
         * @param position リストの位置
         * @param id 対象のビューのID
         * @return 処理結果
         */
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            mLogger.d("IN");

            FragmentManager manager = getSupportFragmentManager();
            ShopDetailDialog shopDetailDialog = ShopDetailDialog.newInstance(mShopDataList.get(position));
            shopDetailDialog.show(manager, ShopDetailDialog.class.getSimpleName());

            mLogger.d("OUT(OK)");
            return true;
        }
    }
}
