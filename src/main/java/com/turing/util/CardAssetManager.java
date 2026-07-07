package com.turing.util;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * 圖靈密碼驗證卡片資產與佈局管理器
 * 專門負責管理卡片打叉點擊區域的座標配置，將靜態資料從 UI 介面抽離。
 */
public class CardAssetManager {

    /**
     * 根據卡片 ID，動態計算並取得該卡片在 UI LayeredPane 上的所有按鈕點擊邊界 (Rectangle)
     * 
     * @param cardId 卡片編號 (1 ~ 48)
     * @return 包含所有可點擊區域 Rectangle 的 List 清單
     */
    public static List<Rectangle> getCardOptionBounds(int cardId) {
        List<Rectangle> list = new ArrayList<>();
        int btnY = 120;     
        int btnHeight = 65; 

        if (cardId == 40 || cardId == 41 || cardId == 48) {
            int w3 = 94;          
            int h3 = 21;          
            int spacingY = 2;     
            for (int row = 0; row < 3; row++) {      
                for (int col = 0; col < 3; col++) {  
                    int curX = 6 + (col * 96);
                    int curY = btnY + (row * (h3 + spacingY));
                    list.add(new Rectangle(curX, curY, w3, h3));
                }
            }
        }
        else if (cardId == 33 || cardId == 39 || cardId == 42 || cardId == 43 
                || cardId == 44 || cardId == 45 || cardId == 46 || cardId == 47) {
            int w3 = 94;          
            int h2 = 31;          
            int spacingY = 3;
            for (int row = 0; row < 2; row++) {      
                for (int col = 0; col < 3; col++) {  
                    int curX = 6 + (col * 96);
                    int curY = btnY + (row * (h2 + spacingY));
                    list.add(new Rectangle(curX, curY, w3, h2));
                }
            }
        }
        else if (cardId == 1 || cardId == 5 || cardId == 6 || cardId == 7 || cardId == 16 || cardId == 21) {
            int w2 = 142;
            list.add(new Rectangle(6, btnY, w2, btnHeight));        
            list.add(new Rectangle(6 + 146, btnY, w2, btnHeight));  
        }
        else if (cardId == 8 || cardId == 9 || cardId == 10 || cardId == 17) {
            int w4 = 68;
            for (int i = 0; i < 4; i++) {
                list.add(new Rectangle(6 + (i * 73), btnY, w4, btnHeight));
            }
        }
        else {
            int w3 = 94;
            for (int i = 0; i < 3; i++) {
                list.add(new Rectangle(6 + (i * 96), btnY, w3, btnHeight));
            }
        }
        return list;
    }
}