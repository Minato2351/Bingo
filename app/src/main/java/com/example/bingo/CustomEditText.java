package com.example.bingo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.InputFilter;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

public class CustomEditText extends AppCompatEditText {
    private float escala;
    private Paint pFondo;
    private Paint pTexto;
    private static final int MAX_CHAR = 20;

    public CustomEditText(@NonNull Context context) {
        super(context);
        inicializa();
    }

    public CustomEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inicializa();
    }

    public CustomEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inicializa();
    }

    private void inicializa() {
        escala = getResources().getDisplayMetrics().density;

        pFondo = new Paint(Paint.ANTI_ALIAS_FLAG);
        pFondo.setColor(Color.BLACK);
        pFondo.setStyle(Paint.Style.FILL);

        pTexto = new Paint(Paint.ANTI_ALIAS_FLAG);
        pTexto.setColor(Color.WHITE);
        pTexto.setTextSize(20 * escala);

        setFilters(new InputFilter[] { new InputFilter.LengthFilter(MAX_CHAR) }); //bloquea teclado
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();

        float left = width - (50 * escala);
        float top = 5 * escala;
        float right = width - (5 * escala);
        float bottom = 30 * escala;

        canvas.drawRect(left, top, right, bottom, pFondo);

        int letrasEscritas = getText() != null ? getText().length() : 0;
        int restantes = MAX_CHAR - letrasEscritas;

        if (restantes == 0) {
            pFondo.setColor(Color.RED);
        } else {
            pFondo.setColor(Color.BLACK);
        }

        String txtMostrar = String.valueOf(restantes);

        float ajusteX = (restantes < 10) ? (15 * escala) : (10 * escala);

        canvas.drawText(txtMostrar, left + ajusteX, top + (20 * escala), pTexto);
    }
}
