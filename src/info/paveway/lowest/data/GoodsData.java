package info.paveway.lowest.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * 最低価格記録アプリ
 * 商品データクラス
 *
 * @version 1.0 新規作成
 *
 */
public class GoodsData extends AbstractBaseData {

    /** カテゴリID */
    private long mCategoryId;

    /** カテゴリ名 */
    private String mCategoryName;

    /** メモ */
    private String mMemo;

    /** お気に入り */
    private int mFavorite;

    /**
     * 価格データマップ
     * 最低価格順にソートするため内部で使用する。
     */
    private Map<Double, PriceData> mPriceDataMap;

    /** 価格データリスト */
    private List<PriceData> mPriceDataList;

    /**
     * コンストラクタ
     */
    public GoodsData() {
        super();
        mPriceDataMap = new TreeMap<Double, PriceData>();
        mPriceDataList = new ArrayList<PriceData>();
    }

    /**
     * カテゴリIDを設定する。
     *
     * @param categoryId カテゴリID
     */
    public void setCategoryId(long categoryId) {
        mCategoryId = categoryId;
    }

    /**
     * カテゴリIDを返却する。
     *
     * @return カテゴリID
     */
    public long getCategoryId() {
        return mCategoryId;
    }

    /**
     * カテゴリ名を設定する。
     *
     * @param categoryName カテゴリ名
     */
    public void setCategoryName(String categoryName) {
        mCategoryName = categoryName;
    }

    /**
     * カテゴリ名を返却する。
     *
     * @return カテゴリ名
     */
    public String getCategoryName() {
        return mCategoryName;
    }

    /**
     * メモを設定する。
     *
     * @param memo メモ
     */
    public void setMemo(String memo) {
        mMemo = memo;
    }

    /**
     * メモを返却する。
     *
     * @return メモ
     */
    public String getMemo() {
        return mMemo;
    }

    /**
     * お気に入りを設定する。
     *
     * @param favorite お気に入り
     */
    public void setFavorite(int favorite) {
        mFavorite = favorite;
    }

    /**
     * お気に入りを返却する。
     *
     * @return お気に入り
     */
    public int getFavorite() {
        return mFavorite;
    }

    /**
     * 価格データを追加する。
     *
     * @param priceData 価格データ
     */
    public void addPriceData(PriceData priceData) {
        mPriceDataMap.put(priceData.getUnitPrice(), priceData);
    }

    /**
     * 最低価格の価格データを返却する。
     *
     * @return 最低価格の価格データ
     */
    public PriceData getLowestPriceData() {
        Iterator<PriceData> itr = mPriceDataMap.values().iterator();
        // データがある場合
        if (itr.hasNext()) {
            return itr.next();

        // データが無い場合
        } else {
            return null;
        }
    }

    /**
     * 価格データリストを返却する。
     *
     * @return 価格データリスト
     */
    public List<PriceData> getPriceDataList() {
        mPriceDataList.clear();
        Iterator<PriceData> itr = mPriceDataMap.values().iterator();
        while (itr.hasNext()) {
            mPriceDataList.add(itr.next());
        }
        return mPriceDataList;
    }
}
