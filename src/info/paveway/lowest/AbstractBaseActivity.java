package info.paveway.lowest;

import info.paveway.log.Logger;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

/**
 * 最低価格記録アプリ
 * 抽象ベース画面クラス
 *
 * @version 1.0 新規作成
 */
public abstract class AbstractBaseActivity extends FragmentActivity {

    /** ロガー */
    private Logger mLogger = new Logger(AbstractBaseActivity.class);

    /** コンテントリゾルバ */
    protected ContentResolver mResolver;

    /** リソース */
    protected Resources mResources;

    /**
     * 生成された時に呼び出される。
     *
     * @param savedInstanceState 保存した時のインスタンスの状態
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLogger.d("IN");

        // スーパークラスのメソッドを呼び出す。
        super.onCreate(savedInstanceState);

        // コンテントリゾルバを取得する。
        mResolver = getContentResolver();

        // リソースを取得する。
        mResources = getResources();

        mLogger.d("OUT(OK)");
    }

    /**
     * トースト表示する。
     *
     * @param id 文字列リソースID
     */
    protected void toast(int id) {
        toast(getResources().getString(id));
    }

    /**
     * トースト表示する。
     *
     * @param message メッセージ
     */
    protected void toast(String message) {
        Toast.makeText(AbstractBaseActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
