package info.paveway.lowest.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 最低価格
 * 品物データ保持クラス
 *
 * @version 1.0 新規作成
 *
 */
@SuppressWarnings("serial")
public class GoodsData implements Serializable {

    /** 品物ID */
    private int mGoodsId;

    /** 品物名 */
    private String mGoodsName;

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
     * 品物IDを設定する。
     *
     * @param goodsId 品物ID
     */
    public void setGoodsId(int goodsId) {
        mGoodsId = goodsId;
    }

    /**
     * 品物IDを返却する。
     *
     * @return 品物ID
     */
    public int getGoodsId() {
        return mGoodsId;
    }

    /**
     * 品物名を設定する。
     *
     * @param goodsName 品物名
     */
    public void setGoodsName(String goodsName) {
        mGoodsName = goodsName;
    }

    /**
     * 品物名を返却する。
     *
     * @return 品物名
     */
    public String getGoodsName() {
        return mGoodsName;
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
