package info.paveway.lowest.dialog;

import info.paveway.log.Logger;
import info.paveway.lowest.CommonConstants.ExtraKey;
import info.paveway.lowest.R;
import info.paveway.lowest.data.GoodsData;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * 最低価格記録アプリ
 * 商品詳細ダイアログクラス
 *
 * @version 1.0 新規作成
 */
public class GoodsDetailDialog extends AbstractBaseDialogFragment {

    /** ロガー */
    private Logger mLogger = new Logger(GoodsDetailDialog.class);

    /** 商品データ */
    private GoodsData mGoodsData;

    /** カテゴリ表示 */
    private TextView mCategoryNameValue;

    /** 商品名表示 */
    private TextView mGoodsNameValue;

    /**
     * インスタンスを生成する。
     *
     * @return インスタンス
     */
    public static GoodsDetailDialog newInstance(GoodsData goodsData) {
        GoodsDetailDialog instance = new GoodsDetailDialog();
        Bundle args = new Bundle();
        args.putSerializable(ExtraKey.GOODS_DATA, goodsData);
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
        mGoodsData = (GoodsData)getArguments().getSerializable(ExtraKey.GOODS_DATA);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_goods_detail, null);

        mCategoryNameValue = (TextView)rootView.findViewById(R.id.categoryNameValue);
        mGoodsNameValue    = (TextView)rootView.findViewById(R.id.goodsNameValue);

        mCategoryNameValue.setText(mGoodsData.getCategoryName());
        mGoodsNameValue.setText(mGoodsData.getName());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.goods_detail_dialog_title);
        builder.setPositiveButton(R.string.dialog_update_button, null);
        builder.setNeutralButton( R.string.dialog_delete_button, null);
        builder.setNegativeButton(R.string.dialog_close_button,  null);
        builder.setView(rootView);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // ボタン押下でダイアログが閉じないようにリスナーを設定する。
        dialog.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ((AlertDialog)dialog).getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doUpdate();
                    }
                });

                ((AlertDialog)dialog).getButton(Dialog.BUTTON_NEUTRAL).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doDelete();
                    }
                });

                ((AlertDialog)dialog).getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doCancel();
                    }
                });
            }
        });

        mLogger.d("OUT(OUT)");
        return dialog;
    }

    /**
     * 変更する。
     */
    private void doUpdate() {
        mLogger.d("IN");

        // 商品編集ダイアログを表示する。
        FragmentManager manager = getActivity().getSupportFragmentManager();
        GoodsEditDialog goodsEditDialog = GoodsEditDialog.newInstance(mGoodsData);
        goodsEditDialog.show(manager, GoodsEditDialog.class.getSimpleName());

        // 終了する。
        dismiss();
        mLogger.d("OUT(OK)");
    }

    /**
     * 削除する。
     */
    private void doDelete() {
        mLogger.d("IN");

        // 商品データ削除確認ダイアログを表示する。
        FragmentManager manager = getActivity().getSupportFragmentManager();
        GoodsDeleteDialog goodsDeleteDialog = GoodsDeleteDialog.newInstance(mGoodsData);
        goodsDeleteDialog.show(manager, GoodsDeleteDialog.class.getSimpleName());

        // 終了する。
        dismiss();
        mLogger.d("OUT(OK)");
    }

    /**
     * キャンセルする。
     */
    private void doCancel() {
        mLogger.d("IN");

        toast(R.string.error_cancel);

        // 終了する。
        dismiss();
        mLogger.d("OUT(OK)");
    }
}
