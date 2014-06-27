package info.paveway.lowest;

import info.paveway.log.Logger;
import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.data.ShopData;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * 最低価格記録アプリ
 * 店詳細ダイアログクラス
 *
 * @version 1.0 新規作成
 */
public class ShopDetailDialog extends AbstractBaseDialogFragment {

    /** ロガー */
    private Logger mLogger = new Logger(ShopDetailDialog.class);

    /** 店データ */
    private ShopData mShopData;

    /** 店名表示 */
    private TextView mShopNameValue;

    /**
     * インスタンスを生成する。
     *
     * @return インスタンス
     */
    public static ShopDetailDialog newInstance(ShopData shopData) {
        ShopDetailDialog instance = new ShopDetailDialog();
        Bundle args = new Bundle();
        args.putSerializable(ExtraKey.SHOP_DATA, shopData);
        instance.setArguments(args);
        return instance;
    }

    /**
     * 生成した時に呼び出される。
     *
     * @param savedInstanceState 保存した時のインスタンスの状態
     * @return ダイアログ
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mLogger.d("IN");

        // 引数を取得する。
        mShopData = (ShopData)getArguments().getSerializable(ExtraKey.SHOP_DATA);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_shop_detail, null);

        mShopNameValue = (TextView)rootView.findViewById(R.id.shopNameValue);
        mShopNameValue.setText(mShopData.getName());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("店詳細");
        builder.setPositiveButton("変更", new DialogButtonOnClickListener());
        builder.setNeutralButton("削除", new DialogButtonOnClickListener());
        builder.setNegativeButton("キャンセル", new DialogButtonOnClickListener());
        builder.setView(rootView);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        mLogger.d("OUT(OUT)");
        return dialog;
    }

    /**************************************************************************/
    /**
     * ダイアログボタンクリックリスナークラス
     *
     */
    private class DialogButtonOnClickListener implements DialogInterface.OnClickListener {

        /** ロガー */
        private Logger mLogger = new Logger(DialogButtonOnClickListener.class);

        /**
         * ボタンがクリックされた時に呼び出される。
         *
         * @param dialog ダイアログ
         * @param which クリックされたボタン
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mLogger.d("IN");

            // ボタンにより処理を判別する。
            switch (which) {
            // 変更ボタンの場合
            case Dialog.BUTTON_POSITIVE: {
                mLogger.d("BUTTON_POSITIVE");

                // 店データ編集ダイアログを表示する。
                FragmentManager manager = getActivity().getSupportFragmentManager();
                ShopEditDialog shopEditDialog = ShopEditDialog.newInstance(mShopData);
                shopEditDialog.show(manager, ShopEditDialog.class.getSimpleName());

                // 終了する。
                dismiss();
                break;
            }

            // 削除ボタンの場合
            case Dialog.BUTTON_NEUTRAL: {
                // 店データ削除確認ダイアログを表示する。
                FragmentManager manager = getActivity().getSupportFragmentManager();
                ShopDeleteDialog shopDeleteDialog = ShopDeleteDialog.newInstance(mShopData);
                shopDeleteDialog.show(manager, ShopDeleteDialog.class.getSimpleName());

                // 終了する。
                dismiss();
                break;
            }

            // キャンセルボタンの場合
            case Dialog.BUTTON_NEGATIVE:
                toast("キャンセルします");

                // 終了する。
                dismiss();
                break;
            }

            mLogger.d("OUT(OUT)");
        }
    }
}
