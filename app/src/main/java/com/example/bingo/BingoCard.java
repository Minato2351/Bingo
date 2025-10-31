package com.example.bingo;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class BingoCard extends View {
    public int[][] cuadros = new int[5][5];
    private Bitmap bingoCard;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public BingoCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    //Al invocar BingoCard para un jugador este automaticamente le asigna los valores a su carta
    private void init(){
        for(int i=0; i<5; i++){
            for(int j=0; j<5; j++){
                cuadros[i][j] = (int)(Math.random() * (15) + 1 + (i * 15));
            }
        }
        cuadros[2][2] = 0;
        bingoCard = BitmapFactory.decodeResource(getResources(), R.drawable.bingo_card);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the bingo card image
        if (bingoCard != null) {
            canvas.drawBitmap(bingoCard, 0, 0, null);
        }

        // Set up paint for numbers
        paint.setColor(Color.BLACK);
        paint.setTextSize(48);
        paint.setTextAlign(Paint.Align.CENTER);

        float cellWidth = getWidth() / 5f;
        float cellHeight = getHeight() / 5f;

        // Draw numbers in each cell
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (cuadros[i][j] != 0) {
                    float x = i * cellWidth + cellWidth / 2;
                    float y = j * cellHeight + cellHeight / 1.7f;
                    canvas.drawText(String.valueOf(cuadros[i][j]), x, y, paint);
                } else if (i == 2 && j == 2) {
                    paint.setColor(Color.RED);
                    canvas.drawText("FREE", i * cellWidth + cellWidth / 2, j * cellHeight + cellHeight / 1.7f, paint);
                    paint.setColor(Color.BLACK);
                }
            }
        }
    }
}
