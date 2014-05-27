package info.paveway.lowest;

import info.paveway.lowest.data.ShopProvider;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * 最低価格
 * 店リスト画面
 *
 * @version 1.0 新規作成
 *
 */
public class ShopListActivity extends Activity {

	/** コンテントリゾルバ */
	private ContentResolver mResolver;

	/**
	 * 生成された時に呼び出される。
	 *
	 * @param savedInstanceState 保存された時のインスタンスの状態
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// スーパークラスのメソッド
        super.onCreate(savedInstanceState);

        // レイアウトを設定する。
        setContentView(R.layout.activity_shop_list);

        // コンテントリゾルバを取得する。
        mResolver = getContentResolver();

        // 各ウィジットを設定する。
        ((Button)findViewById(R.id.addButton)).setOnClickListener(new ButtonOnClickListener());

        List<String> shopNameList = getShopNameList();

        ArrayAdapter<String> adapter =
        		new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        for (String shopName : shopNameList) {
        	adapter.add(shopName);
        }
        ListView shopListView = (ListView)findViewById(R.id.shopListView);
        shopListView.setAdapter(adapter);
        shopListView.setOnItemClickListener(new ShopListOnItemClickListener());
        shopListView.setOnItemLongClickListener(new ShopListOnItemLongClickListener());
    }

    /**
     * 店名リストを取得する。
     *
     * @return 店名リスト
     */
    private List<String> getShopNameList() {
    	List<String> shopNameList = new ArrayList<String>();
    	// 店データのカーソルを取得する。
        Cursor c = mResolver.query(ShopProvider.CONTENT_URI, null, null, null, null);
        try {
        	// カーソルが取得できた場合
            if (null != c) {
            	// データがある場合
                if (c.moveToFirst()) {
                    do {
                        // 店名データリストに設定する。
                        shopNameList.add(c.getString(c.getColumnIndex(ShopProvider.SHOP_NAME)));
                    } while (c.moveToNext());
                }
            }
        } finally {
            if (null != c) {
                c.close();
            }
        }
        return shopNameList;
    }

    /**************************************************************************/
    /**
     * ボタンクリックリスナークラス
     *
     */
    private class ButtonOnClickListener implements OnClickListener {

    	/**
    	 * ボタンがクリックされた時に呼び出される。
    	 *
    	 * @param v クリックされたボタン
    	 */
        @Override
        public void onClick(View v) {
        	// ボタンにより処理を判別する。
            switch (v.getId()) {
            // 追加ボタンの場合
            case R.id.addButton:
                final EditText shopNameValue = new EditText(ShopListActivity.this);
                new AlertDialog.Builder(ShopListActivity.this)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle("店名入力")
                    .setView(shopNameValue)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    	@Override
                        public void onClick(DialogInterface dialog, int which) {
                    		// OKボタンがクリックされた時の処理を行う。
                    		onOKButtonClick(shopNameValue);
                    	}
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    	@Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	// 何もしない。
                        }
                    })
                    .show();
                break;

            default:
                break;
            }
        }

        /**
         * OKボタンがクリックされた時の処理を行う。
         *
         * @param shopNameValue 店名入力値
         */
        private void onOKButtonClick(EditText shopNameValue) {
        	// 入力された店名を取得する。
    		String shopName = shopNameValue.getText().toString();

    		// 店名が入力された場合
    		if ((null != shopName) && !"".equals(shopName)) {
    			// 登録済みかチェックする。
    			boolean exist = false;
    	    	String selection = ShopProvider.SHOP_NAME + " = ?";
    	    	String[] selectionArgs = new String[]{shopName};
    	    	Cursor c = mResolver.query(ShopProvider.CONTENT_URI, null, selection, selectionArgs, null);
    	    	try {
    	    		// カーソルが取得できた場合
    	    		if (null != c) {
    	    			// データがある場合
    	    			if (c.moveToFirst()) {
    	    				// 登録済みとする。
    	    				exist = true;
    	    			}
    	    		}
    	    	} finally {
    	    		if (null != c) {
    	    			c.close();
    	    		}
    	    	}

    	    	// 登録済みの場合
    	    	if (exist) {
    	    		Toast.makeText(ShopListActivity.this, "登録済みです。", Toast.LENGTH_SHORT).show();

    	    	// 未登録の場合
    	    	} else {
    	    		// 店名を登録する。
    	    		ContentValues values = new ContentValues();
    	    		values.put(ShopProvider.SHOP_NAME, shopName);
    	    		Uri result = mResolver.insert(ShopProvider.CONTENT_URI, values);
    	    		// 登録できた場合
    	    		if (null != result) {
    	    			Toast.makeText(ShopListActivity.this, "店名を登録しました。", Toast.LENGTH_SHORT).show();

    	    		// 登録できなかった場合
    	    		} else {
    	    			Toast.makeText(ShopListActivity.this, "店名を登録できませんでした。", Toast.LENGTH_SHORT).show();
    	    		}
    	    	}

    	    // 未入力の場合
    	    } else {
    	    	Toast.makeText(ShopListActivity.this, "店名を入力して下さい。", Toast.LENGTH_SHORT).show();
    	    }
        }
    }

    /**************************************************************************/
    /**
     * 店リストアイテムクリックリスナークラス
     *
     */
    private class ShopListOnItemClickListener implements OnItemClickListener {

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
		}
    }

    /**************************************************************************/
    /**
     * 店リストアイテムロングクリックリスナークラス
     *
     */
    private class ShopListOnItemLongClickListener implements OnItemLongClickListener {

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
			return false;
		}

    }
}
