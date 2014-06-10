package info.paveway.lowest;

import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.CommonConstants.RequestCode;
import info.paveway.lowest.data.PriceData;
import android.content.Intent;
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

    /** 店名 */
    private TextView mShopNameValue;

    /** 数量 */
    private EditText mQuantityValue;

    /** 価格 */
    private EditText mPriceValue;

    /** 価格データ */
    private PriceData mPriceData;

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

        // コンテントリゾルバーを取得する。
        mResolver = getContentResolver();

        // 各ウィジットを設定する。
        mShopNameValue = (TextView)findViewById(R.id.shopNameValue);
        mQuantityValue = (EditText)findViewById(R.id.quantityValue);
        mPriceValue    = (EditText)findViewById(R.id.priceValue);

        // 価格データがある場合
        if (null != mPriceData) {
            mShopNameValue.setText(mPriceData.getName());
            mQuantityValue.setText(String.valueOf(mPriceData.getQuantity()));
            mPriceValue.setText(String.valueOf(mPriceData.getPrice()));
        }

        ((Button)findViewById(R.id.registButton)).setOnClickListener(new ButtonOnClickListener());
        ((Button)findViewById(R.id.clearButton)).setOnClickListener(new ButtonOnClickListener());
        ((Button)findViewById(R.id.shopNameButton)).setOnClickListener(new ButtonOnClickListener());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((RequestCode.SHOP_LIST == requestCode) && (RESULT_OK == resultCode)) {
            mShopNameValue.setText(data.getStringExtra("shopName"));
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
                // 未入力の項目がある場合
                if ((null == shopName      ) || "".equals(shopName      ) &&
                    (null == quantityString) || "".equals(quantityString) &&
                    (null == priceString   ) || "".equals(priceString   )) {
                    toast("全て入力して下さい");

                // 全て入力された場合
                } else {
                    double quantity = Double.parseDouble(quantityString);
                    long price = Long.parseLong(priceString);
                    mPriceData.setName(shopName);
                    mPriceData.setQuantity(quantity);
                    mPriceData.setPrice(price);

                    Intent intent = new Intent();
                    intent.putExtra(ExtraKey.PRICE_DATA, mPriceData);
                    setResult(RESULT_OK, intent);

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
