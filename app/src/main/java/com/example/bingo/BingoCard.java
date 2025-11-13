package com.example.bingo;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class BingoCard extends View {
    Cuadro[][] cuadros = new Cuadro[5][5];
    int[] Numeros = new int[5]; //Evita repeticion
    boolean repetido = false;
    private Bitmap bingoCard;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    int numeroRand = 0;
    private Bitmap estampa;

    public BingoCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    //Al invocar BingoCard para un jugador este automaticamente le asigna los valores a su carta
    private void init(){
        for(int i=0; i<5; i++){
            for(int j=0; j<5; j++){
                numeroRand = (int)(Math.random() * (15) + 1 + (i * 15));
                for(int z=0; z<5; z++){
                    if(numeroRand == Numeros[z]){
                        repetido = true;
                    }
                }
                if(repetido == false){
                    Numeros[j] = numeroRand;
                    cuadros[i][j] = new Cuadro(numeroRand, false);
                }else if(repetido == true){
                    j--;
                    repetido = false;
                }
            }
        }
        cuadros[2][2].numero = 0;
        cuadros[2][2].estampa = true;
        bingoCard = BitmapFactory.decodeResource(getResources(), R.drawable.bingo_card);
        estampa = BitmapFactory.decodeResource(getResources(), R.drawable.estampa);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the bingo card image
        if (bingoCard != null) {
            Rect src = new Rect(0, 0, bingoCard.getWidth(), bingoCard.getHeight());
            Rect dst = new Rect(0, 0, getWidth(), getHeight());
            canvas.drawBitmap(bingoCard, src, dst, null);
        }

        // Set up paint for numbers
        paint.setColor(Color.parseColor("#800080"));
        paint.setTextSize(90);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.CENTER);

        float cellWidth = getWidth() / 6f;
        float cellHeight = getHeight() / 6.9f;
        float topMargin = getHeight() * 0.20f;
        float leftMargin = getHeight() * 0.033f;

        // Draw numbers in each cell
        for (int i = 0; i < 5; i++) {
            switch (i){
                case 1:
                    paint.setColor(Color.parseColor("#2196F3"));
                    break;
                case 2:
                    paint.setColor(Color.parseColor("#4CAF50"));
                    break;
                case 3:
                    paint.setColor(Color.parseColor("#FFEB3B"));
                    break;
                case 4:
                    paint.setColor(Color.parseColor("#FF9800"));
                    break;
            }
            for (int j = 0; j < 5; j++) {
                if (i == 2 && j == 2 ) {}
                else {
                    if (cuadros[i][j].numero != 0) {
                        float x = i * cellWidth + cellWidth / 2 + leftMargin;
                        float y = j * cellHeight + cellHeight / 1.7f + topMargin;
                        canvas.drawText(String.valueOf(cuadros[i][j].numero), x, y, paint);
                        if(cuadros[i][j].estampa == true) {
                            float imageSize = cellWidth * 1.2f; // 60% del cuadro
                            float imageX = x - imageSize / 2;   // centra
                            float imageY = y - imageSize / 2;   // centra

                            Rect dst = new Rect(
                                    (int) imageX,
                                    (int) imageY,
                                    (int) (imageX + imageSize),
                                    (int) (imageY + imageSize)
                            );
                            canvas.drawBitmap(estampa, null, dst, null);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float cellWidth = getWidth() / 6f;
            float cellHeight = getHeight() / 6.9f;
            float topMargin = getHeight() * 0.20f;
            float leftMargin = getHeight() * 0.033f;

            // Determine which cell was tapped
            float x = event.getX() - leftMargin;
            float y = event.getY() - topMargin;

            // Ignore taps outside the actual bingo grid
            if (x < 0 || y < 0) return false;
            if (x > cellWidth * 5 || y > cellHeight * 5) return false;

            // Determine which cell was tapped
            int col = (int) (x / cellWidth);
            int row = (int) (y / cellHeight);

            if (col >= 0 && col < 5 && row >= 0 && row < 5) {
                int number = cuadros[col][row].numero;
                if (number == 0 && col == 2 && row == 2) {
                    Toast.makeText(getContext(), "FREE space!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "You tapped: " + number, Toast.LENGTH_SHORT).show();
                    if(cuadros[col][row].estampa == true){
                        cuadros[col][row].estampa = false;
                        invalidate();
                    }else{
                        cuadros[col][row].estampa = true;
                        invalidate();
                    }
                }
            }
            return true;
        }
        return super.onTouchEvent(event);
    }
}
