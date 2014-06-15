package info.paveway.lowest;

import info.paveway.log.Logger;
import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.CommonConstants.RequestCode;
import info.paveway.lowest.data.LowestProvider;
import info.paveway.lowest.data.LowestProvider.PriceTable;
import info.paveway.lowest.data.PriceData;
import info.paveway.lowest.data.ShopData;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 最低価格
 * 価格編集画面
 *
 * @version 1.0 新規作成
 *
 */
public class PriceEditActivity extends AbstractBaseActivity {

    /** ロガー */
    private Logger mLogger = new Logger(PriceEditActivity.class);

    /** 価格データ */
    private PriceData mPriceData;

    /** 店名 */
    private TextView mShopNameValue;

    /** 数量 */
    private EditText mQuantityValue;

    /** 価格 */
    private EditText mPriceValue;

    /** メモ */
    private EditText mMemoValue;

    /**
     * 生成された時に呼び出される。
     *
     * @param savedInstanceState 保存された時のインスタンスの状態
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // スーパークラスのメソッドを呼び出す。
        super.onCreate(savedInstanceState);

        // レイアウトを設定する。
        setContentView(R.layout.activity_price_edit);

        // インテントを取得する。
        Intent intent = getIntent();
        // 引継ぎデータがある場合
        if (null == intent) {
            toast(R.string.illeagal_transition);
            // 終了する。
            finish();
            return;
        }

        // 価格データを取得する。
        mPriceData = (PriceData)intent.getSerializableExtra(ExtraKey.PRICE_DATA);
        // 引継ぎデータが取得できない場合
        if (null == mPriceData) {
            // 終了する。
            toast(R.string.illeagal_transition);
            finish();
            return;
        }

        // 各ウィジットを設定する。
        mShopNameValue = (TextView)findViewById(R.id.shopNameValue);
        mQuantityValue = (EditText)findViewById(R.id.quantityValue);
        mPriceValue    = (EditText)findViewById(R.id.priceValue);
        mMemoValue     = (EditText)findViewById(R.id.memoValue);

        ((TextView)findViewById(R.id.categoryNameValue)).setText(mPriceData.getCategoryName());
        ((TextView)findViewById(R.id.goodsNameValue)).setText(mPriceData.getGoodsName());
        String shopName = mPriceData.getShopName();
        if (StringUtil.isNotNullOrEmpty(shopName)) {
            mShopNameValue.setText(shopName);
            mQuantityValue.setText(String.valueOf(mPriceData.getQuantity()));
            mPriceValue.setText(String.valueOf(mPriceData.getPrice()));
            mMemoValue.setText(mPriceData.getMemo());
        }

        ((Button)findViewById(R.id.registButton)).setOnClickListener(new ButtonOnClickListener());
        ((Button)findViewById(R.id.clearButton)).setOnClickListener(new ButtonOnClickListener());
        ((Button)findViewById(R.id.shopNameButton)).setOnClickListener(new ButtonOnClickListener());
    }

    /**
     * 他の画面の呼び出しから戻った時に呼び出される。
     *
     * @param requestCode 要求コード
     * @param resultCode 結果コード
     * @param data データ
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 店リスト画面の戻りかつ正常終了の場合
        if ((RequestCode.SHOP_LIST == requestCode) && (RESULT_OK == resultCode)) {
            // 引継ぎデータから店データを取得する。
            ShopData shopData = (ShopData)data.getSerializableExtra(ExtraKey.SHOP_DATA);

            // 店データが取得できた場合
            if (null != shopData) {
                // 店名を設定する。
                mShopNameValue.setText(shopData.getName());

                // 価格データに店データのデータを設定する。
                mPriceData.setShopId(shopData.getId());
                mPriceData.setShopName(shopData.getName());
            }
        }
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
            // 登録ボタンの場合
            case R.id.registButton:
                // 入力値を取得する。
                String shopName = mShopNameValue.getText().toString();
                String quantityString = mQuantityValue.getText().toString();
                String priceString = mPriceValue.getText().toString();
                String memo = mMemoValue.getText().toString();
                // 未入力の項目がある場合
                if ((null == shopName      ) || "".equals(shopName      ) &&
                    (null == quantityString) || "".equals(quantityString) &&
                    (null == priceString   ) || "".equals(priceString   )) {
                    toast("全て入力して下さい");

                // 全て入力された場合
                } else {
                    double quantity = 0.0D;
                    try {
                        quantity = Double.parseDouble(quantityString);
                    } catch (Exception e) {
                        toast("不正な値です");
                        return;
                    }
                    long price = 0L;
                    try {
                        price = Long.parseLong(priceString);
                    } catch (Exception e) {
                        toast("不正な値です");
                        return;
                    }

                    ContentValues values = new ContentValues();
                    values.put(PriceTable.CATEGORY_ID,   mPriceData.getCagetoryId());
                    values.put(PriceTable.CATEGORY_NAME, mPriceData.getCategoryName());
                    values.put(PriceTable.GOODS_ID,      mPriceData.getGoodsId());
                    values.put(PriceTable.GOODS_NAME,    mPriceData.getGoodsName());
                    values.put(PriceTable.SHOP_ID,       mPriceData.getShopId());
                    values.put(PriceTable.SHOP_NAME,     mPriceData.getShopName());
                    values.put(PriceTable.QUANTITY,      quantity);
                    values.put(PriceTable.PRICE,         price);
                    values.put(PriceTable.MEMO,          memo);

                    // 価格データIDを取得する。
                    long priceId = mPriceData.getId();

                    // 未登録の場合
                    if (0 == priceId) {
                        Uri result = mResolver.insert(LowestProvider.PRICE_CONTENT_URI, values);

                        // 登録に失敗した場合
                        if (null == result) {
                            toast("登録に失敗しました");

                        // 登録に成功した場合
                        } else {
                            mPriceData.setId(ContentUris.parseId(result));
                            mPriceData.setQuantity(quantity);
                            mPriceData.setPrice(price);
                            mPriceData.setMemo(memo);
                        }

                    // 登録済みの場合
                    } else {
                        String selection = PriceTable.ID + " = ?";
                        String[] selectionArgs = {String.valueOf(priceId)};
                        int result =
                                mResolver.update(LowestProvider.PRICE_CONTENT_URI, values, selection, selectionArgs);

                        // 更新に失敗した場合
                        if (1 != result) {
                            toast("更新に失敗しました");

                        // 更新に成功した場合
                        } else {
                            mPriceData.setQuantity(quantity);
                            mPriceData.setPrice(price);
                            mPriceData.setMemo(memo);
                        }
                    }

                    // アクティビティを終了する。
                    finish();
                }
                break;

            // クリアボタンの場合
            case R.id.clearButton:
                // 入力項目をクリアする。
                mShopNameValue.setText("");
                mQuantityValue.setText("");
                mPriceValue.setText("");
                mMemoValue.setText("");
                break;

            // 店名ボタンの場合
            case R.id.shopNameButton:
                // 価格編集画面に遷移する。
                Intent intent = new Intent(PriceEditActivity.this, ShopListActivity.class);
                startActivityForResult(intent, RequestCode.SHOP_LIST);
                break;

            // 上記以外
            default:
                // 何もしない。
                break;
            }
        }
    }
}
